package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.dto.AssignCareersRequest
import com.example.Aplicativo_web.dto.CreateUserRequest
import com.example.Aplicativo_web.dto.UserResponse
import com.example.Aplicativo_web.entity.AppUserEntity
import com.example.Aplicativo_web.entity.UserCareerEntity
import com.example.Aplicativo_web.entity.UserCareerId
import com.example.Aplicativo_web.entity.UserRoleEntity
import com.example.Aplicativo_web.entity.UserRoleId
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.CareerRepository
import com.example.Aplicativo_web.repository.RoleRepository
import com.example.Aplicativo_web.repository.UserCareerRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class AdminUserService(
    private val userRepo: AppUserRepository,
    private val roleRepo: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userCareerRepo: UserCareerRepository,
    private val careerRepo: CareerRepository
) {

    // ✅ Crear usuario + asignar roles
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

    // ✅ Asignar carreras a un usuario (COORDINATOR / TUTOR)
    @Transactional
    fun assignCareers(userId: Long, req: AssignCareersRequest) {
        val user = userRepo.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe") }

        val careerIds = req.careerIds.distinct()

        // si mandan lista vacía, se interpreta como "quitar todas"
        if (careerIds.isEmpty()) {
            userCareerRepo.deleteAllByUserId(user.id!!)
            return
        }

        // validar carreras existen (trae entidades CareerEntity)
        val careers = careerRepo.findAllById(careerIds).associateBy { it.id!! }
        if (careers.size != careerIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más carreras no existen")
        }

        // reemplazar asignaciones anteriores
        userCareerRepo.deleteAllByUserId(user.id!!)

        // ✅ Guardar usando EmbeddedId + relaciones
        careerIds.forEach { cid ->
            val career = careers[cid]
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no existe: $cid")

            userCareerRepo.save(
                UserCareerEntity(
                    id = UserCareerId(
                        userId = user.id!!,
                        careerId = career.id!!
                    ),
                    user = user,
                    career = career
                )
            )
        }
    }
}
