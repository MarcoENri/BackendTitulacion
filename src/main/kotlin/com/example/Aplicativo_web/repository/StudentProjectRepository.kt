package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentProjectEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface StudentProjectRepository : JpaRepository<StudentProjectEntity, Long> {

    @Query("""
        select sp from StudentProjectEntity sp
        where sp.student.id = :studentId and sp.isCurrent = true
        order by sp.assignedAt desc
    """)
    fun findCurrentByStudentId(@Param("studentId") studentId: Long): List<StudentProjectEntity>
}
