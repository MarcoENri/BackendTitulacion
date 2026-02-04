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

    // ==========================
    // CREAR USUARIO (CON CARRERAS)
    // ==========================
    @Transactional
    fun createUser(req: CreateUserRequest): UserResponse {

        val username = req.username.trim().lowercase()
        val email = req.email.trim().lowercase()

        if (userRepo.existsByUsername(username)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "El username ya existe")
        }

        if (userRepo.existsByEmail(email)) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado")
        }

        val user = AppUserEntity(
            username = username,
            password = passwordEncoder.encode(req.password),
            fullName = req.fullName.trim(),
            email = email,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        val saved = userRepo.save(user)

        // ✅ aceptar roles con o sin ROLE_
        val normalizedRoles = req.roles
            .distinct()
            .map { it.trim().uppercase() }
            .map { if (it.startsWith("ROLE_")) it.removePrefix("ROLE_") else it }

        val roles = normalizedRoles.map { roleName ->
            roleRepo.findByName(roleName)
                .orElseThrow {
                    ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no existe: $roleName")
                }
        }

        roles.forEach { role ->
            saved.roles.add(
                UserRoleEntity(
                    id = UserRoleId(saved.id, role.id),
                    user = saved,
                    role = role
                )
            )
        }

        val finalUser = userRepo.save(saved)

        // ✅ guardar carreras si llegan
        val careerIds = (req.careerIds ?: emptyList()).distinct()
        if (careerIds.isNotEmpty()) {

            val careers = careerRepo.findAllById(careerIds).associateBy { it.id!! }
            if (careers.size != careerIds.size) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más carreras no existen")
            }

            userCareerRepo.deleteAllByUserId(finalUser.id!!)

            careerIds.forEach { cid ->
                val career = careers[cid]
                    ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no existe: $cid")

                userCareerRepo.save(
                    UserCareerEntity(
                        id = UserCareerId(finalUser.id!!, career.id!!),
                        user = finalUser,
                        career = career
                    )
                )
            }
        }

        return toResponse(finalUser)
    }

    // ==========================
    // LISTAR USUARIOS (por rol)
    // ==========================
    @Transactional(readOnly = true)
    fun listUsers(role: String?): List<UserResponse> {

        val users = if (role.isNullOrBlank()) {
            userRepo.findAll()
        } else {
            val r = role.trim().uppercase().let {
                if (it.startsWith("ROLE_")) it.removePrefix("ROLE_") else it
            }
            userRepo.findAllByRoleNameWithRoles(r)
        }

        return users.map { toResponse(it) }
    }

    // ==================================================
    // ✅ NUEVO: LISTAR JURADOS (FINAL DEFENSE)
    // ==================================================
    @Transactional(readOnly = true)
    fun listJuries(): List<UserResponse> {
        return userRepo.findAll()
            .filter { user ->
                user.roles.any { ur ->
                    ur.role?.name in setOf(
                        "JURY",
                        "TUTOR",
                        "DOCENTE",
                        "COORDINATOR"
                    )
                }
            }
            .map { toResponse(it) }
    }

    // ==========================
    // ASIGNAR CARRERAS
    // ==========================
    @Transactional
    fun assignCareers(userId: Long, req: AssignCareersRequest) {

        val user = userRepo.findById(userId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no existe") }

        val careerIds = req.careerIds.distinct()

        if (careerIds.isEmpty()) {
            userCareerRepo.deleteAllByUserId(user.id!!)
            return
        }

        val careers = careerRepo.findAllById(careerIds).associateBy { it.id!! }
        if (careers.size != careerIds.size) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más carreras no existen")
        }

        userCareerRepo.deleteAllByUserId(user.id!!)

        careerIds.forEach { cid ->
            val career = careers[cid]
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Carrera no existe: $cid")

            userCareerRepo.save(
                UserCareerEntity(
                    id = UserCareerId(user.id!!, career.id!!),
                    user = user,
                    career = career
                )
            )
        }
    }

    // ==========================
    // UTIL: MAPEO ÚNICO
    // ==========================
    private fun toResponse(user: AppUserEntity): UserResponse =
        UserResponse(
            id = user.id!!,
            username = user.username,
            fullName = user.fullName,
            email = user.email,
            enabled = user.enabled,
            roles = user.roles.mapNotNull { it.role?.name }
        )
}
