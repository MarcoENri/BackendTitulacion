package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.CareerDto
import com.example.Aplicativo_web.service.AdminCareerService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/careers")
class AdminCareerController(
    private val service: AdminCareerService
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(): ResponseEntity<List<CareerDto>> {
        return ResponseEntity.ok(service.listCareers())
    }
}
