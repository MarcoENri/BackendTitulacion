package com.example.Aplicativo_web.entity.finaldefense

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class FinalDefenseGroupMemberId(
    var groupId: Long = 0,
    var studentId: Long = 0
) : Serializable
