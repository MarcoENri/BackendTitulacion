package com.example.Aplicativo_web.service

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {

    fun sendResetPasswordEmail(to: String, link: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = "Recuperación de contraseña"
        message.text = """
            Has solicitado recuperar tu contraseña.

            Para continuar, haz clic en el siguiente enlace:
            $link

            Este enlace es válido por 30 minutos.
        """.trimIndent()

        mailSender.send(message)
    }
}
