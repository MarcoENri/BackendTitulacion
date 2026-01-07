package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "email_log")
class EmailLogEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "to_email", nullable = false)
    var toEmail: String = "",

    @Column(nullable = false)
    var subject: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var body: String = "",

    @Column(name = "related_student_id")
    var relatedStudentId: Long? = null,

    @Column(name = "sent_by_user_id")
    var sentByUserId: Long? = null,

    @Column(nullable = false)
    var status: String = "SENT", // SENT / FAILED

    @Column(name = "error_message", columnDefinition = "text")
    var errorMessage: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
