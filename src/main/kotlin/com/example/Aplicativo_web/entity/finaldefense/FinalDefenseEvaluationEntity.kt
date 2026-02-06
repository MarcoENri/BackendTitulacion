package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.AppUserEntity
import com.example.Aplicativo_web.entity.StudentEntity
import com.example.Aplicativo_web.entity.enums.FinalDefenseVerdict
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "final_defense_evaluation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_fde_booking_jury_student",
            columnNames = ["booking_id", "jury_user_id", "student_id"]
        )
    ]
)
class FinalDefenseEvaluationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    var booking: FinalDefenseBookingEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    var student: StudentEntity,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "jury_user_id", nullable = false)
    var juryUser: AppUserEntity,

    @Column(name = "rubric_score", nullable = false)
    var rubricScore: Int = 0,

    @Column(name = "extra_score", nullable = false)
    var extraScore: Int = 0,

    @Column(name = "total_score", nullable = false)
    var totalScore: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict")
    var verdict: FinalDefenseVerdict? = null,

    @Column(name = "observations")
    var observations: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)

