package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.entity.AcademicPeriodEntity
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@Service
class AcademicPeriodService(
    private val repo: AcademicPeriodRepository
) {

    @Transactional
    fun create(start: LocalDate, end: LocalDate, isActive: Boolean): AcademicPeriodEntity {
        if (end.isBefore(start)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha fin no puede ser menor a la inicial")
        }

        val name = "${monthName(start.monthValue)} ${start.year} / ${monthName(end.monthValue)} ${end.year}"

        if (isActive) {
            repo.deactivateAllActive()
        }

        val saved = repo.save(
            AcademicPeriodEntity(
                name = name,
                startDate = start,
                endDate = end,
                isActive = isActive
            )
        )
        return saved
    }

    @Transactional
    fun activate(periodId: Long) {
        if (!repo.existsById(periodId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no existe")
        }
        repo.deactivateAllActive()
        repo.activateById(periodId)
    }

    fun getActiveOrNull(): AcademicPeriodEntity? {
        return repo.findFirstByIsActiveTrueOrderByStartDateDesc().orElse(null)
    }

    private fun monthName(month: Int): String =
        when (month) {
            1 -> "Enero"
            2 -> "Febrero"
            3 -> "Marzo"
            4 -> "Abril"
            5 -> "Mayo"
            6 -> "Junio"
            7 -> "Julio"
            8 -> "Agosto"
            9 -> "Septiembre"
            10 -> "Octubre"
            11 -> "Noviembre"
            12 -> "Diciembre"
            else -> ""
        }
}
