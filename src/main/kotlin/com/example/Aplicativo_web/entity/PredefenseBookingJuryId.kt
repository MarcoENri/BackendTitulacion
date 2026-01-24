package com.example.Aplicativo_web.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
class PredefenseBookingJuryId(
    @Column(name = "booking_id")
    var bookingId: Long? = null,

    @Column(name = "jury_user_id")
    var juryUserId: Long? = null
) : Serializable
