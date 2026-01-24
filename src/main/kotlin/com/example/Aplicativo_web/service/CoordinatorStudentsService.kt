package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.entity.IncidentEntity
import com.example.Aplicativo_web.entity.ObservationEntity
import com.example.Aplicativo_web.entity.enums.StudentStatus
import com.example.Aplicativo_web.repository.*
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class CoordinatorStudentsService(
    private val userRepo: AppUserRepository,
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository,
    private val academicPeriodRepo: AcademicPeriodRepository
) {

    // ✅ DEJA SOLO UNA (username OR email)
    private fun getCoordinator(input: String) =
        userRepo.findByUsernameIgnoreCaseOrEmailIgnoreCase(input, input)
            .orElseThrow { ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe") }

    private fun resolvePeriodId(periodId: Long?): Long {
        if (periodId != null) return periodId

        val active = academicPeriodRepo
            .findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período académico activo")
            }

        return active.id!!
    }

    // -----------------------
    // LIST (SOLO MIS ESTUDIANTES DEL PERIODO)
    // -----------------------
    fun listMyStudents(username: String, periodId: Long?): List<Map<String, Any?>> {
        val pid = resolvePeriodId(periodId)
        val coordinator = getCoordinator(username)

        val students = studentRepo.findAllByCoordinatorIdAndAcademicPeriodId(coordinator.id!!, pid)

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
                "tutorId" to s.tutor?.id,
                "coordinatorId" to s.coordinator?.id,
                "tutorName" to s.tutor?.fullName,
                "tutorUsername" to s.tutor?.username,
                "coordinatorName" to s.coordinator?.fullName,
                "coordinatorUsername" to s.coordinator?.username,
                "thesisProject" to s.thesisProject,
                "thesisProjectSetAt" to s.thesisProjectSetAt,
                "incidentCount" to incidentRepo.countByStudentId(s.id!!),
                "observationCount" to observationRepo.countByStudentId(s.id!!)
            )
        }
    }

    // -----------------------
    // DETAIL (SOLO SI ES MIO Y DEL PERIODO)
    // -----------------------
    fun getDetail(username: String, studentId: Long, periodId: Long): StudentDetailDto {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
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

    // -----------------------
    // ASSIGN PROJECT + TUTOR (SOLO SI ES MIO Y DEL PERIODO)
    // -----------------------
    @Transactional
    fun assignProject(username: String, studentId: Long, periodId: Long, req: AssignProjectRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes asignar proyecto a un estudiante que no es tuyo o no es del período")

        val tutor = userRepo.findById(req.tutorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor no existe") }

        student.thesisProject = req.projectName.trim()
        student.thesisProjectSetAt = LocalDateTime.now()
        student.tutor = tutor

        studentRepo.save(student)
    }

    // -----------------------
    // CREATE INCIDENT (SOLO SI ES MIO Y DEL PERIODO)
    // -----------------------
    @Transactional
    fun createIncident(username: String, studentId: Long, periodId: Long, req: CreateIncidentRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar incidencias de un estudiante que no es tuyo o no es del período")

        val incident = IncidentEntity(
            student = student,
            stage = req.stage.trim(),
            date = req.date,
            reason = req.reason.trim(),
            action = req.action.trim(),
            createdAt = LocalDateTime.now(),
            createdByUserId = coordinator.id
        )
        incidentRepo.save(incident)

        val count = incidentRepo.countByStudentId(studentId)

        if (count >= 3 && student.status != StudentStatus.REPROBADO) {
            student.status = StudentStatus.REPROBADO
            student.notAptReason = "Acumuló $count incidencias"
            studentRepo.save(student)
        }
    }

    // -----------------------
    // CREATE OBSERVATION (SOLO SI ES MIO Y DEL PERIODO)
    // -----------------------
    @Transactional
    fun createObservation(username: String, studentId: Long, periodId: Long, req: CreateObservationRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
            ?: throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes registrar observaciones de un estudiante que no es tuyo o no es del período")

        val obs = ObservationEntity(
            student = student,
            author = coordinator.fullName,
            text = req.text.trim(),
            createdAt = LocalDateTime.now(),
            authorUserId = coordinator.id
        )
        observationRepo.save(obs)
    }

    // -----------------------
    // UPDATE INCIDENT
    // -----------------------
    @Transactional
    fun updateIncident(username: String, studentId: Long, incidentId: Long, periodId: Long, req: UpdateIncidentRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
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

    // -----------------------
    // UPDATE OBSERVATION
    // -----------------------
    @Transactional
    fun updateObservation(username: String, studentId: Long, observationId: Long, periodId: Long, req: UpdateObservationRequest) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
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

    // -----------------------
    // DELETE INCIDENT
    // -----------------------
    @Transactional
    fun deleteIncident(username: String, studentId: Long, incidentId: Long, periodId: Long) {
        val coordinator = getCoordinator(username)

        val student = studentRepo.findByIdAndCoordinatorIdAndAcademicPeriodId(studentId, coordinator.id!!, periodId)
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
