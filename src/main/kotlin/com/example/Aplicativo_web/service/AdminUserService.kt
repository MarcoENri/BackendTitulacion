package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.CreateUserRequest
import com.example.Aplicativo_web.dto.UserResponse
import com.example.Aplicativo_web.entity.AppUserEntity
import com.example.Aplicativo_web.entity.UserRoleEntity
import com.example.Aplicativo_web.entity.UserRoleId
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.RoleRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

@Service
class AdminUserService(
    private val userRepo: AppUserRepository,
    private val roleRepo: RoleRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun createUser(req: CreateUserRequest): UserResponse {
        if (userRepo.existsByUsername(req.username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe")
        }

        val user = AppUserEntity(
            username = req.username.trim(),
            password = passwordEncoder.encode(req.password),
            fullName = req.fullName.trim(),
            email = req.email.trim(),
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        val saved = userRepo.save(user)

        val roleEntities = req.roles.distinct().map { roleName ->
            roleRepo.findByName(roleName)
                .orElseThrow { ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no existe: $roleName") }
        }

        roleEntities.forEach { role ->
            saved.roles.add(
                UserRoleEntity(
                    id = UserRoleId(saved.id, role.id),
                    user = saved,
                    role = role
                )
            )
        }

        val finalUser = userRepo.save(saved)

        return UserResponse(
            id = finalUser.id!!,
            username = finalUser.username,
            fullName = finalUser.fullName,
            email = finalUser.email,
            enabled = finalUser.enabled,
            roles = finalUser.roles.mapNotNull { it.role?.name }
        )
    }
}
