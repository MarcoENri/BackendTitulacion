package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AdminAssignCareerRequest
import com.example.Aplicativo_web.service.AdminAssignCareerService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/careers")
class AdminAssignCareerController(
    private val service: AdminAssignCareerService
) {

    @PutMapping("/{careerId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignByCareer(
        @PathVariable careerId: Long,
        @RequestBody req: AdminAssignCareerRequest
    ): ResponseEntity<Void> {
        service.assignByCareer(careerId, req)
        return ResponseEntity.ok().build()
    }
}
