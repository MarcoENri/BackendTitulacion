package com.example.Aplicativo_web.controller

import com.example.Aplicativo_web.repository.CareerRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/careers")
class CareerController(
    private val careerRepo: CareerRepository
) {
    data class CareerDto(val id: Long, val name: String)

    @GetMapping
    fun list(): List<CareerDto> =
        careerRepo.findAll().map { CareerDto(it.id!!, it.name) }
}
