package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.finaldefense.CreateFinalDefenseEvaluationRequest
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseEvaluationDto
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingRepository
import com.example.Aplicativo_web.service.FinalDefenseActaPdfService
import com.example.Aplicativo_web.service.finaldefense.FinalDefenseActaStorageService
import com.example.Aplicativo_web.service.finaldefense.FinalDefenseRubricStorageService
import com.example.Aplicativo_web.service.finaldefense.JuryFinalDefenseService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files

@RestController
@RequestMapping("/jury/final-defense")
class FinalDefenseJuryController(
    private val service: JuryFinalDefenseService,
    private val pdfService: FinalDefenseActaPdfService,
    private val bookingRepo: FinalDefenseBookingRepository,
    private val rubricStorage: FinalDefenseRubricStorageService,
    private val actaStorage: FinalDefenseActaStorageService
) {

    @GetMapping("/bookings")
    @PreAuthorize("hasRole('JURY')")
    fun myBookings(auth: Authentication) = ResponseEntity.ok(service.myBookings(auth.name))

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasRole('JURY')")
    fun detail(auth: Authentication, @PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val dto = service.bookingDetail(id, auth.name)
        val evs = service.listEvaluations(id, auth.name)
        return ResponseEntity.ok(mapOf("booking" to dto, "evaluations" to evs))
    }

    @PostMapping("/bookings/{id}/evaluate")
    @PreAuthorize("hasRole('JURY')")
    fun evaluate(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateFinalDefenseEvaluationRequest
    ): ResponseEntity<FinalDefenseEvaluationDto> =
        ResponseEntity.ok(service.evaluate(id, req, auth.name))

    // ✅ ACTA PDF: si ya está guardada en disco, usarla; si no, generar en caliente
    @GetMapping("/bookings/{id}/acta.pdf")
    @PreAuthorize("hasRole('JURY')")
    fun acta(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val booking = bookingRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val actaFile = booking.actaPath?.trim()

        val pdfBytes: ByteArray = if (!actaFile.isNullOrBlank()) {
            val p = actaStorage.resolvePath(actaFile)
            if (!Files.exists(p)) {
                // si no existe en disco, regeneramos
                pdfService.buildActaPdf(id)
            } else {
                Files.readAllBytes(p)
            }
        } else {
            pdfService.buildActaPdf(id)
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=acta_final_defense_$id.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes)
    }

    // ✅ RÚBRICA PDF
    @GetMapping("/bookings/{id}/rubric.pdf")
    @PreAuthorize("hasRole('JURY')")
    fun rubric(@PathVariable id: Long): ResponseEntity<Resource> {
        val booking = bookingRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val slot = booking.slot ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin slot")
        val window = slot.window ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot sin ventana")

        val stored = window.rubricPath?.trim()
        if (stored.isNullOrBlank()) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Esta ventana aún no tiene rúbrica subida")
        }

        val resource = rubricStorage.loadAsResource(stored)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=rubrica_window_${window.id}.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(resource)
    }
}
