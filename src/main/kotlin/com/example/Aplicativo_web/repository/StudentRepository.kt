package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface StudentRepository : JpaRepository<StudentEntity, Long> {

    fun findByDni(dni: String): Optional<StudentEntity>

    @Query("""
        select s from StudentEntity s
        where s.career.id in :careerIds
        order by s.lastName asc, s.firstName asc
    """)
    fun findAllByCareerIds(@Param("careerIds") careerIds: List<Long>): List<StudentEntity>
}
