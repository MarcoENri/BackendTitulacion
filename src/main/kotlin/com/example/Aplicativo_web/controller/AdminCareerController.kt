package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.CareerCardDto
import com.example.Aplicativo_web.dto.CareerDto
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.CareerRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.service.AdminCareerService
import com.example.Aplicativo_web.service.storage.CareerCoverStorageService
import org.springframework.core.io.Resource
import org.springframework.http.*
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/admin/careers")
class AdminCareerController(
    private val service: AdminCareerService,
    private val careerRepo: CareerRepository,
    private val studentRepo: StudentRepository,
    private val periodRepo: AcademicPeriodRepository,
    private val coverStorage: CareerCoverStorageService
) {

    // ✅ TU ENDPOINT ORIGINAL (NO SE TOCA)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(): ResponseEntity<List<CareerDto>> {
        return ResponseEntity.ok(service.listCareers())
    }

    // ✅ NUEVO: tarjetas con conteo
    @GetMapping("/cards")
    @PreAuthorize("hasRole('ADMIN')")
    fun cards(@RequestParam(required = false) periodId: Long?): ResponseEntity<List<CareerCardDto>> {

        val pid = periodId ?: periodRepo.findFirstByIsActiveTrueOrderByStartDateDesc()
            .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay periodo activo") }
            .id!!

        val careers = careerRepo.findAll().sortedBy { it.name.lowercase() }

        val list = careers.map { c ->
            val count = studentRepo.countByCareerIdAndPeriod(c.id!!, pid)
            CareerCardDto(
                id = c.id!!,
                name = c.name,
                color = c.color,
                coverImage = c.coverImage,
                studentsCount = count
            )
        }

        return ResponseEntity.ok(list)
    }

    // ✅ NUEVO: subir portada (y opcional color)
    @PostMapping("/{id}/cover")
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadCover(
        @PathVariable id: Long,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) color: String?
    ): ResponseEntity<Void> {
        val career = careerRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Carrera no existe") }

        if (file.isEmpty) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío")

        val filename = coverStorage.saveCareerCover(id, file.bytes, file.originalFilename)
        career.coverImage = filename

        if (!color.isNullOrBlank()) career.color = color.trim()

        careerRepo.save(career)
        return ResponseEntity.ok().build()
    }

    // ✅ NUEVO: servir imagen
    @GetMapping("/cover/{filename}")
    fun cover(@PathVariable filename: String): ResponseEntity<Resource> {
        val res = coverStorage.loadAsResource(filename)

        val contentType = when {
            filename.endsWith(".png", true) -> MediaType.IMAGE_PNG
            filename.endsWith(".jpg", true) || filename.endsWith(".jpeg", true) -> MediaType.IMAGE_JPEG
            filename.endsWith(".webp", true) -> MediaType.valueOf("image/webp")
            else -> MediaType.APPLICATION_OCTET_STREAM
        }

        return ResponseEntity.ok()
            .cacheControl(CacheControl.noCache())
            .contentType(contentType)
            .body(res)
    }
}
