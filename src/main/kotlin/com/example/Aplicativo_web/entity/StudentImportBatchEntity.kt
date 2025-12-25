package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "student_import_batch")
class StudentImportBatchEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "uploaded_by")
    var uploadedBy: Long? = null,

    @Column(name = "file_name", nullable = false)
    var fileName: String = "",

    @Column(name = "file_type", nullable = false)
    var fileType: String = "XLSX",

    @Column(name = "total_rows", nullable = false)
    var totalRows: Int = 0,

    @Column(name = "inserted_rows", nullable = false)
    var insertedRows: Int = 0,

    @Column(name = "updated_rows", nullable = false)
    var updatedRows: Int = 0,

    @Column(name = "failed_rows", nullable = false)
    var failedRows: Int = 0,

    @Column(nullable = false)
    var status: String = "PROCESSING", // PROCESSING / COMPLETED / FAILED

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null
)
