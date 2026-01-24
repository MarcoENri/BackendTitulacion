// StudentMappers.kt
package com.example.Aplicativo_web.mappers

import com.example.Aplicativo_web.dto.AdminStudentRow
import com.example.Aplicativo_web.entity.StudentEntity

fun StudentEntity.toAdminRow(
    incidentCount: Long,
    observationCount: Long
): AdminStudentRow {
    val id = this.id ?: 0L
    return AdminStudentRow(
        id = id,
        dni = dni,
        firstName = firstName,
        lastName = lastName,
        email = email,
        corte = corte,
        section = section,
        modality = modality,
        // ✅ CORRECCIÓN: Se agrega el mapeo del ID de la carrera
        careerId = career?.id ?: 0L,
        career = career?.name ?: "N/A",
        titulationType = titulationType,
        status = status.name,
        incidentCount = incidentCount,
        observationCount = observationCount,
        academicPeriodName = academicPeriod?.name ?: "-"
    )
}