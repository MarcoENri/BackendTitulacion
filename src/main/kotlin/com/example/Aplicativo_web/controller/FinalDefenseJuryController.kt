package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.finaldefense.CreateFinalDefenseEvaluationRequest
import com.example.Aplicativo_web.dto.finaldefense.FinalDefenseEvaluationDto
import com.example.Aplicativo_web.repository.finaldefense.FinalDefenseBookingRepository
import com.example.Aplicativo_web.service.FinalDefenseActaPdfService
import com.example.Aplicativo_web.service.finaldefense.FinalDefenseActaStorageService
import com.example.Aplicativo_web.service.finaldefense.FinalDefenseRubricStorageService
import com.example.Aplicativo_web.service.finaldefense.JuryFinalDefenseService
import org.springframework.core.io.Resource
import org.springframework.http.*
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
    @PreAuthorize("hasAnyRole('JURY','TUTOR','DOCENTE','COORDINATOR')")
    fun myBookings(auth: Authentication) =
        ResponseEntity.ok(service.myBookings(auth.name))

    @GetMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('JURY','TUTOR','DOCENTE','COORDINATOR')")
    fun detail(auth: Authentication, @PathVariable id: Long): ResponseEntity<Map<String, Any>> {
        val booking = service.bookingDetail(id, auth.name)
        val evaluations = service.listEvaluations(id, auth.name)
        return ResponseEntity.ok(
            mapOf(
                "booking" to booking,
                "evaluations" to evaluations
            )
        )
    }

    @PostMapping("/bookings/{id}/evaluate")
    @PreAuthorize("hasAnyRole('JURY','TUTOR','DOCENTE','COORDINATOR')")
    fun evaluate(
        auth: Authentication,
        @PathVariable id: Long,
        @RequestBody req: CreateFinalDefenseEvaluationRequest
    ): ResponseEntity<FinalDefenseEvaluationDto> =
        ResponseEntity.ok(service.evaluate(id, req, auth.name))

    // ✅ ACTA PDF
    @GetMapping("/bookings/{id}/acta.pdf")
    @PreAuthorize("hasAnyRole('JURY','TUTOR','DOCENTE','COORDINATOR')")
    fun acta(@PathVariable id: Long): ResponseEntity<ByteArray> {

        val booking = bookingRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val bytes = booking.actaPath?.let {
            val path = actaStorage.resolvePath(it)
            if (Files.exists(path)) Files.readAllBytes(path)
            else pdfService.buildActaPdf(id)
        } ?: pdfService.buildActaPdf(id)

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=acta_final_$id.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(bytes)
    }

    // ✅ RÚBRICA PDF
    @GetMapping("/bookings/{id}/rubric.pdf")
    @PreAuthorize("hasAnyRole('JURY','TUTOR','DOCENTE','COORDINATOR')")
    fun rubric(@PathVariable id: Long): ResponseEntity<Resource> {

        val booking = bookingRepo.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val window = booking.slot?.window
            ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin ventana")

        val path = window.rubricPath
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Ventana sin rúbrica")

        val resource = rubricStorage.loadAsResource(path)

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=rubrica_window_${window.id}.pdf"
            )
            .body(resource)
    }
}
