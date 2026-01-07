package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.UserResponse
import com.example.Aplicativo_web.service.CoordinatorLookupService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/coordinator")
class CoordinatorLookupController(
    private val service: CoordinatorLookupService
) {

    @GetMapping("/tutors")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun listTutors(): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(service.listTutors())
    }
}
