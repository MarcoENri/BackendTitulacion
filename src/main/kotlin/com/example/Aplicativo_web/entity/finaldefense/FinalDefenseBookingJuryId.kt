package com.example.Aplicativo_web.entity.finaldefense

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class FinalDefenseBookingJuryId(
    var bookingId: Long = 0,
    var juryUserId: Long = 0
) : Serializable
