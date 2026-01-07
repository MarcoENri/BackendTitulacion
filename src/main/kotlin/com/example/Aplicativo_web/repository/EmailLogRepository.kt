package com.example.Aplicativo_web.repository

import com.example.Aplicativo_web.entity.EmailLogEntity
import org.springframework.data.jpa.repository.JpaRepository

interface EmailLogRepository : JpaRepository<EmailLogEntity, Long>
