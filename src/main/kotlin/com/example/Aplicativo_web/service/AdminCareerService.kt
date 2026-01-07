package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.CareerDto
import com.example.Aplicativo_web.repository.CareerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCareerService(
    private val careerRepo: CareerRepository
) {

    @Transactional(readOnly = true)
    fun listCareers(): List<CareerDto> {
        return careerRepo.findAll()
            .sortedBy { it.name.lowercase() }
            .map { CareerDto(id = it.id!!, name = it.name) }
    }
}
