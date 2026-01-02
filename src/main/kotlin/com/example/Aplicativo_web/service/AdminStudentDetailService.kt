package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.ObservationRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

@Service
class AdminStudentDetailService(
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {
    fun getDetail(studentId: Long): StudentDetailDto {
        val s = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no encontrado") }

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
            status = s.status,

            // ✅ NUEVO (V2)
            tutorId = s.tutor?.id,
            coordinatorId = s.coordinator?.id,
            thesisProject = s.thesisProject,
            thesisProjectSetAt = s.thesisProjectSetAt,

            incidentCount = incidentRepo.countByStudent_Id(studentId),
            observationCount = observationRepo.countByStudent_Id(studentId),
            incidents = incidents,
            observations = observations
        )

    }
}
