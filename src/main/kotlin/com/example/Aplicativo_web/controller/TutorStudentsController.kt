package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.CreateIncidentRequest
import com.example.Aplicativo_web.dto.CreateObservationRequest
import com.example.Aplicativo_web.dto.StudentDetailDto
import com.example.Aplicativo_web.service.TutorStudentsService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/tutor/students")
class TutorStudentsController(
    private val service: TutorStudentsService
) {

    @GetMapping
    @PreAuthorize("hasRole('TUTOR')")
    fun list(auth: Authentication): ResponseEntity<Any> {
        return ResponseEntity.ok(service.listMyStudents(auth.name))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    fun detail(auth: Authentication, @PathVariable id: Long): ResponseEntity<StudentDetailDto> {
        return ResponseEntity.ok(service.getDetail(auth.name, id))
    }

    @PostMapping("/{id}/incidents")
    @PreAuthorize("hasRole('TUTOR')")
    fun createIncident(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateIncidentRequest
    ): ResponseEntity<Void> {
        service.createIncident(auth.name, id, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/observations")
    @PreAuthorize("hasRole('TUTOR')")
    fun createObservation(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateObservationRequest
    ): ResponseEntity<Void> {
        service.createObservation(auth.name, id, req)
        return ResponseEntity.ok().build()
    }
}
