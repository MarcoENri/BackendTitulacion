package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.AcademicPeriodEntity
import com.example.Aplicativo_web.entity.CareerEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "final_defense_group")
class FinalDefenseGroupEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_period_id", nullable = false)
    var academicPeriod: AcademicPeriodEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    var career: CareerEntity? = null,

    @Column(name = "project_name")
    var projectName: String? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
)
