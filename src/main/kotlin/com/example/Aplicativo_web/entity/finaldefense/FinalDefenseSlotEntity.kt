package com.example.Aplicativo_web.entity.finaldefense

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "final_defense_slot")
class FinalDefenseSlotEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "window_id", nullable = false)
    var window: FinalDefenseWindowEntity? = null,

    @Column(name = "starts_at", nullable = false)
    var startsAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ends_at", nullable = false)
    var endsAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
