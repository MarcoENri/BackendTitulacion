package com.example.Aplicativo_web.security

import com.example.Aplicativo_web.repository.AppUserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepo: AppUserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepo.findByUsernameWithRoles(username)
            .orElseThrow { UsernameNotFoundException("Usuario no encontrado: $username") }

        val authorities = user.roles
            .mapNotNull { it.role?.name }
            .map { SimpleGrantedAuthority("ROLE_$it") }

        return User(
            user.username,
            user.password,
            user.enabled,
            true,
            true,
            true,
            authorities
        )
    }
}
