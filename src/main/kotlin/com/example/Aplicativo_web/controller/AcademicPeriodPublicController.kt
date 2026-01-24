package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AcademicPeriodDto
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/periods")
class AcademicPeriodPublicController(
    private val repo: AcademicPeriodRepository
) {

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','TUTOR')")
    fun active(): AcademicPeriodDto {
        val p = repo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "No hay periodo activo") }

        return AcademicPeriodDto(
            id = p.id!!,
            name = p.name,
            startDate = p.startDate,
            endDate = p.endDate,
            isActive = p.isActive
        )
    }
}
