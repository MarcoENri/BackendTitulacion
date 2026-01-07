package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.SendStudentEmailRequest
import com.example.Aplicativo_web.dto.SendStudentEmailResponse
import com.example.Aplicativo_web.service.StudentEmailService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/students")
class StudentEmailController(
    private val emailService: StudentEmailService
) {

    @PostMapping("/{id}/email")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','TUTOR')")
    fun sendEmail(
        auth: Authentication,
        @PathVariable("id") studentId: Long,
        @RequestBody req: SendStudentEmailRequest
    ): ResponseEntity<SendStudentEmailResponse> {
        emailService.sendToStudent(
            studentId = studentId,
            subject = req.subject,
            body = req.body,
            senderUsername = auth.name
        )
        return ResponseEntity.ok(SendStudentEmailResponse(status = "SENT"))
    }
}
