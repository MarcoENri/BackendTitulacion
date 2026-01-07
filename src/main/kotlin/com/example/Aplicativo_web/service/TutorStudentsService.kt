package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.entity.IncidentEntity
import com.example.Aplicativo_web.entity.ObservationEntity
import com.example.Aplicativo_web.repository.*
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

    private fun getTutor(username: String) =
        userRepo.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe")

    // -----------------------
    // LIST
    // -----------------------
    fun listMyStudents(username: String): List<Map<String, Any?>> {
        val tutor = getTutor(username)
        val students = studentRepo.findAllByTutorId(tutor.id!!)

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
                "status" to s.status,
                "coordinatorId" to s.coordinator?.id,
                "tutorId" to s.tutor?.id,
                "thesisProject" to s.thesisProject,
                "thesisProjectSetAt" to s.thesisProjectSetAt,
                "incidentCount" to incidentRepo.countByStudent_Id(s.id!!),
                "observationCount" to observationRepo.countByStudent_Id(s.id!!)
            )
        }
    }

    // -----------------------
    // DETAIL (solo si pertenece al tutor)
    // -----------------------
    fun getDetail(username: String, studentId: Long): StudentDetailDto {
        val tutor = getTutor(username)

        val student = studentRepo.findByIdAndTutor_Id(studentId, tutor.id!!)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes ver un estudiante que no es tuyo")

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
            status = student.status,

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

    // -----------------------
    // CREATE INCIDENT
    // -----------------------
    @Transactional
    fun createIncident(username: String, studentId: Long, req: CreateIncidentRequest) {
        val tutor = getTutor(username)

        val student = studentRepo.findByIdAndTutor_Id(studentId, tutor.id!!)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar incidencias de un estudiante que no es tuyo")

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

        // ✅ misma regla que coordinador: 3 incidencias => REPROBADO
        val count = incidentRepo.countByStudent_Id(studentId)
        if (count >= 3 && student.status != "REPROBADO") {
            student.status = "REPROBADO"
            student.notAptReason = "Acumuló $count incidencias"
            studentRepo.save(student)
        }
    }

    // -----------------------
    // CREATE OBSERVATION
    // -----------------------
    @Transactional
    fun createObservation(username: String, studentId: Long, req: CreateObservationRequest) {
        val tutor = getTutor(username)

        val student = studentRepo.findByIdAndTutor_Id(studentId, tutor.id!!)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes registrar observaciones de un estudiante que no es tuyo")

        val obs = ObservationEntity(
            student = student,
            author = tutor.fullName,     // ✅ el nombre del tutor
            text = req.text.trim(),
            createdAt = LocalDateTime.now(),
            authorUserId = tutor.id
        )
        observationRepo.save(obs)
    }
}
