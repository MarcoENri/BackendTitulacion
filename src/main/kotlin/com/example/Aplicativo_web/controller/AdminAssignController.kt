package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AdminAssignCareerRequest
import com.example.Aplicativo_web.dto.AdminAssignStudentRequest
import com.example.Aplicativo_web.service.AdminAssignService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/assign")
class AdminAssignController(
    private val service: AdminAssignService
) {

    @PostMapping("/career")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignByCareer(@RequestBody req: AdminAssignCareerRequest): Map<String, Any?> {
        return service.assignByCareer(req)
    }

    @PostMapping("/students/{studentId}/coordinator")
    @PreAuthorize("hasRole('ADMIN')")
    fun assignStudent(
        @PathVariable studentId: Long,
        @RequestBody req: AdminAssignStudentRequest
    ): Map<String, Any?> {
        return service.assignStudent(studentId, req)
    }
}
