package com.example.Aplicativo_web.entity.finaldefense

import com.example.Aplicativo_web.entity.StudentEntity
import jakarta.persistence.*

@Entity
@Table(name = "final_defense_group_member")
class FinalDefenseGroupMemberEntity(
    @EmbeddedId
    var id: FinalDefenseGroupMemberId = FinalDefenseGroupMemberId(),

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    var group: FinalDefenseGroupEntity? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("studentId")
    @JoinColumn(name = "student_id")
    var student: StudentEntity? = null
)
