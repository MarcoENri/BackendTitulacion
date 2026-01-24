package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AdminStudentRow
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.ObservationRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/students")
class AdminStudentsController(
    private val studentRepository: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(@RequestParam(required = false) academicPeriodId: Long?): List<AdminStudentRow> {
        val students = if (academicPeriodId != null) {
            studentRepository.findByAcademicPeriod_Id(academicPeriodId)
        } else {
            studentRepository.findAll()
        }

        return students.map { s ->
            val id = s.id!!
            AdminStudentRow(
                id = id,
                dni = s.dni,
                firstName = s.firstName,
                lastName = s.lastName,
                email = s.email,
                corte = s.corte,
                section = s.section,
                modality = s.modality,

                career = s.career?.name ?: "N/A",
                careerId = s.career?.id,        // ✅ Campo agregado correctamente

                titulationType = s.titulationType,
                status = s.status.name,
                incidentCount = incidentRepo.countByStudentId(id),
                observationCount = observationRepo.countByStudentId(id),
                academicPeriodName = s.academicPeriod?.name ?: "-"
            )
        }
    }
}