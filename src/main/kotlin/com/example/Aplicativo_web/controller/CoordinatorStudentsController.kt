package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.service.CoordinatorStudentsService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/coordinator/students")
class CoordinatorStudentsController(
    private val service: CoordinatorStudentsService,
    private val academicPeriodRepository: AcademicPeriodRepository
) {

    @GetMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    fun list(
        auth: Authentication,
        @RequestParam(required = false) periodId: Long?
    ): ResponseEntity<Any> {

        val pid = periodId
            ?: academicPeriodRepository
                .findFirstByIsActiveTrueOrderByStartDateDesc()
                .orElseThrow {
                    ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No hay período académico activo"
                    )
                }
                .id!!

        return ResponseEntity.ok(
            service.listMyStudents(auth.name, pid)
        )
    }



    @GetMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun detail(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestParam("periodId") periodId: Long
    ): ResponseEntity<StudentDetailDto> {
        return ResponseEntity.ok(service.getDetail(auth.name, id, periodId))
    }

    @PostMapping("/{studentId}/assign")
    @PreAuthorize("hasRole('COORDINATOR')")
    fun assignProject(
        auth: Authentication,
        @PathVariable studentId: Long,
        @RequestParam periodId: Long,
        @RequestBody req: AssignProjectRequest
    ): ResponseEntity<Map<String, Any?>> {

        service.assignProject(auth.name, studentId, periodId, req)

        return ResponseEntity.ok(
            mapOf(
                "studentId" to studentId,
                "periodId" to periodId,
                "tutorId" to req.tutorId,
                "projectName" to req.projectName
            )
        )
    }

    @PostMapping("/{id}/incidents")
    @PreAuthorize("hasRole('COORDINATOR')")
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
    @PreAuthorize("hasRole('COORDINATOR')")
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
    @PreAuthorize("hasRole('COORDINATOR')")
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
    @PreAuthorize("hasRole('COORDINATOR')")
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
    @PreAuthorize("hasRole('COORDINATOR')")
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

