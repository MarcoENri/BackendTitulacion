package com.example.Aplicativo_web.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "student_project")
class StudentProjectEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    var student: StudentEntity? = null,

    @Column(name = "project_name", nullable = false)
    var projectName: String = "",

    @Column(name = "assigned_at", nullable = false)
    var assignedAt: LocalDateTime = LocalDateTime.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by_user_id")
    var assignedBy: AppUserEntity? = null,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean = true
)
