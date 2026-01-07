package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.entity.EmailLogEntity
import com.example.Aplicativo_web.repository.AppUserRepository
import com.example.Aplicativo_web.repository.EmailLogRepository
import com.example.Aplicativo_web.repository.StudentRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class StudentEmailService(
    private val mailSender: JavaMailSender,
    private val studentRepo: StudentRepository,
    private val userRepo: AppUserRepository,
    private val emailLogRepo: EmailLogRepository,
    @Value("\${app.mail.from}") private val fromEmail: String,
    @Value("\${app.mail.enabled:true}") private val mailEnabled: Boolean
) {

    @Transactional
    fun sendToStudent(studentId: Long, subject: String, body: String, senderUsername: String) {

        if (!mailEnabled) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Envío de correo deshabilitado en configuración")
        }

        val student = studentRepo.findById(studentId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante no existe") }

        val toEmail = student.email.trim()
        if (toEmail.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "El estudiante no tiene email")
        }

        val senderUser = userRepo.findByUsername(senderUsername)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no existe")

        val subjectTrim = subject.trim()
        val bodyTrim = body.trim()

        if (subjectTrim.isBlank() || bodyTrim.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Asunto y mensaje son obligatorios")
        }

        val log = EmailLogEntity(
            toEmail = toEmail,
            subject = subjectTrim,
            body = bodyTrim,
            relatedStudentId = studentId,
            sentByUserId = senderUser.id,
            status = "SENT",
            createdAt = LocalDateTime.now()
        )

        try {
            val msg = SimpleMailMessage()
            msg.setTo(toEmail)
            msg.from = fromEmail // ✅ importante
            msg.subject = subjectTrim

            msg.text = """
                $bodyTrim

                ---
                Enviado por: ${senderUser.fullName} (@${senderUser.username})
            """.trimIndent()

            mailSender.send(msg)

            emailLogRepo.save(log)
        } catch (ex: Exception) {
            ex.printStackTrace() // ✅ AHORA verás el error real en consola

            log.status = "FAILED"
            log.errorMessage = ex.message
            emailLogRepo.save(log)

            throw ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "No se pudo enviar el correo: ${ex.message}"
            )
        }
    }
}
