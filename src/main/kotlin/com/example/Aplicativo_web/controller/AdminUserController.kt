package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AssignCareersRequest
import com.example.Aplicativo_web.dto.CreateUserRequest
import com.example.Aplicativo_web.dto.UserResponse
import com.example.Aplicativo_web.service.AdminUserService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/users")
class AdminUserController(
    private val adminUserService: AdminUserService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody req: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(adminUserService.createUser(req))
    }

    // ✅ NUEVO: listar usuarios (opcionalmente filtrados por rol)
    // Ej: /admin/users?role=ROLE_TUTOR
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun listUsers(@RequestParam(required = false) role: String?): ResponseEntity<List<UserResponse>> {
        return ResponseEntity.ok(adminUserService.listUsers(role))
    }

    @PostMapping("/{id}/careers")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignCareers(
        @PathVariable id: Long,
        @RequestBody req: AssignCareersRequest
    ): ResponseEntity<Void> {
        adminUserService.assignCareers(id, req)
        return ResponseEntity.ok().build()
    }
    @GetMapping("/juries")
    @PreAuthorize("hasRole('ADMIN')")
    fun listJuries(): ResponseEntity<List<UserResponse>> =
        ResponseEntity.ok(adminUserService.listJuries())
}
