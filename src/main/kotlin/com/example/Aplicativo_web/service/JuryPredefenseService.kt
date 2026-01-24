package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.entity.*
import com.example.Aplicativo_web.repository.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class JuryPredefenseService(
    private val studentRepo: StudentRepository,
    private val windowRepo: PredefenseWindowRepository,
    private val slotRepo: PredefenseSlotRepository,
    private val bookingRepo: PredefenseBookingRepository,
    private val bookingJuryRepo: PredefenseBookingJuryRepository,
    private val observationRepo: PredefenseObservationRepository,
    private val periodRepo: AcademicPeriodRepository,
    private val emailService: StudentEmailService,
    private val userRepo: AppUserRepository
) {

    private fun resolvePeriodId(periodId: Long?): Long {
        if (periodId != null) return periodId
        return periodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período activo") }
            .id!!
    }

    // ✅ HELPER AGREGADO: Busca por username o email de forma segura
    private fun getJury(input: String) =
        userRepo.findByUsernameIgnoreCaseOrEmailIgnoreCase(input, input)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Jurado no existe") }

    @Transactional(readOnly = true)
    fun listStudentsByCareer(careerId: Long, periodId: Long?): List<JuryCareerStudentsDto> {
        val pid = resolvePeriodId(periodId)
        return studentRepo.findAllByCareerIdAndAcademicPeriod_Id(careerId, pid)
            .map {
                JuryCareerStudentsDto(
                    id = it.id!!,
                    dni = it.dni,
                    fullName = "${it.firstName} ${it.lastName}",
                    email = it.email,
                    status = it.status.name
                )
            }
    }

    @Transactional(readOnly = true)
    fun listActiveWindowsForCareer(careerId: Long, periodId: Long?): List<PredefenseWindowDto> {
        val pid = resolvePeriodId(periodId)
        return windowRepo.findActiveForPeriodAndCareer(pid, careerId)
            .map { PredefenseWindowDto.from(it) }
    }

    @Transactional
    fun createSlot(windowId: Long, startsAt: LocalDateTime, endsAt: LocalDateTime): PredefenseSlotDto {
        val window = windowRepo.findById(windowId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana no existe") }

        if (!window.isActive) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana cerrada")
        if (endsAt.isBefore(startsAt) || endsAt.isEqual(startsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor que startsAt")
        }
        if (startsAt.isBefore(window.startsAt) || endsAt.isAfter(window.endsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El slot debe estar dentro del rango de la ventana")
        }

        val slot = slotRepo.save(
            PredefenseSlotEntity(
                window = window,
                startsAt = startsAt,
                endsAt = endsAt,
                createdAt = LocalDateTime.now()
            )
        )

        return PredefenseSlotDto.from(slot, booked = false, bookingId = null)
    }

    @Transactional(readOnly = true)
    fun listSlots(windowId: Long): List<PredefenseSlotDto> {
        val slots = slotRepo.findAllByWindow_IdOrderByStartsAtAsc(windowId)
        return slots.map { s ->
            val booking = bookingRepo.findBySlot_Id(s.id!!)
            PredefenseSlotDto.from(s, booked = booking != null, bookingId = booking?.id)
        }
    }

    @Transactional
    fun bookSlot(req: CreatePredefenseBookingRequest, juryUsername: String): PredefenseBookingDto {
        val slot = slotRepo.findById(req.slotId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Slot no existe") }

        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot sin ventana")
        if (!window.isActive) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ventana cerrada")

        if (bookingRepo.findBySlot_Id(slot.id!!) != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Slot ya reservado")
        }

        val student = studentRepo.findById(req.studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val studentPeriodId = student.academicPeriod?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El estudiante no tiene período asignado")
        val windowPeriodId = window.academicPeriod?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La ventana no tiene período")

        if (studentPeriodId != windowPeriodId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El estudiante pertenece a otro período")
        }

        val booking = bookingRepo.save(
            PredefenseBookingEntity(
                slot = slot,
                student = student,
                createdAt = LocalDateTime.now()
            )
        )

        // ✅ USO DEL HELPER
        val jury = getJury(juryUsername)

        bookingJuryRepo.save(
            PredefenseBookingJuryEntity(
                id = PredefenseBookingJuryId(
                    bookingId = booking.id!!,
                    juryUserId = jury.id!!
                ),
                booking = booking,
                juryUser = jury
            )
        )

        return PredefenseBookingDto.from(booking)
    }

    @Transactional
    fun createObservation(bookingId: Long, req: CreatePredefenseObservationRequest, juryUsername: String): PredefenseObservationDto {
        val booking = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        // ✅ USO DEL HELPER
        val jury = getJury(juryUsername)

        val obs = observationRepo.save(
            PredefenseObservationEntity(
                booking = booking,
                authorUserId = jury.id,
                authorName = jury.fullName,
                text = req.text.trim(),
                createdAt = LocalDateTime.now()
            )
        )

        val student = booking.student ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin estudiante")
        val slot = booking.slot ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin slot")
        val careerName = student.career?.name ?: "-"

        emailService.sendToStudent(
            studentId = student.id!!,
            subject = "Observaciones de Predefensa",
            body = """
                Carrera: $careerName
                Fecha/Hora: ${slot.startsAt}

                Observaciones:
                ${req.text.trim()}

                (Recuerda revisar y corregir antes de la defensa final)
            """.trimIndent(),
            senderUsernameOrEmail = juryUsername
        )

        return PredefenseObservationDto.from(obs)
    }

    @Transactional(readOnly = true)
    fun listObservations(bookingId: Long): List<PredefenseObservationDto> {
        return observationRepo.findAllByBooking_IdOrderByCreatedAtAsc(bookingId)
            .map { PredefenseObservationDto.from(it) }
    }
}