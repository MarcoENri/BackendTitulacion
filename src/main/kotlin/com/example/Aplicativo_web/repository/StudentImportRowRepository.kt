package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentImportRowEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentImportRowRepository : JpaRepository<StudentImportRowEntity, Long>
