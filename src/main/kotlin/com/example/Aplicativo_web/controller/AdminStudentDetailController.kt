package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.StudentDetailDto
import com.example.Aplicativo_web.service.AdminStudentDetailService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/students")
class AdminStudentDetailController(
    private val detailService: AdminStudentDetailService
) {
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun detail(@PathVariable id: Long): ResponseEntity<StudentDetailDto> =
        ResponseEntity.ok(detailService.getDetail(id))
}
