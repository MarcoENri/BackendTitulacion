package com.example.Aplicativo_web.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class JwtService(
    @Value("\${app.jwt.secret}") private val secret: String,
    @Value("\${app.jwt.issuer}") private val issuer: String,
    @Value("\${app.jwt.access-token-minutes:60}") private val minutes: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(username: String, authorities: Collection<GrantedAuthority>): String {
        val now = Instant.now()
        val exp = now.plus(minutes, ChronoUnit.MINUTES)

        val roles = authorities.map { it.authority } // e.g. ROLE_ADMIN

        return JWT.create()
            .withIssuer(issuer)
            .withSubject(username)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(exp))
            .withClaim("roles", roles)
            .sign(algorithm)
    }

    fun validateAndGetUsername(token: String): String {
        val verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .build()

        val decoded = verifier.verify(token)
        return decoded.subject
    }
}
