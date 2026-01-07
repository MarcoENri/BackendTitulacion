package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.AdminAssignStudentRequest
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.repository.UserCareerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminAssignStudentService(
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,
    private val userCareerRepo: UserCareerRepository
) {

    @Transactional
    fun assign(studentId: Long, req: AdminAssignStudentRequest) {
        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val studentCareerId = student.career?.id
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Estudiante sin carrera")

        val coordinator = userRepo.findById(req.coordinatorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinador no existe") }

        // ✅ validar que ese coordinador tenga la carrera del estudiante en user_career
        val careerIds = userCareerRepo.findCareerIdsByUserId(coordinator.id!!)
        if (!careerIds.contains(studentCareerId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese coordinador no tiene asignada la carrera del estudiante")
        }

        val tutor = req.tutorId?.let {
            userRepo.findById(it).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor no existe") }
        }

        student.coordinator = coordinator
        student.tutor = tutor

        val projectTrim = req.projectName?.trim()?.takeIf { it.isNotBlank() }
        student.thesisProject = projectTrim
        student.thesisProjectSetAt = if (projectTrim != null) LocalDateTime.now() else null

        studentRepo.save(student)
    }
}
