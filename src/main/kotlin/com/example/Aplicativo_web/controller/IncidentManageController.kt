package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.UpdateIncidentRequest
import com.example.Aplicativo_web.service.IncidentManageService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/incidents")
class IncidentManageController(
    private val service: IncidentManageService
) {

    // ✅ EDITAR incidencia (admin/coordinator/tutor)
    @PutMapping("/students/{studentId}/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','TUTOR')")
    fun update(
        auth: Authentication,
        @PathVariable studentId: Long,
        @PathVariable incidentId: Long,
        @RequestParam(required = false) periodId: Long?,
        @RequestBody req: UpdateIncidentRequest
    ): Map<String, Any?> {
        return service.updateIncident(auth, studentId, incidentId, periodId, req)
    }

    // ✅ ELIMINAR incidencia (admin/coordinator/tutor)
    @DeleteMapping("/students/{studentId}/{incidentId}")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','TUTOR')")
    fun delete(
        auth: Authentication,
        @PathVariable studentId: Long,
        @PathVariable incidentId: Long,
        @RequestParam(required = false) periodId: Long?
    ): Map<String, Any?> {
        return service.deleteIncident(auth, studentId, incidentId, periodId)
    }
}
