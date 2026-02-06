package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.*
import com.example.Aplicativo_web.service.JuryPredefenseService
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/jury/predefense")
class JuryPredefenseController(
    private val service: JuryPredefenseService
) {

    // --- Todos los roles pueden ver ---
    @GetMapping("/careers/{careerId}/students")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR','TUTOR')")
    fun listStudentsByCareer(
        @PathVariable careerId: Long,
        @RequestParam(required = false) periodId: Long?
    ): ResponseEntity<List<JuryCareerStudentsDto>> {
        return ResponseEntity.ok(service.listStudentsByCareer(careerId, periodId))
    }

    @GetMapping("/careers/{careerId}/windows")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR','TUTOR')")
    fun listActiveWindows(
        @PathVariable careerId: Long,
        @RequestParam(required = false) periodId: Long?
    ): ResponseEntity<List<PredefenseWindowDto>> {
        return ResponseEntity.ok(service.listActiveWindowsForCareer(careerId, periodId))
    }

    @GetMapping("/windows/{windowId}/slots")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR','TUTOR')")
    fun listSlots(@PathVariable windowId: Long): ResponseEntity<List<PredefenseSlotDto>> {
        return ResponseEntity.ok(service.listSlots(windowId))
    }

    // --- Solo Jurado y Coordinador pueden crear slots ---
    @PostMapping("/windows/{windowId}/slots")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR')")
    fun createSlot(
        @PathVariable windowId: Long,
        @RequestParam startsAt: LocalDateTime,
        @RequestParam endsAt: LocalDateTime
    ): ResponseEntity<PredefenseSlotDto> {
        return ResponseEntity.ok(service.createSlot(windowId, startsAt, endsAt))
    }

    // --- Solo Jurado y Coordinador pueden reservar ---
    @PostMapping("/bookings")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR')")
    fun bookSlot(
        auth: Authentication,
        @RequestBody req: CreatePredefenseBookingRequest
    ): ResponseEntity<PredefenseBookingDto> {
        return ResponseEntity.ok(service.bookSlot(req, auth.name))
    }

    // --- Todos los roles pueden ver observaciones ---
    @GetMapping("/bookings/{bookingId}/observations")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR','TUTOR')")
    fun listObservations(@PathVariable bookingId: Long): ResponseEntity<List<PredefenseObservationDto>> {
        return ResponseEntity.ok(service.listObservations(bookingId))
    }

    // --- Todos los roles pueden enviar observaciones ---
    @PostMapping("/bookings/{bookingId}/observations")
    @PreAuthorize("hasAnyRole('JURY','COORDINATOR','TUTOR')")
    fun createObservation(
        auth: Authentication,
        @PathVariable bookingId: Long,
        @RequestBody req: CreatePredefenseObservationRequest
    ): ResponseEntity<PredefenseObservationDto> {
        return ResponseEntity.ok(service.createObservation(bookingId, req, auth.name))
    }
}




