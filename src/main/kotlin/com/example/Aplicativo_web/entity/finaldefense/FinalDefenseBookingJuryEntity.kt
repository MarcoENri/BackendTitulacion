package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.AppUserEntity
import jakarta.persistence.*

@Entity
@Table(name = "final_defense_booking_jury")
class FinalDefenseBookingJuryEntity(
    @EmbeddedId
    var id: FinalDefenseBookingJuryId = FinalDefenseBookingJuryId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    var booking: FinalDefenseBookingEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juryUserId")
    @JoinColumn(name = "jury_user_id")
    var juryUser: AppUserEntity? = null
)
