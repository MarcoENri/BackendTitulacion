package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "predefense_observation")
class PredefenseObservationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: PredefenseBookingEntity? = null,

    @Column(name = "author_user_id")
    var authorUserId: Long? = null,

    @Column(name = "author_name", nullable = false)
    var authorName: String = "",

    @Column(name = "text", nullable = false, columnDefinition = "text")
    var text: String = "",

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
