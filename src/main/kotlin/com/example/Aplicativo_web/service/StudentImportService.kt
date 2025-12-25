package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.ImportBatchResponse
import com.example.Aplicativo_web.entity.StudentImportBatchEntity
import com.example.Aplicativo_web.entity.StudentImportRowEntity
import com.example.Aplicativo_web.entity.StudentEntity
import com.example.Aplicativo_web.repository.*
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class StudentImportService(
    private val batchRepo: StudentImportBatchRepository,
    private val rowRepo: StudentImportRowRepository,
    private val studentRepo: StudentRepository,
    private val careerRepo: CareerRepository
) {

    /**
     * Formato esperado (fila 1 como headers):
     * dni | first_name | last_name | email | corte | section | modality | career_name | titulation_type
     */
    @Transactional
    fun importXlsx(file: MultipartFile, uploadedByUserId: Long?): ImportBatchResponse {
        val batch = batchRepo.save(
            StudentImportBatchEntity(
                uploadedBy = uploadedByUserId,
                fileName = file.originalFilename ?: "students.xlsx",
                fileType = "XLSX",
                status = "PROCESSING",
                createdAt = LocalDateTime.now()
            )
        )

        val formatter = DataFormatter()
        var total = 0
        var inserted = 0
        var updated = 0
        var failed = 0

        XSSFWorkbook(file.inputStream).use { wb ->
            val sheet = wb.getSheetAt(0)
            val lastRow = sheet.lastRowNum

            // empieza desde 1 (asumiendo row 0 = encabezados)
            for (i in 1..lastRow) {
                val r = sheet.getRow(i) ?: continue
                total++

                fun cell(col: Int): String =
                    formatter.formatCellValue(r.getCell(col)).trim()

                val dni = cell(0)
                val firstName = cell(1)
                val lastName = cell(2)
                val email = cell(3)
                val corte = cell(4)
                val section = cell(5)
                val modality = cell(6).ifBlank { null }
                val careerName = cell(7)
                val titulationType = cell(8).ifBlank { "EXAMEN" }

                val rowLog = StudentImportRowEntity(
                    batchId = batch.id!!,
                    rowNumber = i + 1,
                    dni = dni,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    corte = corte,
                    section = section,
                    modality = modality,
                    careerName = careerName,
                    createdAt = LocalDateTime.now()
                )

                try {
                    // Validaciones mínimas
                    if (dni.isBlank() || firstName.isBlank() || lastName.isBlank() ||
                        email.isBlank() || corte.isBlank() || section.isBlank() || careerName.isBlank()
                    ) {
                        throw IllegalArgumentException("Faltan campos obligatorios (dni, nombres, email, corte, section, career_name)")
                    }

                    val career = careerRepo.findByName(careerName)
                        .orElseGet { careerRepo.save(com.example.Aplicativo_web.entity.CareerEntity(name = careerName)) }

                    val existing = studentRepo.findByDni(dni).orElse(null)

                    if (existing == null) {
                        val s = StudentEntity(
                            dni = dni,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            corte = corte,
                            section = section,
                            modality = modality,
                            titulationType = titulationType,
                            status = "EN_CURSO",
                            career = career,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )
                        studentRepo.save(s)
                        inserted++
                    } else {
                        existing.firstName = firstName
                        existing.lastName = lastName
                        existing.email = email
                        existing.corte = corte
                        existing.section = section
                        existing.modality = modality
                        existing.titulationType = titulationType
                        existing.career = career
                        existing.updatedAt = LocalDateTime.now()
                        studentRepo.save(existing)
                        updated++
                    }

                    rowLog.status = "OK"
                    rowRepo.save(rowLog)

                } catch (ex: Exception) {
                    failed++
                    rowLog.status = "ERROR"
                    rowLog.errorMessage = ex.message
                    rowRepo.save(rowLog)
                }
            }
        }

        batch.totalRows = total
        batch.insertedRows = inserted
        batch.updatedRows = updated
        batch.failedRows = failed
        batch.status = if (failed > 0) "COMPLETED" else "COMPLETED"
        batchRepo.save(batch)

        return ImportBatchResponse(
            batchId = batch.id!!,
            status = batch.status,
            fileName = batch.fileName,
            totalRows = total,
            insertedRows = inserted,
            updatedRows = updated,
            failedRows = failed
        )
    }
}
