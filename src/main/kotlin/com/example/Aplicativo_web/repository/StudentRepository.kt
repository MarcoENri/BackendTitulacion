package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface StudentRepository : JpaRepository<StudentEntity, Long> {
    fun findByDni(dni: String): Optional<StudentEntity>
}
