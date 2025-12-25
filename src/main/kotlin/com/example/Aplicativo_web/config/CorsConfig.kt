package com.example.Aplicativo_web.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter

@Configuration
class CorsConfig {

    @Bean
    fun corsFilter(): CorsFilter {
        val configuration = CorsConfiguration()
        // --- CORRECCIÓN CRÍTICA AQUÍ ---
        configuration.allowedOrigins = listOf("http://localhost:5173", "http://localhost:5183") // ¡Ahora son dos elementos en la lista!
        // -----------------------------
        configuration.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowCredentials = true
        configuration.allowedHeaders = listOf("*")
        configuration.exposedHeaders = listOf("X-Auth-Token", "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return CorsFilter(source)
    }
}