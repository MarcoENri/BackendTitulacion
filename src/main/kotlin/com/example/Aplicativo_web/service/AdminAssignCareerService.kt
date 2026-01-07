package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.AdminAssignCareerRequest
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.repository.UserCareerRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminAssignCareerService(
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,
    private val userCareerRepo: UserCareerRepository
) {

    @Transactional
    fun assignByCareer(careerId: Long, req: AdminAssignCareerRequest) {

        val coordinator = userRepo.findById(req.coordinatorId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Coordinador no existe") }

        // ✅ validar que el coordinador tenga esa carrera asignada en user_career
        val coordinatorCareerIds = userCareerRepo.findCareerIdsByUserId(coordinator.id!!)
        if (!coordinatorCareerIds.contains(careerId)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ese coordinador NO tiene asignada esa carrera")
        }

        val tutor = req.tutorId?.let {
            userRepo.findById(it).orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Tutor no existe") }
        }

        val onlyUnassigned = req.onlyUnassigned ?: true
        val students = if (onlyUnassigned) {
            // ✅ solo estudiantes de esa carrera SIN coordinator/tutor asignado
            studentRepo.findAllByCareerIdAndCoordinatorIsNull(careerId)
        } else {
            // ✅ todos los estudiantes de esa carrera
            studentRepo.findAllByCareerId(careerId)
        }

        val projectTrim = req.projectName?.trim()?.takeIf { it.isNotBlank() }

        students.forEach { s ->
            s.coordinator = coordinator
            s.tutor = tutor

            if (projectTrim != null) {
                s.thesisProject = projectTrim
                s.thesisProjectSetAt = LocalDateTime.now()
            }
        }

        studentRepo.saveAll(students)
    }
}
