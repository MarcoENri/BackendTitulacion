package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.dto.AcademicPeriodDto
import com.example.Aplicativo_web.dto.CreateAcademicPeriodRequest
import com.example.Aplicativo_web.repository.AcademicPeriodRepository
import com.example.Aplicativo_web.repository.StudentRepository
import com.example.Aplicativo_web.service.AcademicPeriodService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/admin/academic-periods")
class AcademicPeriodController(
    private val repo: AcademicPeriodRepository,
    private val service: AcademicPeriodService,
    private val studentRepo: StudentRepository
) {

    // ✅ LISTA (para select del frontend)
    // GET /admin/academic-periods?onlyActive=true
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(@RequestParam(required = false) onlyActive: Boolean?): List<AcademicPeriodDto> {
        val periods = if (onlyActive == true) {
            repo.findByIsActiveTrue()
        } else {
            repo.findAll()
        }

        return periods
            .sortedByDescending { it.startDate }
            .map { it.toDto() }
    }

    // ✅ CREAR PERIODO
    // POST /admin/academic-periods
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody req: CreateAcademicPeriodRequest): ResponseEntity<AcademicPeriodDto> {
        val saved = service.create(
            start = req.startDate,
            end = req.endDate,
            isActive = req.isActive
        )
        return ResponseEntity.ok(saved.toDto())
    }

    // ✅ ACTIVAR PERIODO (admin decide cuál es el actual)
    // POST /admin/academic-periods/{periodId}/activate
    @PostMapping("/{periodId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    fun activate(@PathVariable periodId: Long): ResponseEntity<Void> {
        // valida que exista
        val exists = repo.existsById(periodId)
        if (!exists) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no existe")

        // apaga todos y activa este
        repo.deactivateAllActive()
        repo.activateById(periodId)

        return ResponseEntity.ok().build()
    }

    // ✅ RESUMEN / HISTORIAL (con conteo de estudiantes)
    // GET /admin/academic-periods/summary
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    fun summary(): List<Map<String, Any?>> {
        return repo.findAll()
            .sortedByDescending { it.startDate }
            .map { p ->
                mapOf(
                    "id" to p.id!!,
                    "name" to p.name,
                    "startDate" to p.startDate,
                    "endDate" to p.endDate,
                    "isActive" to p.isActive,
                    "studentCount" to studentRepo.countByAcademicPeriod(p.id!!)
                )
            }
    }

    // ✅ CUÁNTOS ESTUDIANTES ESTÁN SIN PERIODO
    // GET /admin/academic-periods/unassigned-count
    @GetMapping("/unassigned-count")
    @PreAuthorize("hasRole('ADMIN')")
    fun unassignedCount(): Map<String, Any> {
        return mapOf("sinPeriodo" to studentRepo.countWithoutAcademicPeriod())
    }

    // ✅ ARREGLAR DATOS VIEJOS: asignar a TODOS los estudiantes con academicPeriod null un periodo
    // POST /admin/academic-periods/{periodId}/assign-null-students
    @PostMapping("/{periodId}/assign-null-students")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    fun assignNullStudents(@PathVariable periodId: Long): ResponseEntity<Map<String, Any>> {
        val period = repo.findById(periodId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Periodo no existe") }

        val students = studentRepo.findAll().filter { it.academicPeriod == null }
        students.forEach { it.academicPeriod = period }
        studentRepo.saveAll(students)

        return ResponseEntity.ok(
            mapOf(
                "periodId" to period.id!!,
                "updatedCount" to students.size
            )
        )
    }

    // ✅ DEVOLVER PERIODO ACTIVO (para COORDINATOR/TUTOR/ADMIN)
    // GET /admin/academic-periods/active
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','COORDINATOR','TUTOR')")
    fun active(): ResponseEntity<AcademicPeriodDto> {
        val p = service.getActiveOrNull() ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(p.toDto())
    }

    // =========================
    // helpers
    // =========================
    private fun com.example.Aplicativo_web.entity.AcademicPeriodEntity.toDto(): AcademicPeriodDto {
        return AcademicPeriodDto(
            id = this.id!!,
            name = this.name,
            startDate = this.startDate,
            endDate = this.endDate,
            isActive = this.isActive
        )
    }
}
