package com.example.Aplicativo_web.service

import com.example.Aplicativo_web.repository.finaldefense.*
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.awt.Color
import java.io.ByteArrayOutputStream

@Service
class FinalDefenseActaPdfService(
    private val bookingRepo: FinalDefenseBookingRepository,
    private val evalRepo: FinalDefenseEvaluationRepository,
    private val groupMemberRepo: FinalDefenseGroupMemberRepository,
    private val bookingJuryRepo: FinalDefenseBookingJuryRepository
) {
    fun buildActaPdf(bookingId: Long): ByteArray {
        val booking = bookingRepo.findById(bookingId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Booking no existe") }

        val slot = booking.slot ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin slot")
        val group = booking.group ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking sin grupo")

        val members = groupMemberRepo.findAllByGroup_Id(group.id!!)
        val students = members.mapNotNull { it.student }

        val juries = bookingJuryRepo.findAllByBooking_Id(booking.id!!).mapNotNull { it.juryUser?.fullName }
        val evals = evalRepo.findAllByBooking_IdOrderByCreatedAtAsc(bookingId)

        val out = ByteArrayOutputStream()
        val doc = Document(PageSize.A4, 36f, 36f, 36f, 36f)
        PdfWriter.getInstance(doc, out)
        doc.open()

        val title = Paragraph("ACTA DE DEFENSA FINAL", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f))
        title.alignment = Element.ALIGN_CENTER
        doc.add(title)
        doc.add(Paragraph(" "))

        doc.add(Paragraph("Periodo: ${group.academicPeriod?.name ?: "-"}"))
        doc.add(Paragraph("Carrera: ${group.career?.name ?: "-"}"))
        doc.add(Paragraph("Tema/Proyecto: ${group.projectName ?: "-"}"))
        doc.add(Paragraph("Fecha/Hora: ${slot.startsAt}  →  ${slot.endsAt}"))
        doc.add(Paragraph(" "))

        doc.add(Paragraph("Integrantes:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
        students.forEach { s -> doc.add(Paragraph("- ${s.firstName} ${s.lastName} (${s.dni})")) }
        doc.add(Paragraph(" "))

        doc.add(Paragraph("Tribunal (3 jurados):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
        juries.forEach { j -> doc.add(Paragraph("- $j")) }
        doc.add(Paragraph(" "))

        // Tabla: 4 columnas
        val table = PdfPTable(4)
        table.widthPercentage = 100f
        table.setWidths(floatArrayOf(4f, 1.5f, 1.5f, 1.5f))

        fun th(text: String): PdfPCell {
            val c = PdfPCell(Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10f)))
            c.backgroundColor = Color(230, 230, 230)
            c.horizontalAlignment = Element.ALIGN_CENTER
            c.paddingLeft = 6f
            return c
        }

        table.addCell(th("Jurado"))
        table.addCell(th("Rúbrica/50"))
        table.addCell(th("Extra/50"))
        table.addCell(th("Total/100"))

        evals.forEach { e ->
            table.addCell(PdfPCell(Phrase(e.juryUser?.fullName ?: "-")))
            table.addCell(PdfPCell(Phrase("${e.rubricScore}")))
            table.addCell(PdfPCell(Phrase("${e.extraScore}")))
            table.addCell(PdfPCell(Phrase("${e.totalScore}")))
        }

        doc.add(table)
        doc.add(Paragraph(" "))

        val avg = booking.finalAverage
        doc.add(
            Paragraph(
                "Promedio final (automático): ${avg?.let { String.format("%.2f", it) } ?: "-"}",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
            )
        )

        val finalVerdict =
            booking.verdict?.name
                ?: if (avg == null) "PENDIENTE"
                else if (avg >= 70.0) "APROBADO" else "REPROBADO"

        doc.add(Paragraph("Veredicto final: $finalVerdict", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
        doc.add(Paragraph(" "))

        // Observaciones jurados
        val obs = evals.mapNotNull { it.observations?.trim()?.takeIf { s -> s.isNotBlank() } }
        doc.add(Paragraph("Observaciones (jurados):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
        if (obs.isEmpty()) doc.add(Paragraph("-")) else obs.forEach { doc.add(Paragraph("- $it")) }

        // Observación final del booking (si existe)
        val finalObs = booking.finalObservations?.trim()?.takeIf { it.isNotBlank() }
        if (finalObs != null) {
            doc.add(Paragraph(" "))
            doc.add(Paragraph("Observación final (registro):", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)))
            doc.add(Paragraph(finalObs))
        }

        doc.add(Paragraph(" "))
        doc.add(Paragraph("Firmas (referencial):"))
        juries.forEach { doc.add(Paragraph("__________________________   $it")) }

        doc.close()
        return out.toByteArray()
    }
}
