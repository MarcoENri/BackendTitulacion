package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AssignProjectRequest
import com.example.Aplicativo_web.dto.CreateIncidentRequest
import com.example.Aplicativo_web.dto.CreateObservationRequest
import com.example.Aplicativo_web.dto.StudentDetailDto
import com.example.Aplicativo_web.service.CoordinatorStudentsService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/coordinator/students")
class CoordinatorStudentsController(
    private val service: CoordinatorStudentsService
) {

    @GetMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    fun list(auth: Authentication): ResponseEntity<Any> {
        return ResponseEntity.ok(service.listMyStudents(auth.name))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun detail(auth: Authentication, @PathVariable id: Long): ResponseEntity<StudentDetailDto> {
        return ResponseEntity.ok(service.getDetail(auth.name, id))
    }

    @PutMapping("/{id}/project")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun assignProject(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: AssignProjectRequest
    ): ResponseEntity<Void> {
        service.assignProject(auth.name, id, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/incidents")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun createIncident(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateIncidentRequest
    ): ResponseEntity<Void> {
        service.createIncident(auth.name, id, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/observations")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun createObservation(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateObservationRequest
    ): ResponseEntity<Void> {
        service.createObservation(auth.name, id, req)
        return ResponseEntity.ok().build()
    }
}
