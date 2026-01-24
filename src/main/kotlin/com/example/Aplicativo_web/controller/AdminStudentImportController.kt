package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.ImportBatchResponse
import com.example.Aplicativo_web.service.StudentImportService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/admin/students/import")
class AdminStudentImportController(
    private val importService: StudentImportService
) {

    @PostMapping("/xlsx")
    @PreAuthorize("hasRole('ADMIN')")
    fun importXlsx(
        auth: Authentication,
        @RequestParam("file") file: MultipartFile,
        @RequestParam("academicPeriodId", required = false) academicPeriodId: Long?
    ): ResponseEntity<ImportBatchResponse> {

        val uploadedByUserId: Long? = null

        val res = importService.importXlsx(
            file = file,
            uploadedByUserId = uploadedByUserId,
            academicPeriodId = academicPeriodId
        )

        return ResponseEntity.ok(res)
    }

}
