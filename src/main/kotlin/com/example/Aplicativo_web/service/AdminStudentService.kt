package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.AdminStudentRow
import com.example.Aplicativo_web.repository.IncidentRepository
import com.example.Aplicativo_web.repository.ObservationRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.stereotype.Service

@Service
class AdminStudentService(
    private val studentRepo: StudentRepository,
    private val incidentRepo: IncidentRepository,
    private val observationRepo: ObservationRepository
) {

    fun listStudents(academicPeriodId: Long?): List<AdminStudentRow> {
        val students = if (academicPeriodId != null) {
            studentRepo.findByAcademicPeriod_Id(academicPeriodId)
        } else {
            studentRepo.findAll()
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
                academicPeriodName = s.academicPeriod?.name ?: "-",

                section = s.section,
                modality = s.modality,

                // ✅ AGREGADO: careerId necesario
                careerId = s.career?.id ?: 0L,
                career = s.career?.name ?: "N/A",

                titulationType = s.titulationType,
                status = s.status.name,

                incidentCount = incidentRepo.countByStudentId(id),
                observationCount = observationRepo.countByStudentId(id)
            )
        }
    }
}