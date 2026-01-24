package com.example.Aplicativo_web.service.finaldefense

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class FinalDefenseActaStorageService {

    private val baseDir: Path = Paths.get("uploads", "final-defense-actas").toAbsolutePath().normalize()

    init {
        Files.createDirectories(baseDir)
    }

    // ✅ Guardamos SOLO el filename (portable)
    fun saveActaPdf(bookingId: Long, bytes: ByteArray): String {
        val filename = "acta_booking_${bookingId}_${System.currentTimeMillis()}.pdf"
        val target = baseDir.resolve(filename).normalize()

        Files.write(target, bytes)
        return filename
    }

    fun loadAsResource(storedPathOrFilename: String): Resource {
        val cleaned = storedPathOrFilename.trim()

        val p = if (cleaned.contains("/") || cleaned.contains("\\") || cleaned.startsWith("C:")) {
            Paths.get(cleaned).toAbsolutePath().normalize()
        } else {
            baseDir.resolve(cleaned).normalize()
        }

        // seguridad
        if (!p.startsWith(baseDir)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta inválida de acta")
        }

        if (!Files.exists(p)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el acta PDF en disco: ${p.fileName}")
        }

        val res = UrlResource(p.toUri())
        if (!res.exists() || !res.isReadable) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede leer el acta PDF")
        }

        return res
    }

    fun resolvePath(filename: String): Path {
        val cleaned = filename.trim()
        return baseDir.resolve(cleaned).normalize()
    }
}
