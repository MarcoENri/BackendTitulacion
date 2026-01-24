package com.example.Aplicativo_web.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }



    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { } // asegúrate de tener CorsConfigurationSource si lo necesitas
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // ✅ Preflight CORS
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                    // ✅ Auth público
                    .requestMatchers("/auth/**").permitAll()

                    .requestMatchers(HttpMethod.GET, "/admin/careers/cover/**").permitAll()

                    // ✅ Básicos
                    .requestMatchers("/error").permitAll()

                    // (Opcional) health check si lo usas
                    // .requestMatchers("/actuator/health").permitAll()

                    // 🔒 Todo lo demás requiere JWT
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
