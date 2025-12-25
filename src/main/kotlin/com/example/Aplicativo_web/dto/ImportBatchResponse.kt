package com.example.Aplicativo_web.dto

data class ImportBatchResponse(
    val batchId: Long,
    val status: String,
    val fileName: String,
    val totalRows: Int,
    val insertedRows: Int,
    val updatedRows: Int,
    val failedRows: Int
)
