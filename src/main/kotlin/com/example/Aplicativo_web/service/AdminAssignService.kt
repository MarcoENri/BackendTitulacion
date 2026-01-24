package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.AdminAssignCareerRequest
import com.example.Aplicativo_web.dto.AdminAssignStudentRequest
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class AdminAssignService(
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,
    private val academicPeriodRepo: AcademicPeriodRepository
) {

    private fun resolvePeriodId(academicPeriodId: Long?): Long {
        if (academicPeriodId != null) return academicPeriodId

        val active = academicPeriodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow {
                ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período académico activo")
            }

        return active.id!!
    }

    @Transactional
    fun assignByCareer(req: AdminAssignCareerRequest): Map<String, Any?> {
        val periodId = resolvePeriodId(req.academicPeriodId)

        val coordinator = userRepo.findById(req.coordinatorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinador no existe") }

        val tutor = req.tutorId?.let { tutorId ->
            userRepo.findById(tutorId)
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor no existe") }
        }

        // ✅ CLAVE: careerId DEBE ser Long en el DTO
        val careerId = req.careerId

        val students = if (req.onlyUnassigned != false) {
            studentRepo.findAllByCareerIdAndAcademicPeriod_IdAndCoordinatorIsNull(careerId, periodId)
        } else {
            studentRepo.findAllByCareerIdAndAcademicPeriod_Id(careerId, periodId)
        }

        students.forEach { s ->
            s.coordinator = coordinator
            if (tutor != null) s.tutor = tutor
            if (!req.projectName.isNullOrBlank()) s.thesisProject = req.projectName.trim()
        }

        studentRepo.saveAll(students)

        return mapOf(
            "periodId" to periodId,
            "careerId" to careerId,
            "updatedCount" to students.size
        )
    }

    @Transactional
    fun assignStudent(studentId: Long, req: AdminAssignStudentRequest): Map<String, Any?> {
        val periodId = resolvePeriodId(req.academicPeriodId)

        val coordinator = userRepo.findById(req.coordinatorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinador no existe") }

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

        student.coordinator = coordinator
        studentRepo.save(student)

        // ✅ evita Long? / Map<String, Long?> mezclas
        return mapOf(
            "studentId" to student.id!!,
            "periodId" to periodId,
            "coordinatorId" to coordinator.id!!,
            "message" to "Asignación aplicada"
        )
    }
}
