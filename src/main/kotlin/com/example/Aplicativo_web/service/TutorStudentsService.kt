package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.entity.IncidentEntity
import com.example.Aplicativo_web.entity.ObservationEntity
import com.example.Aplicativo_web.entity.enums.StudentStatus
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.ObservationRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class TutorStudentsService(
    private val userRepo: AppUserRepository,
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {

    private fun getTutor(input: String) =
        userRepo.findByUsernameIgnoreCaseOrEmailIgnoreCase(input, input)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe") }

    fun listMyStudents(usernameOrEmail: String, periodId: Long): List<Map<String, Any?>> {
        val tutor = getTutor(usernameOrEmail)
        val students = studentRepo.findAllByTutorIdAndAcademicPeriodId(tutor.id!!, periodId)

        return students.map { s ->
            mapOf(
                "id" to s.id,
                "dni" to s.dni,
                "firstName" to s.firstName,
                "lastName" to s.lastName,
                "email" to s.email,
                "corte" to s.corte,
                "section" to s.section,
                "modality" to s.modality,
                "career" to s.career?.name,
                "titulationType" to s.titulationType,
                "status" to s.status.name,
                "coordinatorId" to s.coordinator?.id,
                "tutorId" to s.tutor?.id,
                "thesisProject" to s.thesisProject,
                "thesisProjectSetAt" to s.thesisProjectSetAt,
                "incidentCount" to incidentRepo.countByStudentId(s.id!!),
                "observationCount" to observationRepo.countByStudentId(s.id!!)
            )
        }
    }

    fun getDetail(usernameOrEmail: String, studentId: Long, periodId: Long): StudentDetailDto {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver un estudiante que no es tuyo o no es del período")

        val incidents = incidentRepo.findAllByStudent_Id(studentId).map {
            IncidentDto(
                id = it.id!!,
                stage = it.stage,
                date = it.date,
                reason = it.reason,
                action = it.action,
                createdAt = it.createdAt,
                createdByUserId = it.createdByUserId
            )
        }

        val observations = observationRepo.findAllByStudent_Id(studentId).map {
            ObservationDto(
                id = it.id!!,
                author = it.author,
                text = it.text,
                createdAt = it.createdAt,
                authorUserId = it.authorUserId
            )
        }

        return StudentDetailDto(
            id = student.id!!,
            dni = student.dni,
            firstName = student.firstName,
            lastName = student.lastName,
            email = student.email,
            corte = student.corte,
            section = student.section,
            modality = student.modality,
            career = student.career?.name ?: "-",
            titulationType = student.titulationType,
            status = student.status.name,
            tutorId = student.tutor?.id,
            coordinatorId = student.coordinator?.id,
            thesisProject = student.thesisProject,
            thesisProjectSetAt = student.thesisProjectSetAt,
            incidentCount = incidents.size.toLong(),
            observationCount = observations.size.toLong(),
            incidents = incidents,
            observations = observations
        )
    }

    @Transactional
    fun createIncident(usernameOrEmail: String, studentId: Long, periodId: Long, req: CreateIncidentRequest) {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar incidencias de un estudiante que no es tuyo o no es del período")

        val incident = IncidentEntity(
            student = student,
            stage = req.stage.trim(),
            date = req.date,
            reason = req.reason.trim(),
            action = req.action.trim(),
            createdAt = LocalDateTime.now(),
            createdByUserId = tutor.id
        )
        incidentRepo.save(incident)

        val count = incidentRepo.countByStudentId(studentId)
        if (count >= 3 && student.status != StudentStatus.REPROBADO) {
            student.status = StudentStatus.REPROBADO
            student.notAptReason = "Acumuló $count incidencias"
            studentRepo.save(student)
        }
    }

    @Transactional
    fun createObservation(usernameOrEmail: String, studentId: Long, periodId: Long, req: CreateObservationRequest) {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes registrar observaciones de un estudiante que no es tuyo o no es del período")

        val obs = ObservationEntity(
            student = student,
            author = tutor.fullName,
            text = req.text.trim(),
            createdAt = LocalDateTime.now(),
            authorUserId = tutor.id
        )
        observationRepo.save(obs)
    }

    @Transactional
    fun updateIncident(usernameOrEmail: String, studentId: Long, incidentId: Long, periodId: Long, req: UpdateIncidentRequest) {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar incidencias de un estudiante que no es tuyo o no es del período")

        val incident = incidentRepo.findById(incidentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no existe") }

        if (incident.student?.id != student.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece a este estudiante")
        }

        incident.stage = req.stage.trim()
        incident.date = req.date
        incident.reason = req.reason.trim()
        incident.action = req.action.trim()
        incident.updatedAt = LocalDateTime.now()

        incidentRepo.save(incident)
    }

    @Transactional
    fun updateObservation(usernameOrEmail: String, studentId: Long, observationId: Long, periodId: Long, req: UpdateObservationRequest) {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes editar observaciones de un estudiante que no es tuyo o no es del período")

        val obs = observationRepo.findById(observationId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Observación no existe") }

        if (obs.student?.id != student.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La observación no pertenece a este estudiante")
        }

        obs.text = req.text.trim()
        obs.updatedAt = LocalDateTime.now()

        observationRepo.save(obs)
    }

    @Transactional
    fun deleteIncident(usernameOrEmail: String, studentId: Long, incidentId: Long, periodId: Long) {
        val tutor = getTutor(usernameOrEmail)

        val student = studentRepo.findByIdAndTutorIdAndAcademicPeriodId(studentId, tutor.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes eliminar incidencias de un estudiante que no es tuyo o no es del período")

        val incident = incidentRepo.findById(incidentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no existe") }

        if (incident.student?.id != student.id) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Incidencia no pertenece al estudiante")
        }

        incidentRepo.delete(incident)

        val remaining = incidentRepo.countByStudentId(student.id!!)

        if (remaining < 3 && student.status == StudentStatus.REPROBADO) {
            student.status = StudentStatus.EN_CURSO
            student.notAptReason = null
            studentRepo.save(student)
        }
    }
}
