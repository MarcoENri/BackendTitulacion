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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@Service
class JuryFinalDefenseService(
    private val bookingRepo: FinalDefenseBookingRepository,
    private val bookingJuryRepo: FinalDefenseBookingJuryRepository,
    private val evalRepo: FinalDefenseEvaluationRepository,
    private val groupMemberRepo: FinalDefenseGroupMemberRepository,
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,

    // ✅ NUEVO: generar PDF + guardar en disco
    private val actaPdfService: FinalDefenseActaPdfService,
    private val actaStorage: FinalDefenseActaStorageService
) {

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

    @Transactional
    fun evaluate(
        bookingId: Long,
        req: CreateFinalDefenseEvaluationRequest,
        juryUsername: String
    ): FinalDefenseEvaluationDto {

        if (!bookingJuryRepo.isJuryAssigned(bookingId, juryUsername)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No estás asignado a este booking")
        }

        val b = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        // ✅ permitir evaluar también si ya está FINALIZED? normalmente NO:
        if (b.status != FinalDefenseBookingStatus.SCHEDULED) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El booking no está en estado SCHEDULED")
        }

        val jury = userRepo.findByUsernameIgnoreCaseOrEmailIgnoreCase(juryUsername, juryUsername)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jurado no existe") }


        // Rangos 0..50
        val rubricClamped = min(50, max(0, req.rubricScore))
        val extraClamped = min(50, max(0, req.extraScore))
        val total = rubricClamped + extraClamped

        val verdict = if (total >= 70) FinalDefenseVerdict.APROBADO else FinalDefenseVerdict.REPROBADO

        val existing = evalRepo.findByBooking_IdAndJuryUser_Id(bookingId, jury.id!!)

        val entityToSave = if (existing != null) {
            existing.apply {
                rubricScore = rubricClamped
                extraScore = extraClamped
                totalScore = total
                this.verdict = verdict
                observations = req.observations?.trim()
                createdAt = LocalDateTime.now()
            }
        } else {
            FinalDefenseEvaluationEntity(
                booking = b,
                juryUser = jury,
                rubricScore = rubricClamped,
                extraScore = extraClamped,
                totalScore = total,
                verdict = verdict,
                observations = req.observations?.trim(),
                createdAt = LocalDateTime.now()
            )
        }

        val saved = evalRepo.save(entityToSave)

        // ✅ Si ya hay 3 evaluaciones => finalizar automático (promedio + acta PDF)
        tryFinalizeIfComplete(b.id!!)

        return FinalDefenseEvaluationDto.from(saved)
    }

    private fun tryFinalizeIfComplete(bookingId: Long) {
        val b = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val evals = evalRepo.findAllByBooking_IdOrderByCreatedAtAsc(bookingId)
        if (evals.size < 3) return

        val avg = evals.map { it.totalScore }.average()
        val roundedAvg = round(avg * 100) / 100.0

        val finalVerdict =
            if (roundedAvg >= 70.0) FinalDefenseVerdict.APROBADO else FinalDefenseVerdict.REPROBADO

        b.finalAverage = roundedAvg
        b.verdict = finalVerdict
        b.status = FinalDefenseBookingStatus.FINALIZED

        // ✅ actualizar estado estudiantes
        val group = b.group ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin group")
        val members = groupMemberRepo.findAllByGroupIdFetchStudents(group.id!!)

        members.forEach { gm ->
            val st = gm.student ?: return@forEach
            st.status = if (finalVerdict == FinalDefenseVerdict.APROBADO) StudentStatus.APROBADO else StudentStatus.REPROBADO
            studentRepo.save(st)
        }

        // ✅ generar acta PDF solo si no existe
        if (b.actaPath.isNullOrBlank()) {
            val pdfBytes = actaPdfService.buildActaPdf(bookingId) // ByteArray PDF real
            val filename = actaStorage.saveActaPdf(bookingId, pdfBytes)
            b.actaPath = filename
        }

        bookingRepo.save(b)
    }

    private fun buildBookingDto(b: FinalDefenseBookingEntity): FinalDefenseBookingDto {
        val slot = b.slot ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "booking sin slot")
        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "slot sin window")
        val period = window.academicPeriod ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "window sin period")

        val group = b.group ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "booking sin group")
        val career = group.career ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "group sin career")

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
