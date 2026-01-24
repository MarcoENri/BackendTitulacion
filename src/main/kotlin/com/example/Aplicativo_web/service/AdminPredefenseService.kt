package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.CreatePredefenseWindowRequest
import com.example.Aplicativo_web.dto.PredefenseWindowDto
import com.example.Aplicativo_web.entity.PredefenseWindowEntity
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.CareerRepository
import com.example.Aplicativo_web.repository.PredefenseWindowRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminPredefenseService(
    private val windowRepo: PredefenseWindowRepository,
    private val periodRepo: AcademicPeriodRepository,
    private val careerRepo: CareerRepository
) {

    private fun resolvePeriodId(periodId: Long?): Long {
        if (periodId != null) return periodId
        return periodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay período académico activo") }
            .id!!
    }

    @Transactional
    fun createWindow(req: CreatePredefenseWindowRequest): PredefenseWindowDto {
        val periodId = resolvePeriodId(req.academicPeriodId)

        val period = periodRepo.findById(periodId)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Periodo no existe") }

        val career = req.careerId?.let { cid ->
            careerRepo.findById(cid)
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no existe") }
        }

        if (req.endsAt.isBefore(req.startsAt) || req.endsAt.isEqual(req.startsAt)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "endsAt debe ser mayor que startsAt")
        }

        val window = windowRepo.save(
            PredefenseWindowEntity(
                academicPeriod = period,
                career = career,
                startsAt = req.startsAt,
                endsAt = req.endsAt,
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )

        return PredefenseWindowDto.from(window)
    }

    @Transactional(readOnly = true)
    fun listWindows(periodId: Long?): List<PredefenseWindowDto> {
        val pid = resolvePeriodId(periodId)
        return windowRepo.findAllByAcademicPeriod_IdOrderByStartsAtDesc(pid)
            .map { PredefenseWindowDto.from(it) }
    }

    @Transactional
    fun closeWindow(id: Long): Map<String, Any?> {
        val w = windowRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana no existe") }

        w.isActive = false
        w.closedAt = LocalDateTime.now()
        windowRepo.save(w)

        return mapOf("id" to id, "closed" to true)
    }
}
