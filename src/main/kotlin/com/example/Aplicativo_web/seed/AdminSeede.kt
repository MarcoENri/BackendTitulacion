package com.example.Aplicativo_web.seed

import com.example.Aplicativo_web.entity.AppUserEntity
import com.example.Aplicativo_web.entity.UserRoleEntity
import com.example.Aplicativo_web.entity.UserRoleId
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.RoleRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class AdminSeeder(
    private val userRepo: AppUserRepository,
    private val roleRepo: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.seed.admin.username:admin}") private val adminUsername: String,
    @Value("\${app.seed.admin.password:Admin123*}") private val adminPassword: String
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepo.existsByUsername(adminUsername)) return

        val adminRole = roleRepo.findByName("ADMIN")
            .orElseThrow { IllegalStateException("No existe el rol ADMIN. Revisa el V1 SQL (inserts).") }

        val admin = AppUserEntity(
            username = adminUsername,
            password = passwordEncoder.encode(adminPassword),
            fullName = "Administrador",
            email = "admin@demo.com",
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        val saved = userRepo.save(admin)

        val ur = UserRoleEntity(
            id = UserRoleId(saved.id, adminRole.id),
            user = saved,
            role = adminRole
        )
        saved.roles.add(ur)
        userRepo.save(saved)

        println("✅ Admin creado: $adminUsername / $adminPassword")
    }
}
