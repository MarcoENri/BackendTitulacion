package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.enums.FinalDefenseBookingStatus
import com.example.Aplicativo_web.entity.enums.FinalDefenseVerdict
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "final_defense_booking")
class FinalDefenseBookingEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    var slot: FinalDefenseSlotEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    var group: FinalDefenseGroupEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FinalDefenseBookingStatus = FinalDefenseBookingStatus.SCHEDULED,

    @Column(name = "final_average")
    var finalAverage: Double? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict")
    var verdict: FinalDefenseVerdict? = null,

    @Column(name = "final_observations")
    var finalObservations: String? = null,

    @Column(name = "acta_path")
    var actaPath: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
