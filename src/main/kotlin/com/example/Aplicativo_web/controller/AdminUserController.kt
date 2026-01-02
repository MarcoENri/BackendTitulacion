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

    // ✅ Crear usuario con roles
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody req: CreateUserRequest): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(adminUserService.createUser(req))
    }

    // ✅ Asignar carreras (user_career)
    @PostMapping("/{id}/careers")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignCareers(
        @PathVariable id: Long,
        @RequestBody req: AssignCareersRequest
    ): ResponseEntity<Void> {
        adminUserService.assignCareers(id, req)
        return ResponseEntity.ok().build()
    }
}
