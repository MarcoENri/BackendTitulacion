package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.*
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
    fun list(
        auth: Authentication,
        @RequestParam("periodId") periodId: Long
    ): ResponseEntity<Any> {
        return ResponseEntity.ok(service.listMyStudents(auth.name, periodId))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TUTOR')")
    fun detail(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestParam("periodId") periodId: Long
    ): ResponseEntity<StudentDetailDto> {
        return ResponseEntity.ok(service.getDetail(auth.name, id, periodId))
    }

    @PostMapping("/{id}/incidents")
    @PreAuthorize("hasRole('TUTOR')")
    fun createIncident(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestParam("periodId") periodId: Long,
        @RequestBody req: CreateIncidentRequest
    ): ResponseEntity<Void> {
        service.createIncident(auth.name, id, periodId, req)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/{id}/observations")
    @PreAuthorize("hasRole('TUTOR')")
    fun createObservation(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestParam("periodId") periodId: Long,
        @RequestBody req: CreateObservationRequest
    ): ResponseEntity<Void> {
        service.createObservation(auth.name, id, periodId, req)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{studentId}/incidents/{incidentId}")
    @PreAuthorize("hasRole('TUTOR')")
    fun updateIncident(
        auth: Authentication,
        @PathVariable studentId: Long,
        @PathVariable incidentId: Long,
        @RequestParam("periodId") periodId: Long,
        @RequestBody req: UpdateIncidentRequest
    ): ResponseEntity<Void> {
        service.updateIncident(auth.name, studentId, incidentId, periodId, req)
        return ResponseEntity.ok().build()
    }

    @PutMapping("/{studentId}/observations/{observationId}")
    @PreAuthorize("hasRole('TUTOR')")
    fun updateObservation(
        auth: Authentication,
        @PathVariable studentId: Long,
        @PathVariable observationId: Long,
        @RequestParam("periodId") periodId: Long,
        @RequestBody req: UpdateObservationRequest
    ): ResponseEntity<Void> {
        service.updateObservation(auth.name, studentId, observationId, periodId, req)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{studentId}/incidents/{incidentId}")
    @PreAuthorize("hasRole('TUTOR')")
    fun deleteIncident(
        auth: Authentication,
        @PathVariable studentId: Long,
        @PathVariable incidentId: Long,
        @RequestParam("periodId") periodId: Long
    ): ResponseEntity<Void> {
        service.deleteIncident(auth.name, studentId, incidentId, periodId)
        return ResponseEntity.noContent().build()
    }
}
