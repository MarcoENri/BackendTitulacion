package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AdminStudentRow
import com.example.Aplicativo_web.service.AdminStudentService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/students")
class AdminStudentsController(
    private val adminStudentService: AdminStudentService
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(): ResponseEntity<List<AdminStudentRow>> =
        ResponseEntity.ok(adminStudentService.listStudents())
}
