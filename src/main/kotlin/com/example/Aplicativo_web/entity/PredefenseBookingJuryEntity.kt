package com.example.Aplicativo_web.entity

import jakarta.persistence.*

@Entity
@Table(name = "predefense_booking_jury")
class PredefenseBookingJuryEntity(

    @EmbeddedId
    var id: PredefenseBookingJuryId = PredefenseBookingJuryId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("bookingId")
    @JoinColumn(name = "booking_id")
    var booking: PredefenseBookingEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("juryUserId")
    @JoinColumn(name = "jury_user_id")
    var juryUser: AppUserEntity? = null
)
