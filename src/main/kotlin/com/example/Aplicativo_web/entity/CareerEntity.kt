package com.example.Aplicativo_web.entity

import jakarta.persistence.*

@Entity
@Table(name = "career")
class CareerEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    // 👇 QUITA unique=true aquí (la unicidad debe ser por normalized_name)
    @Column(nullable = false)
    var name: String = "",

    @Column(name = "cover_image")
    var coverImage: String? = null,

    @Column(name = "color")
    var color: String? = null,

    // 👇 Pon default "" para que JPA no falle si se instancia vacío
    //    (igual tu import lo setea bien siempre)
    @Column(name = "normalized_name", nullable = false)
    var normalizedName: String = ""
)
