package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AdminAssignStudentRequest
import com.example.Aplicativo_web.service.AdminAssignStudentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/students")
class AdminAssignStudentController(
    private val service: AdminAssignStudentService
) {

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    fun assign(
        @PathVariable id: Long,
        @RequestBody req: AdminAssignStudentRequest
    ): ResponseEntity<Void> {
        service.assign(id, req)
        return ResponseEntity.ok().build()
    }
}
