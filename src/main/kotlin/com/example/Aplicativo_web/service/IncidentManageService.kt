package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.UpdateIncidentRequest
import com.example.Aplicativo_web.entity.enums.StudentStatus
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class IncidentManageService(
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val userRepo: AppUserRepository,
    private val academicPeriodRepo: AcademicPeriodRepository
) {

    private fun resolvePeriodId(periodId: Long?): Long {
        if (periodId != null) return periodId
        val active = academicPeriodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período académico activo") }
        return active.id!!
    }

    private fun hasRole(auth: Authentication, role: String): Boolean {
        // role llega como "ADMIN" / "COORDINATOR" / "TUTOR"
        return auth.authorities.any { it.authority == "ROLE_$role" }
    }

    private fun assertCanTouchStudent(auth: Authentication, studentId: Long, periodId: Long) {
        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val studentPeriodId = student.academicPeriod?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El estudiante no tiene período académico asignado")

        if (studentPeriodId != periodId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El estudiante pertenece a otro período (studentPeriodId=$studentPeriodId) y estás usando periodId=$periodId"
            )
        }

        if (hasRole(auth, "ADMIN")) return

        val username = auth.name
        if (hasRole(auth, "COORDINATOR")) {
            val coordUsername = student.coordinator?.username
            if (coordUsername == null || coordUsername != username) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar incidencias de un estudiante que no es tuyo (coordinador)")
            }
            return
        }

        if (hasRole(auth, "TUTOR")) {
            val tutorUsername = student.tutor?.username
            if (tutorUsername == null || tutorUsername != username) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes gestionar incidencias de un estudiante que no es tuyo (tutor)")
            }
            return
        }

        throw ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado")
    }

    @Transactional
    fun updateIncident(auth: Authentication, studentId: Long, incidentId: Long, periodIdParam: Long?, req: UpdateIncidentRequest): Map<String, Any?> {
        val periodId = resolvePeriodId(periodIdParam)

        assertCanTouchStudent(auth, studentId, periodId)

        val incident = incidentRepo.findById(incidentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no existe") }

        val incStudentId = incident.student?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Incidencia no tiene estudiante asociado")

        if (incStudentId != studentId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece a este estudiante")
        }

        incident.stage = req.stage.trim()
        incident.date = req.date
        incident.reason = req.reason.trim()
        incident.action = req.action.trim()
        incident.updatedAt = LocalDateTime.now()

        incidentRepo.save(incident)

        return mapOf(
            "studentId" to studentId,
            "incidentId" to incidentId,
            "periodId" to periodId,
            "updated" to true
        )
    }

    @Transactional
    fun deleteIncident(auth: Authentication, studentId: Long, incidentId: Long, periodIdParam: Long?): Map<String, Any?> {
        val periodId = resolvePeriodId(periodIdParam)

        assertCanTouchStudent(auth, studentId, periodId)

        val incident = incidentRepo.findById(incidentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Incidencia no existe") }

        val incStudentId = incident.student?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Incidencia no tiene estudiante asociado")

        if (incStudentId != studentId) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La incidencia no pertenece a este estudiante")
        }

        incidentRepo.delete(incident)

        // ✅ recalcular estado por regla de 3 incidencias
        // CORREGIDO AQUÍ:
        val remaining = incidentRepo.countByStudentId(studentId)

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        if (remaining < 3 && student.status == StudentStatus.REPROBADO) {
            // ✅ vuelve a EN_CURSO si ya no cumple el límite
            student.status = StudentStatus.EN_CURSO
            student.notAptReason = null
            studentRepo.save(student)
        }

        return mapOf(
            "studentId" to studentId,
            "incidentId" to incidentId,
            "periodId" to periodId,
            "remainingIncidents" to remaining,
            "studentStatus" to student.status.name
        )
    }
}