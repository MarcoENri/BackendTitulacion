package com.example.Aplicativo_web.service.finaldefense

import com.example.Aplicativo_web.dto.finaldefense.CreateFinalDefenseBookingRequest
import com.example.Aplicativo_web.dto.finaldefense.CreateFinalDefenseSlotRequest
import com.example.Aplicativo_web.dto.finaldefense.CreateFinalDefenseWindowRequest
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseBookingDto
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseJuryDto
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseSlotDto
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseStudentMiniDto
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseWindowDto
import com.example.Aplicativo_web.entity.enums.FinalDefenseBookingStatus
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseBookingEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseBookingJuryEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseBookingJuryId
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseGroupEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseGroupMemberEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseGroupMemberId
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseSlotEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseWindowEntity
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.CareerRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingJuryRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseGroupMemberRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseGroupRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseSlotRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseWindowRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminFinalDefenseService(
    private val periodRepo: AcademicPeriodRepository,
    private val careerRepo: CareerRepository,
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,

    private val windowRepo: FinalDefenseWindowRepository,
    private val slotRepo: FinalDefenseSlotRepository,

    private val groupRepo: FinalDefenseGroupRepository,
    private val groupMemberRepo: FinalDefenseGroupMemberRepository,

    private val bookingRepo: FinalDefenseBookingRepository,
    private val bookingJuryRepo: FinalDefenseBookingJuryRepository
) {

    private fun resolvePeriodId(periodId: Long?): Long {
        if (periodId != null) return periodId
        return periodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período activo") }
            .id!!
    }

    @Transactional
    fun createWindow(req: CreateFinalDefenseWindowRequest): FinalDefenseWindowDto {
        val pid = resolvePeriodId(req.academicPeriodId)
        val period = periodRepo.findById(pid)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Período no existe") }

        val career = req.careerId?.let {
            careerRepo.findById(it).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrera no existe") }
        }

        if (!req.endsAt.isAfter(req.startsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor que startsAt")
        }

        val w = windowRepo.save(
            FinalDefenseWindowEntity(
                academicPeriod = period,
                career = career,
                startsAt = req.startsAt,
                endsAt = req.endsAt,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )

        return toWindowDto(w)
    }

    @Transactional(readOnly = true)
    fun listWindows(periodId: Long?): List<FinalDefenseWindowDto> {
        val pid = resolvePeriodId(periodId)
        return windowRepo.findAllByAcademicPeriod_IdOrderByStartsAtAsc(pid).map { toWindowDto(it) }

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
        if (!req.endsAt.isAfter(req.startsAt)) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor")
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

        return toSlotDto(slot, booked = false, bookingId = null)
    }

    @Transactional(readOnly = true)
    fun listSlots(windowId: Long): List<FinalDefenseSlotDto> {
        val slots = slotRepo.findAllByWindow_IdOrderByStartsAtAsc(windowId)
        return slots.map { s ->
            val booking = bookingRepo.findBySlot_Id(s.id!!)
            toSlotDto(s, booked = booking != null, bookingId = booking?.id)
        }
    }

    @Transactional
    fun createBooking(req: CreateFinalDefenseBookingRequest): FinalDefenseBookingDto {
        if (req.studentIds.isEmpty() || req.studentIds.size > 2) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "studentIds debe tener 1 o 2 estudiantes")
        }
        if (req.juryUserIds.distinct().size != 3) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe asignar exactamente 3 jurados")
        }

        val slot = slotRepo.findById(req.slotId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Slot no existe") }

        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot sin ventana")
        if (!window.isActive) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana cerrada")
        if (bookingRepo.findBySlot_Id(slot.id!!) != null) throw ResponseStatusException(HttpStatus.CONFLICT, "Slot ya reservado")

        val period = window.academicPeriod ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana sin período")
        val pid = period.id!!

        val students = req.studentIds.distinct().map { sid ->
            studentRepo.findById(sid).orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe: $sid")
            }
        }

        // validar periodo
        students.forEach { st ->
            val spid = st.academicPeriod?.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin período")
            if (spid != pid) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante pertenece a otro período")
        }

        // validar carrera
        val careerId = students.first().career?.id ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")
        if (students.any { it.career?.id != careerId }) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Los estudiantes deben ser de la misma carrera")
        }

        // validar ventana vs carrera (si ventana es por carrera específica)
        val wCareerId = window.career?.id
        if (wCareerId != null && wCareerId != careerId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La ventana es de otra carrera")
        }

        // ✅ VALIDACIÓN NUEVA: Usando thesisProject del estudiante
        students.forEach { st ->
            val pn = st.thesisProject?.trim()
            if (pn.isNullOrBlank()) {
                throw ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Uno o más estudiantes no tienen proyecto asignado (thesis_project)"
                )
            }
        }

        val career = careerRepo.findById(careerId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrera no existe") }

        // ✅ Obtener nombre limpio
        val projectName = students.first().thesisProject?.trim()
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Error inesperado obteniendo nombre del proyecto")

        val group = groupRepo.save(
            FinalDefenseGroupEntity(
                academicPeriod = period,
                career = career,
                projectName = projectName,
                createdAt = LocalDateTime.now()
            )
        )

        students.forEach { st ->
            groupMemberRepo.save(
                FinalDefenseGroupMemberEntity(
                    id = FinalDefenseGroupMemberId(group.id!!, st.id!!),
                    group = group,
                    student = st
                )
            )
        }

        val booking = bookingRepo.save(
            FinalDefenseBookingEntity(
                slot = slot,
                group = group,
                status = FinalDefenseBookingStatus.SCHEDULED,
                finalObservations = req.finalObservations?.trim(),
                createdAt = LocalDateTime.now()
            )
        )

        val juryUsers = req.juryUserIds.distinct().map { uid ->
            userRepo.findById(uid).orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Jurado no existe: $uid") }
        }

        juryUsers.forEach { u ->
            val hasJury = u.roles.any { it.role?.name?.equals("JURY", ignoreCase = true) == true }
            if (!hasJury) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario ${u.username} no tiene rol JURY")
        }

        juryUsers.forEach { u ->
            bookingJuryRepo.save(
                FinalDefenseBookingJuryEntity(
                    id = FinalDefenseBookingJuryId(booking.id!!, u.id!!),
                    booking = booking,
                    juryUser = u
                )
            )
        }

        return buildBookingDto(booking)
    }

    @Transactional(readOnly = true)
    fun listBookings(periodId: Long?): List<FinalDefenseBookingDto> {
        val pid = resolvePeriodId(periodId)
        return bookingRepo.findAllByPeriodFetch(pid).map { buildBookingDto(it) }
    }

    private fun toWindowDto(w: FinalDefenseWindowEntity): FinalDefenseWindowDto {
        val p = w.academicPeriod ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "window sin period")
        return FinalDefenseWindowDto(
            id = w.id!!,
            academicPeriodId = p.id!!,
            academicPeriodName = p.name,
            careerId = w.career?.id,
            careerName = w.career?.name,
            startsAt = w.startsAt,
            endsAt = w.endsAt,
            isActive = w.isActive,
            hasRubric = !w.rubricPath.isNullOrBlank() // ✅ ESTA ES LA LÍNEA QUE TE FALTA
        )
    }


    private fun toSlotDto(s: FinalDefenseSlotEntity, booked: Boolean, bookingId: Long?): FinalDefenseSlotDto {
        val w = s.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "slot sin window")
        return FinalDefenseSlotDto(
            id = s.id!!,
            windowId = w.id!!,
            startsAt = s.startsAt,
            endsAt = s.endsAt,
            booked = booked,
            bookingId = bookingId
        )
    }

    private fun buildBookingDto(b: FinalDefenseBookingEntity): FinalDefenseBookingDto {
        val slot = b.slot ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "booking sin slot")
        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "slot sin window")
        val period = window.academicPeriod ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "window sin period")

        val group = b.group ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "booking sin group")
        val career = group.career ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "group sin career")

        val members = groupMemberRepo.findAllByGroupIdFetchStudents(group.id!!)
        val students = members.map { gm ->
            val s = gm.student ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "member sin student")

            FinalDefenseStudentMiniDto(
                id = s.id!!,
                dni = s.dni,
                fullName = "${s.firstName} ${s.lastName}",
                email = s.email,
                status = s.status.name,
                projectName = s.thesisProject
            )
        }

        val juries = bookingJuryRepo.findAllByBookingIdFetchJury(b.id!!).map { bj ->
            val u = bj.juryUser ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "bj sin user")
            FinalDefenseJuryDto(
                id = u.id!!,
                username = u.username,
                fullName = u.fullName,
                email = u.email
            )
        }

        return FinalDefenseBookingDto(
            id = b.id!!,
            status = b.status,
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
            finalAverage = b.finalAverage,
            verdict = b.verdict,
            finalObservations = b.finalObservations,
            actaPath = b.actaPath
        )
    }
}