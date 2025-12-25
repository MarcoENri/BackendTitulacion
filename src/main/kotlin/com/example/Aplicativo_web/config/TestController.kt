package com.example.Aplicativo_web.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController {

    @GetMapping("/me")
    fun me() = mapOf("status" to "JWT OK")
}
