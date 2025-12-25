package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.CareerEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface CareerRepository : JpaRepository<CareerEntity, Long> {
    fun findByName(name: String): Optional<CareerEntity>
}
