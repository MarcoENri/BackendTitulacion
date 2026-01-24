package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.AcademicPeriodEntity
import com.example.Aplicativo_web.entity.CareerEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "final_defense_window")
class FinalDefenseWindowEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    var academicPeriod: AcademicPeriodEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id")
    var career: CareerEntity? = null,

    @Column(name = "starts_at", nullable = false)
    var startsAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ends_at", nullable = false)
    var endsAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    // ✅ Campo agregado para la rúbrica
    @Column(name = "rubric_path")
    var rubricPath: String? = null
)