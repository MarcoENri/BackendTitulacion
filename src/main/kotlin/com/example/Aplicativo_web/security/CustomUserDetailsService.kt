package com.example.Aplicativo_web.security

import com.example.Aplicativo_web.repository.AppUserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService(
    private val userRepo: AppUserRepository
) : UserDetailsService {

    @Transactional(readOnly = true)
    override fun loadUserByUsername(input: String): UserDetails {

        val user = userRepo.findByUsernameOrEmailWithRoles(input)
            .orElseThrow {
                UsernameNotFoundException("Usuario o correo no encontrado: $input")
            }

        val authorities = user.roles
            .mapNotNull { it.role?.name }
            .map { roleName ->
                val normalized =
                    if (roleName.startsWith("ROLE_")) roleName else "ROLE_$roleName"
                SimpleGrantedAuthority(normalized)
            }

        return User(
            user.username,   // identidad interna
            user.password,
            user.enabled,
            true,
            true,
            true,
            authorities
        )
    }
}
