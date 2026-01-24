package com.example.Aplicativo_web.service.storage

import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class CareerCoverStorageService {

    private val baseDir: Path = Paths.get("uploads", "career-covers")
        .toAbsolutePath().normalize()

    init {
        Files.createDirectories(baseDir)
    }

    fun saveCareerCover(careerId: Long, bytes: ByteArray, originalFilename: String?): String {
        val ext = (originalFilename ?: "")
            .substringAfterLast('.', "")
            .lowercase()

        val allowed = setOf("png", "jpg", "jpeg", "webp")
        if (ext !in allowed) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo PNG/JPG/WEBP")
        }

        val filename = "career_${careerId}_cover_${System.currentTimeMillis()}.$ext"
        val target = baseDir.resolve(filename).normalize()

        Files.write(target, bytes)
        return filename
    }

    fun loadAsResource(filename: String): Resource {
        val cleaned = filename.trim()
        val p = baseDir.resolve(cleaned).normalize()

        if (!p.startsWith(baseDir)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta inválida de portada")
        }
        if (!Files.exists(p)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Portada no encontrada")
        }

        val res = UrlResource(p.toUri())
        if (!res.exists() || !res.isReadable) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede leer la portada")
        }
        return res
    }
}
