package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.IncidentDto
import com.example.Aplicativo_web.dto.ObservationDto
import com.example.Aplicativo_web.dto.StudentDetailDto
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.ObservationRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class AdminStudentDetailService(
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {
    fun getDetail(studentId: Long): StudentDetailDto {
        val s = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado") }

        // Si tus repositorios usan findAllByStudentId (sin guion bajo), cámbialo aquí también.
        // Por ahora mantengo el findAllByStudent_Id como en el original,
        // pero ajusto los conteos que son los que han dado error.
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
            id = s.id!!,
            dni = s.dni,
            firstName = s.firstName,
            lastName = s.lastName,
            email = s.email,
            corte = s.corte,
            section = s.section,
            modality = s.modality,
            career = s.career?.name ?: "N/A",
            titulationType = s.titulationType,
            status = s.status.toString(), // ✅ enum->String

            tutorId = s.tutor?.id,
            coordinatorId = s.coordinator?.id,
            thesisProject = s.thesisProject,
            thesisProjectSetAt = s.thesisProjectSetAt,

            // CORREGIDO AQUI:
            incidentCount = incidentRepo.countByStudentId(studentId),
            // CORREGIDO AQUI:
            observationCount = observationRepo.countByStudentId(studentId),

            incidents = incidents,
            observations = observations
        )
    }
}