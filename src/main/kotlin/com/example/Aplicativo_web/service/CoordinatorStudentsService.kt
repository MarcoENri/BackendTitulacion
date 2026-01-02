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
class CoordinatorStudentsService(
    private val userRepo: AppUserRepository,
    private val userCareerRepo: UserCareerRepository,
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {

    // -----------------------
    // helpers
    // -----------------------
    private fun getCoordinator(username: String) =
        userRepo.findByUsername(username)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe")

    private fun ensureCanManageStudent(coordinatorId: Long, studentCareerId: Long) {
        val allowed = userCareerRepo.findCareerIdsByUserId(coordinatorId)
        if (!allowed.contains(studentCareerId)) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar estudiantes de otra carrera")
        }
    }

    // -----------------------
    // LIST
    // -----------------------
    fun listMyStudents(username: String): List<Map<String, Any?>> {
        val user = getCoordinator(username)

        val careerIds = userCareerRepo.findCareerIdsByUserId(user.id!!)
        if (careerIds.isEmpty()) return emptyList()

        return studentRepo.findAllByCareerIds(careerIds).map { s ->
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
                "tutorId" to s.tutor?.id,
                "coordinatorId" to s.coordinator?.id,
                "thesisProject" to s.thesisProject,
                "thesisProjectSetAt" to s.thesisProjectSetAt
            )
        }
    }

    // -----------------------
    // DETAIL
    // -----------------------
    fun getDetail(username: String, studentId: Long): StudentDetailDto {
        val user = getCoordinator(username)

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val careerId = student.career?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        ensureCanManageStudent(user.id!!, careerId)

        val incidents = incidentRepo.findAllByStudent_Id(studentId).map {
            IncidentDto(
                id = it.id!!,
                stage = it.stage,
                date = it.date,              // ✅ LocalDate
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
    // ASSIGN PROJECT + TUTOR
    // -----------------------
    @Transactional
    fun assignProject(username: String, studentId: Long, req: AssignProjectRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val careerId = student.career?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        ensureCanManageStudent(coordinator.id!!, careerId)

        val tutor = userRepo.findById(req.tutorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor no existe") }

        student.thesisProject = req.projectName.trim()
        student.thesisProjectSetAt = LocalDateTime.now()
        student.tutor = tutor
        student.coordinator = coordinator

        studentRepo.save(student)
    }

    // -----------------------
    // CREATE INCIDENT (max 3 => REPROBADO)
    // -----------------------
    @Transactional
    fun createIncident(username: String, studentId: Long, req: CreateIncidentRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val careerId = student.career?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        ensureCanManageStudent(coordinator.id!!, careerId)

        val incident = IncidentEntity(
            student = student,
            stage = req.stage.trim(),
            date = req.date, // ✅ LocalDate
            reason = req.reason.trim(),
            action = req.action.trim(),
            createdAt = LocalDateTime.now(),
            createdByUserId = coordinator.id
        )
        incidentRepo.save(incident)

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
        val coordinator = getCoordinator(username)

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val careerId = student.career?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        ensureCanManageStudent(coordinator.id!!, careerId)

        val obs = ObservationEntity(
            student = student,
            author = coordinator.fullName,
            text = req.text.trim(),
            createdAt = LocalDateTime.now(),
            authorUserId = coordinator.id
        )
        observationRepo.save(obs)
    }
}
