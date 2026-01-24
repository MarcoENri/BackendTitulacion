package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.ImportBatchResponse
import com.example.Aplicativo_web.entity.CareerEntity
import com.example.Aplicativo_web.entity.StudentEntity
import com.example.Aplicativo_web.entity.StudentImportBatchEntity
import com.example.Aplicativo_web.entity.StudentImportRowEntity
import com.example.Aplicativo_web.entity.enums.StudentStatus
import com.example.Aplicativo_web.repository.*
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException
import java.text.Normalizer
import java.time.LocalDateTime

@Service
class StudentImportService(
    private val batchRepo: StudentImportBatchRepository,
    private val rowRepo: StudentImportRowRepository,
    private val studentRepo: StudentRepository,
    private val careerRepo: CareerRepository,
    private val academicPeriodRepo: AcademicPeriodRepository
) {

    // 1. Limpia espacios, mantiene mayúsculas/tildes para visualización
    // Ej: "  INGENIERÍA   CIVIL " -> "INGENIERÍA CIVIL"
    private fun displayCareerName(raw: String): String =
        raw.trim().replace(Regex("\\s+"), " ")

    // 2. Normaliza para búsquedas únicas (quita acentos y pasa a minúsculas)
    // Ej: "INGENIERÍA CIVIL" -> "ingenieria civil"
    private fun normalizeCareerName(raw: String): String {
        val trimmed = raw.trim().replace(Regex("\\s+"), " ")
        val noAccents = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return noAccents.lowercase()
    }

    /**
     * Formato esperado (fila 1 como headers):
     * dni | first_name | last_name | email | corte | section | modality | career_name | titulation_type
     */
    @Transactional
    fun importXlsx(
        file: MultipartFile,
        uploadedByUserId: Long?,
        academicPeriodId: Long?
    ): ImportBatchResponse {

        // --- 1. Resolver el periodo académico ---
        val periodIdToUse = academicPeriodId ?: academicPeriodRepo
            .findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow {
                ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No hay un periodo académico ACTIVO. Activa uno antes de importar."
                )
            }
            .id!!

        val period = academicPeriodRepo.findById(periodIdToUse)
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Corte académico no existe") }

        // --- 2. Crear el Batch ---
        val batch = batchRepo.save(
            StudentImportBatchEntity(
                uploadedBy = uploadedByUserId,
                fileName = file.originalFilename ?: "students.xlsx",
                fileType = "XLSX",
                status = "PROCESSING",
                createdAt = LocalDateTime.now()
            )
        )

        // --- CACHE LOCAL Y LÓGICA DE CARRERAS ---
        // Cache Map: Key = Normalized Name (ej: "medicina"), Value = Entity
        val careerCache = mutableMapOf<String, CareerEntity>()

        fun getOrCreateCareer(rawName: String): CareerEntity {
            // Paso A: Obtener nombre "bonito"
            val display = displayCareerName(rawName)
            if (display.isBlank()) throw IllegalArgumentException("El campo career_name está vacío")

            // Paso B: Obtener nombre normalizado (llave única lógica)
            val norm = normalizeCareerName(display)

            // Paso C: Buscar en cache, DB o crear
            return careerCache.getOrPut(norm) {
                // 1) Buscar por normalizedName en DB (definitivo)
                // NOTA: Asegúrate de tener findByNormalizedName en tu CareerRepository
                careerRepo.findByNormalizedName(norm)
                    ?: try {
                        // 2) Crear (si hay carrera creada concurrentemente, el UNIQUE lo evita)
                        careerRepo.save(
                            CareerEntity(
                                name = display,          // Nombre bonito: "Ingeniería Civil"
                                normalizedName = norm    // Nombre búsqueda: "ingenieria civil"
                            )
                        )
                    } catch (e: DataIntegrityViolationException) {
                        // 3) Si ya se creó en otra transacción (race condition), re-consulta
                        careerRepo.findByNormalizedName(norm)
                            ?: throw e // Si falla aquí, es otro error de integridad
                    }
            }
        }

        val formatter = DataFormatter()
        var total = 0
        var inserted = 0
        var updated = 0
        var failed = 0

        // --- 3. Leer Excel ---
        XSSFWorkbook(file.inputStream).use { wb ->
            val sheet = wb.getSheetAt(0)
            val lastRow = sheet.lastRowNum

            for (i in 1..lastRow) { // row 0 = encabezados
                val r = sheet.getRow(i) ?: continue
                total++

                fun cell(col: Int): String =
                    formatter.formatCellValue(r.getCell(col)).trim()

                val dni = cell(0)
                val firstName = cell(1)
                val lastName = cell(2)
                val email = cell(3)
                val corteFromExcel = cell(4)
                val section = cell(5)
                val modality = cell(6).ifBlank { null }
                val careerNameRaw = cell(7)
                val titulationType = cell(8).ifBlank { "EXAMEN" }

                val rowLog = StudentImportRowEntity(
                    batchId = batch.id!!,
                    rowNumber = i + 1,
                    dni = dni,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    corte = corteFromExcel,
                    section = section,
                    modality = modality,
                    careerName = careerNameRaw,
                    createdAt = LocalDateTime.now()
                )

                try {
                    // Validaciones básicas
                    if (dni.isBlank() || firstName.isBlank() || lastName.isBlank()
                        || email.isBlank() || section.isBlank() || careerNameRaw.isBlank()
                    ) {
                        throw IllegalArgumentException(
                            "Faltan campos obligatorios (dni, first_name, last_name, email, section, career_name)"
                        )
                    }

                    if (!email.contains("@") || !email.contains(".")) {
                        throw IllegalArgumentException("Email inválido: $email")
                    }

                    // ✅ Obtener o crear carrera (usando la nueva lógica robusta)
                    val career = getOrCreateCareer(careerNameRaw)

                    // Buscar estudiante existente en ESTE periodo
                    val existing = studentRepo.findByDniAndAcademicPeriod_Id(dni, period.id!!).orElse(null)

                    if (existing == null) {
                        val s = StudentEntity(
                            dni = dni,
                            firstName = firstName,
                            lastName = lastName,
                            email = email,
                            corte = corteFromExcel,
                            section = section,
                            modality = modality,
                            titulationType = titulationType,
                            status = StudentStatus.EN_CURSO,
                            career = career,
                            academicPeriod = period,
                            createdAt = LocalDateTime.now(),
                            updatedAt = LocalDateTime.now()
                        )
                        studentRepo.save(s)
                        inserted++
                    } else {
                        // Actualizar existente
                        existing.firstName = firstName
                        existing.lastName = lastName
                        existing.email = email
                        existing.corte = corteFromExcel
                        existing.section = section
                        existing.modality = modality
                        existing.titulationType = titulationType
                        existing.career = career // Actualizamos carrera normalizada
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

        // --- 4. Finalizar Batch ---
        batch.totalRows = total
        batch.insertedRows = inserted
        batch.updatedRows = updated
        batch.failedRows = failed
        batch.status = "COMPLETED"
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