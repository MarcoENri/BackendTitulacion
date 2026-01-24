package com.example.Aplicativo_web.entity

import com.example.Aplicativo_web.entity.finaldefense.FinalDefenseGroupEntity
import jakarta.persistence.*

@Embeddable
data class FinalDefenseGroupStudentId(
    @Column(name = "group_id") var groupId: Long = 0,
    @Column(name = "student_id") var studentId: Long = 0
)

@Entity
@Table(name = "final_defense_group_student")
class FinalDefenseGroupStudentEntity(
    @EmbeddedId
    var id: FinalDefenseGroupStudentId = FinalDefenseGroupStudentId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    var group: FinalDefenseGroupEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    var student: StudentEntity? = null
)
