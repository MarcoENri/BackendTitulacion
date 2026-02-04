package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.finaldefense.*
import com.example.Aplicativo_web.entity.finaldefense.*
import com.example.Aplicativo_web.repository.*
import com.example.Aplicativo_web.repository.finaldefense.*
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class FinalDefenseAdminService(
    private val periodRepo: AcademicPeriodRepository,
    private val careerRepo: CareerRepository,
    private val studentProjectRepo: StudentProjectRepository, // Se mantiene por si se usa en otro lado, aunque aquí ya no es crítico
    private val userRepo: AppUserRepository,
    private val studentRepo: StudentRepository,
    private val windowRepo: FinalDefenseWindowRepository,
    private val slotRepo: FinalDefenseSlotRepository,
    private val bookingRepo: FinalDefenseBookingRepository,
    private val groupRepo: FinalDefenseGroupRepository,
    private val groupMemberRepo: FinalDefenseGroupMemberRepository,
    private val bookingJuryRepo: FinalDefenseBookingJuryRepository
) {

    private fun resolvePeriodId(periodId: Long?): Long =
        periodId ?: periodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período activo") }
            .id!!

    @Transactional
    fun createWindow(req: CreateFinalDefenseWindowRequest): FinalDefenseWindowDto {
        val pid = resolvePeriodId(req.academicPeriodId)
        val period = periodRepo.findById(pid)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Período no existe") }

        if (!req.endsAt.isAfter(req.startsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor que startsAt")
        }

        val career = req.careerId?.let {
            careerRepo.findById(it).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrera no existe") }
        }

        val saved = windowRepo.save(
            FinalDefenseWindowEntity(
                academicPeriod = period,
                career = career,
                startsAt = req.startsAt,
                endsAt = req.endsAt,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )

        return FinalDefenseWindowDto.from(saved)
    }

    @Transactional(readOnly = true)
    fun listWindows(periodId: Long?): List<FinalDefenseWindowDto> {
        val pid = resolvePeriodId(periodId)
        return windowRepo.findAllForPeriod(pid).map { FinalDefenseWindowDto.from(it) }
    }

    @Transactional
    fun closeWindow(windowId: Long) {
        val w = windowRepo.findById(windowId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana no existe") }
        w.isActive = false
        windowRepo.save(w)
    }

    @Transactional
    fun createSlot(windowId: Long, req: CreateFinalDefenseSlotRequest): FinalDefenseSlotDto {
        val w = windowRepo.findById(windowId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana no existe") }

        if (!w.isActive) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana cerrada")
        if (!req.endsAt.isAfter(req.startsAt)) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor que startsAt")
        if (req.startsAt.isBefore(w.startsAt) || req.endsAt.isAfter(w.endsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El slot debe estar dentro del rango de la ventana")
        }

        val slot = slotRepo.save(
            FinalDefenseSlotEntity(
                window = w,
                startsAt = req.startsAt,
                endsAt = req.endsAt,
                createdAt = LocalDateTime.now()
            )
        )

        return FinalDefenseSlotDto.from(slot, booked = false, bookingId = null)
    }

    @Transactional(readOnly = true)
    fun listSlots(windowId: Long): List<FinalDefenseSlotDto> {
        val slots = slotRepo.findAllByWindow_IdOrderByStartsAtAsc(windowId)
        return slots.map { s ->
            val b = bookingRepo.findBySlot_Id(s.id!!)
            FinalDefenseSlotDto.from(s, booked = b != null, bookingId = b?.id)
        }
    }

    @Transactional(readOnly = true)
    fun listStudentsForCareer(careerId: Long, periodId: Long?): List<FinalDefenseStudentMiniDto> {
        val pid = resolvePeriodId(periodId)
        val students = studentRepo.findAllByCareerIdAndAcademicPeriod_Id(careerId, pid)

        return students.map { s ->
            // ✅ LIMPIEZA: Usamos directamente thesisProject del estudiante (más rápido)
            FinalDefenseStudentMiniDto(
                id = s.id!!,
                dni = s.dni,
                fullName = "${s.firstName} ${s.lastName}",
                email = s.email,
                status = s.status.name,
                projectName = s.thesisProject // Mapeo directo de la entidad Student
            )
        }
    }

    @Transactional
    fun createBooking(req: CreateFinalDefenseBookingRequest): FinalDefenseBookingDto {
        // Validaciones iniciales
        if (req.studentIds.isEmpty() || req.studentIds.size > 2) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes seleccionar 1 o 2 estudiantes")
        }
        if (req.juryUserIds.distinct().size < 1) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Debes asignar exactamente al menos un jurado")
        }

        val slot = slotRepo.findById(req.slotId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Slot no existe") }

        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot sin ventana")
        if (!window.isActive) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana cerrada")
        if (bookingRepo.findBySlot_Id(slot.id!!) != null) throw ResponseStatusException(HttpStatus.CONFLICT, "Ese slot ya está reservado")

        val students = studentRepo.findAllById(req.studentIds).toList()
        if (students.size != req.studentIds.size) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más estudiantes no existen")

        val periodId = window.academicPeriod?.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana sin período")
        val firstCareer = students.first().career?.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        // Validaciones de estudiantes (mismo periodo y carrera)
        students.forEach { s ->
            val sp = s.academicPeriod?.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin período")
            if (sp != periodId) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante pertenece a otro período")
            if (s.career?.id != firstCareer) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Los estudiantes deben ser de la misma carrera")
        }
        window.career?.id?.let { widCareer ->
            if (widCareer != firstCareer) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La carrera del estudiante no coincide con la ventana")
        }

        // ✅ CORRECCIÓN APLICADA: Usar student.thesisProject
        val projects = students.map { st -> st.thesisProject?.trim() }

        // 1. Verificar que todos tengan proyecto
        if (projects.any { it.isNullOrBlank() }) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Uno o más estudiantes no tienen proyecto asignado (thesis_project)"
            )
        }

        // 2. Si son 2 estudiantes, verificar que sea el mismo proyecto exacto
        if (projects.distinct().size != 1) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Los estudiantes seleccionados no tienen el mismo proyecto"
            )
        }

        val validProjectName = projects.first()!!

        // Crear Grupo
        val group = groupRepo.save(
            FinalDefenseGroupEntity(
                academicPeriod = window.academicPeriod,
                career = students.first().career,
                projectName = validProjectName,
                createdAt = LocalDateTime.now()
            )
        )

        students.forEach { s ->
            groupMemberRepo.save(
                FinalDefenseGroupMemberEntity(
                    id = FinalDefenseGroupMemberId(group.id!!, s.id!!),
                    group = group,
                    student = s
                )
            )
        }

        val booking = bookingRepo.save(
            FinalDefenseBookingEntity(
                slot = slot,
                group = group,
                status = com.example.Aplicativo_web.entity.enums.FinalDefenseBookingStatus.SCHEDULED,
                finalObservations = req.finalObservations?.trim(),
                createdAt = LocalDateTime.now()
            )
        )

        val juries = userRepo.findAllById(req.juryUserIds).toList()
        if (juries.size < 1) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Uno o más jurados no existen")

        juries.forEach { j ->
            bookingJuryRepo.save(
                FinalDefenseBookingJuryEntity(
                    id = FinalDefenseBookingJuryId(booking.id!!, j.id!!),
                    booking = booking,
                    juryUser = j
                )
            )
        }

        return buildBookingDto(booking.id!!)
    }

    @Transactional(readOnly = true)
    fun bookingDetail(bookingId: Long): FinalDefenseBookingDto = buildBookingDto(bookingId)

    private fun buildBookingDto(bookingId: Long): FinalDefenseBookingDto {
        val booking = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val slot = booking.slot
        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "slot sin window")
        val period = window.academicPeriod ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "window sin period")
        val group = booking.group
        val career = group.career ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "group sin career")

        val members = groupMemberRepo.findAllByGroupIdFetchStudents(group.id!!)
        val students = members.mapNotNull { it.student }.map { FinalDefenseStudentMiniDto.from(it) }

        val juries = bookingJuryRepo.findAllByBookingIdFetchJury(booking.id!!).map { FinalDefenseJuryDto.from(it.juryUser!!) }

        return FinalDefenseBookingDto(
            id = booking.id!!,
            status = booking.status,
            slotId = slot.id!!,
            startsAt = slot.startsAt,
            endsAt = slot.endsAt,
            academicPeriodId = period.id!!,
            careerId = career.id!!,
            careerName = career.name,
            groupId = group.id!!,
            projectName = group.projectName,
            students = students,
            jury = juries,
            finalAverage = booking.finalAverage,
            verdict = booking.verdict,
            finalObservations = booking.finalObservations,
            actaPath = booking.actaPath
        )
    }
}