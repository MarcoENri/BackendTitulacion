package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.ImportBatchResponse
import com.example.Aplicativo_web.service.StudentImportService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin/students/import")
class AdminStudentImportController(
    private val importService: StudentImportService
) {

    @PostMapping("/xlsx")
    @PreAuthorize("hasRole('ADMIN')")
    fun importXlsx(@RequestParam("file") file: MultipartFile): ResponseEntity<ImportBatchResponse> {
        // Si luego quieres el uploaded_by real, lo sacamos del username -> app_user.id
        // Por ahora lo dejamos null o lo resolvemos después.
        val res = importService.importXlsx(file, uploadedByUserId = null)
        return ResponseEntity.ok(res)
    }
}
