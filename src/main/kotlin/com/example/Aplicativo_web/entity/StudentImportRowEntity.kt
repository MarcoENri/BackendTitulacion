package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "student_import_row")
class StudentImportRowEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "batch_id", nullable = false)
    var batchId: Long = 0,

    @Column(name = "row_number", nullable = false)
    var rowNumber: Int = 0,

    var dni: String? = null,
    @Column(name = "first_name") var firstName: String? = null,
    @Column(name = "last_name") var lastName: String? = null,
    var email: String? = null,
    var corte: String? = null,
    var section: String? = null,
    var modality: String? = null,
    @Column(name = "career_name") var careerName: String? = null,

    @Column(nullable = false)
    var status: String = "OK", // OK / ERROR

    @Column(name = "error_message")
    var errorMessage: String? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
)
