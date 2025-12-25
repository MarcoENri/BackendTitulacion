package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "student")
class StudentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var dni: String = "",

    @Column(name = "first_name", nullable = false)
    var firstName: String = "",

    @Column(name = "last_name", nullable = false)
    var lastName: String = "",

    @Column(nullable = false)
    var email: String = "",

    @Column(nullable = false)
    var corte: String = "",

    @Column(nullable = false)
    var section: String = "",

    var modality: String? = null,

    @Column(name = "titulation_type", nullable = false)
    var titulationType: String = "EXAMEN",

    @Column(nullable = false)
    var status: String = "EN_CURSO",

    @Column(name = "not_apt_reason")
    var notAptReason: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: CareerEntity? = null,

    @Column(name = "created_at")
    var createdAt: LocalDateTime? = null,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null
)
