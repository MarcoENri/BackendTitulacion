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
class FinalDefenseRubricStorageService {

    private val baseDir: Path = Paths.get("uploads", "final-defense-rubrics").toAbsolutePath().normalize()

    init {
        Files.createDirectories(baseDir)
    }

    // ✅ Guardamos SOLO el filename (más seguro y portable)
    fun saveRubricPdf(windowId: Long, bytes: ByteArray, originalFilename: String?): String {
        val name = (originalFilename ?: "rubrica.pdf").lowercase()
        if (!name.endsWith(".pdf")) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permite PDF")

        val filename = "window_${windowId}_rubric_${System.currentTimeMillis()}.pdf"
        val target = baseDir.resolve(filename).normalize()

        Files.write(target, bytes)

        return filename
    }

    // ✅ Si viene filename, resolvemos con baseDir
    fun loadAsResource(storedPathOrFilename: String): Resource {
        val cleaned = storedPathOrFilename.trim()

        val p = if (cleaned.contains("/") || cleaned.contains("\\") || cleaned.startsWith("C:")) {
            // si alguien ya guardó un path completo, lo soportamos igual
            Paths.get(cleaned).toAbsolutePath().normalize()
        } else {
            baseDir.resolve(cleaned).normalize()
        }

        // seguridad: evitar salirte de uploads
        if (!p.startsWith(baseDir)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ruta inválida de rúbrica")
        }

        if (!Files.exists(p)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el PDF en disco: ${p.fileName}")
        }

        val res = UrlResource(p.toUri())
        if (!res.exists() || !res.isReadable) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "No se puede leer el PDF")
        }
        return res
    }
}
