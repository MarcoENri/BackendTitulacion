package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.UserResponse
import com.example.Aplicativo_web.repository.AppUserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CoordinatorLookupService(
    private val userRepo: AppUserRepository
) {

    @Transactional(readOnly = true)
    fun listTutors(): List<UserResponse> {
        val tutors = userRepo.findAllByRoleNameWithRoles("TUTOR")

        return tutors.map { u ->
            UserResponse(
                id = u.id!!,
                username = u.username,
                fullName = u.fullName,
                email = u.email,
                enabled = u.enabled,
                roles = u.roles.mapNotNull { it.role?.name }
            )
        }
    }
}
