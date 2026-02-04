package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.finaldefense.*
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseWindowRepository
import com.example.Aplicativo_web.service.FinalDefenseAdminService
import com.example.Aplicativo_web.service.finaldefense.FinalDefenseRubricStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/admin/final-defense")
class FinalDefenseAdminController(
    private val service: FinalDefenseAdminService,
    private val windowRepo: FinalDefenseWindowRepository,
    private val rubricStorage: FinalDefenseRubricStorageService
) {

    @PostMapping("/windows")
    @PreAuthorize("hasRole('ADMIN')")
    fun createWindow(@RequestBody req: CreateFinalDefenseWindowRequest) =
        ResponseEntity.ok(service.createWindow(req))

    @GetMapping("/windows")
    @PreAuthorize("hasRole('ADMIN')")
    fun listWindows(@RequestParam(required = false) periodId: Long?) =
        ResponseEntity.ok(service.listWindows(periodId))

    @PostMapping("/windows/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    fun close(@PathVariable id: Long): ResponseEntity<Void> {
        service.closeWindow(id)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/windows/{windowId}/slots")
    @PreAuthorize("hasRole('ADMIN')")
    fun createSlot(
        @PathVariable windowId: Long,
        @RequestBody req: CreateFinalDefenseSlotRequest
    ) = ResponseEntity.ok(service.createSlot(windowId, req))

    @GetMapping("/windows/{windowId}/slots")
    @PreAuthorize("hasRole('ADMIN')")
    fun listSlots(@PathVariable windowId: Long) =
        ResponseEntity.ok(service.listSlots(windowId))

    @GetMapping("/careers/{careerId}/students")
    @PreAuthorize("hasRole('ADMIN')")
    fun listStudents(
        @PathVariable careerId: Long,
        @RequestParam(required = false) periodId: Long?
    ) = ResponseEntity.ok(service.listStudentsForCareer(careerId, periodId))

    @PostMapping("/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    fun createBooking(@RequestBody req: CreateFinalDefenseBookingRequest) =
        ResponseEntity.ok(service.createBooking(req))

    // ✅ Subir rúbrica PDF
    @PostMapping("/windows/{windowId}/rubric")
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadRubric(
        @PathVariable windowId: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<Void> {

        if (file.isEmpty) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Archivo vacío")
        }

        val isPdf =
            file.contentType?.contains("pdf", true) == true ||
                    file.originalFilename?.endsWith(".pdf", true) == true

        if (!isPdf) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permite PDF")
        }

        val window = windowRepo.findById(windowId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana no existe") }

        val savedPath = rubricStorage.saveRubricPdf(
            windowId,
            file.bytes,
            file.originalFilename
        )

        window.rubricPath = savedPath
        windowRepo.save(window)

        return ResponseEntity.ok().build()
    }
}
