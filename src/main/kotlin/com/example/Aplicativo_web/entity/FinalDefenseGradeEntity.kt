package com.example.Aplicativo_web.entity

import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseBookingEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "final_defense_grade",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_final_defense_grade_booking_jury", columnNames = ["booking_id", "jury_user_id"])
    ]
)
class FinalDefenseGradeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: FinalDefenseBookingEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jury_user_id", nullable = false)
    var juryUser: AppUserEntity,

    @Column(name = "rubric_score", nullable = false, precision = 5, scale = 2)
    var rubricScore: BigDecimal = BigDecimal.ZERO,  // 0..50

    @Column(name = "other_score", nullable = false, precision = 5, scale = 2)
    var otherScore: BigDecimal = BigDecimal.ZERO,   // 0..50

    @Column(name = "total_score", nullable = false, precision = 5, scale = 2)
    var totalScore: BigDecimal = BigDecimal.ZERO,   // 0..100

    @Column(columnDefinition = "text")
    var comments: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {
    // <- Importante: esta llave cierra la clase
}
