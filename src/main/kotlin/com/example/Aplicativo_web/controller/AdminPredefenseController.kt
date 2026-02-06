package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.CreatePredefenseWindowRequest
import com.example.Aplicativo_web.dto.PredefenseWindowDto
import com.example.Aplicativo_web.service.AdminPredefenseService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/predefense")
class AdminPredefenseController(
    private val service: AdminPredefenseService
) {

    @PostMapping("/windows")
    @PreAuthorize("hasRole('ADMIN')")
    fun createWindow(
        @RequestBody req: CreatePredefenseWindowRequest
    ): ResponseEntity<PredefenseWindowDto> {
        return ResponseEntity.ok(service.createWindow(req))
    }

    @GetMapping("/windows")
    @PreAuthorize("hasRole('ADMIN')")
    fun listWindows(
        @RequestParam(required = false) periodId: Long?
    ): ResponseEntity<List<PredefenseWindowDto>> {
        return ResponseEntity.ok(service.listWindows(periodId))
    }

    @PostMapping("/windows/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    fun closeWindow(
        @PathVariable id: Long
    ): ResponseEntity<Map<String, Any?>> {
        return ResponseEntity.ok(service.closeWindow(id))
    }
}
