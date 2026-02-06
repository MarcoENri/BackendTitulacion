package com.example.Aplicativo_web.service.finaldefense

import com.example.Aplicativo_web.dto.finaldefense.*
import com.example.Aplicativo_web.entity.enums.FinalDefenseBookingStatus
import com.example.Aplicativo_web.entity.enums.FinalDefenseVerdict
import com.example.Aplicativo_web.entity.enums.StudentStatus
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseBookingEntity
import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseEvaluationEntity
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingJuryRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseEvaluationRepository
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseGroupMemberRepository
import com.example.Aplicativo_web.service.FinalDefenseActaPdfService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime
import kotlin.math.round

@Service
class JuryFinalDefenseService(
    private val bookingRepo: FinalDefenseBookingRepository,
    private val bookingJuryRepo: FinalDefenseBookingJuryRepository,
    private val evalRepo: FinalDefenseEvaluationRepository,
    private val groupMemberRepo: FinalDefenseGroupMemberRepository,
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,
    private val actaPdfService: FinalDefenseActaPdfService,
    private val actaStorage: FinalDefenseActaStorageService
) {

    // --- MÉTODOS DE CONSULTA ---

    @Transactional(readOnly = true)
    fun myBookings(juryUsername: String): List<FinalDefenseBookingDto> {
        val items = bookingJuryRepo.findMyBookingsFetch(juryUsername)
        return items.map { bj -> buildBookingDto(bj.booking!!) }
    }

    @Transactional(readOnly = true)
    fun bookingDetail(bookingId: Long, juryUsername: String): FinalDefenseBookingDto {
        if (!bookingJuryRepo.isJuryAssigned(bookingId, juryUsername)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No estás asignado a este booking")
        }
        val b = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }
        return buildBookingDto(b)
    }

    @Transactional(readOnly = true)
    fun listEvaluations(bookingId: Long, username: String): List<FinalDefenseEvaluationDto> {
        if (!bookingJuryRepo.isJuryAssigned(bookingId, username)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No asignado a este booking")
        }
        return evalRepo.findAllByBooking_IdOrderByCreatedAtAsc(bookingId).map { FinalDefenseEvaluationDto.from(it) }
    }

    // --- LÓGICA DE EVALUACIÓN ---

    @Transactional
    fun evaluate(
        bookingId: Long,
        req: CreateFinalDefenseEvaluationRequest,
        juryUsername: String
    ): FinalDefenseEvaluationDto {

        // 1. Validaciones de seguridad y existencia
        if (!bookingJuryRepo.isJuryAssigned(bookingId, juryUsername)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No estás asignado a este booking")
        }

        val booking = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        if (booking.status != FinalDefenseBookingStatus.SCHEDULED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El booking no está en estado SCHEDULED")
        }

        val jury = userRepo.findByUsernameIgnoreCaseOrEmailIgnoreCase(juryUsername, juryUsername)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jurado no existe") }

        val student = studentRepo.findById(req.studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        // 2. Validar que el estudiante pertenece al grupo del booking
        val group = booking.group ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Booking sin grupo")
        val isMember = groupMemberRepo.findAllByGroup_Id(group.id!!)
            .any { it.student?.id == student.id }

        if (!isMember) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El estudiante no pertenece a este grupo")
        }

        // 3. Procesar puntajes (0..50)
        val rubric = req.rubricScore.coerceIn(0, 50)
        val extra = req.extraScore.coerceIn(0, 50)
        val total = rubric + extra
        val verdict = if (total >= 70) FinalDefenseVerdict.APROBADO else FinalDefenseVerdict.REPROBADO

        // 4. Guardar o actualizar evaluación
        val existing = evalRepo.findByBooking_IdAndJuryUser_IdAndStudent_Id(bookingId, jury.id!!, student.id!!)

        val entity = existing?.apply {
            rubricScore = rubric
            extraScore = extra
            totalScore = total
            this.verdict = verdict
            observations = req.observations?.trim()
            createdAt = LocalDateTime.now()
        } ?: FinalDefenseEvaluationEntity(
            booking = booking,
            student = student,
            juryUser = jury,
            rubricScore = rubric,
            extraScore = extra,
            totalScore = total,
            verdict = verdict,
            observations = req.observations?.trim(),
            createdAt = LocalDateTime.now()
        )

        val saved = evalRepo.save(entity)

        // 5. Intentar finalizar el proceso si todos los jurados evaluaron a todos los alumnos
        tryFinalizeIfComplete(bookingId)

        return FinalDefenseEvaluationDto.from(saved)
    }

    private fun tryFinalizeIfComplete(bookingId: Long) {
        val booking = bookingRepo.findById(bookingId).get()
        val group = booking.group!!
        val members = groupMemberRepo.findAllByGroupIdFetchStudents(group.id!!)
        val totalJuries = bookingJuryRepo.countByBooking_Id(bookingId)

        var allStudentsHaveAllEvals = true
        val studentAverages = mutableListOf<Double>()

        // Verificar cada estudiante
        members.forEach { gm ->
            val student = gm.student ?: return@forEach
            val evals = evalRepo.findAllByBooking_IdAndStudent_Id(bookingId, student.id!!)

            if (evals.size < totalJuries) {
                allStudentsHaveAllEvals = false
                return@forEach
            }

            val avg = evals.map { it.totalScore }.average()
            val roundedAvg = round(avg * 100) / 100.0
            studentAverages.add(roundedAvg)

            // Actualizar estado del estudiante
            student.status = if (roundedAvg >= 70.0) StudentStatus.APROBADO else StudentStatus.REPROBADO
            studentRepo.save(student)
        }

        // Si falta alguna evaluación de algún jurado para algún alumno, no finalizamos
        if (!allStudentsHaveAllEvals) return

        // Finalizar booking
        val finalGroupAvg = if (studentAverages.isNotEmpty()) studentAverages.average() else 0.0
        booking.finalAverage = round(finalGroupAvg * 100) / 100.0
        booking.verdict = if (booking.finalAverage!! >= 70.0) FinalDefenseVerdict.APROBADO else FinalDefenseVerdict.REPROBADO
        booking.status = FinalDefenseBookingStatus.FINALIZED

        // Generar acta PDF
        if (booking.actaPath.isNullOrBlank()) {
            val pdf = actaPdfService.buildActaPdf(bookingId)
            val filename = actaStorage.saveActaPdf(bookingId, pdf)
            booking.actaPath = filename
        }

        bookingRepo.save(booking)
    }

    // --- HELPER PARA CONSTRUCCIÓN DE DTO ---

    private fun buildBookingDto(b: FinalDefenseBookingEntity): FinalDefenseBookingDto {
        val slot = b.slot ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Booking sin slot")
        val window = slot.window ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Slot sin window")
        val period = window.academicPeriod ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Window sin period")
        val group = b.group ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Booking sin group")
        val career = group.career ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Group sin career")

        val members = groupMemberRepo.findAllByGroupIdFetchStudents(group.id!!)
        val students = members.map { gm ->
            val s = gm.student!!
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
            val u = bj.juryUser!!
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