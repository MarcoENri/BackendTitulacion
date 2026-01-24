package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.IncidentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface IncidentRepository : JpaRepository<IncidentEntity, Long> {

    fun findAllByStudent_Id(studentId: Long): List<IncidentEntity>

    // ✅ contar para 1 estudiante
    @Query("select count(i) from IncidentEntity i where i.student.id = :studentId")
    fun countByStudentId(@Param("studentId") studentId: Long): Long

    // ✅ contar para varios (opcional, por si lo usas luego)
    interface StudentCount {
        fun getStudentId(): Long
        fun getCnt(): Long
    }

    @Query("""
        select i.student.id as studentId, count(i) as cnt
        from IncidentEntity i
        where i.student.id in :studentIds
        group by i.student.id
    """)
    fun countByStudentIds(@Param("studentIds") studentIds: List<Long>): List<StudentCount>
}
