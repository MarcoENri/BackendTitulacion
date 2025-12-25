package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.StudentImportBatchEntity
import org.springframework.data.jpa.repository.JpaRepository

interface StudentImportBatchRepository : JpaRepository<StudentImportBatchEntity, Long>
