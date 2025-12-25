package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "incident")
class IncidentEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: StudentEntity? = null,

    @Column(nullable = false)
    var stage: String = "",

    @Column(nullable = false)
    var date: LocalDate = LocalDate.now(),

    @Column(nullable = false, columnDefinition = "text")
    var reason: String = "",

    @Column(nullable = false, columnDefinition = "text")
    var action: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,

    @Column(name = "created_by_user_id")
    var createdByUserId: Long? = null
)
