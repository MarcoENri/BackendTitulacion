package com.example.Aplicativo_web.controller

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
}
