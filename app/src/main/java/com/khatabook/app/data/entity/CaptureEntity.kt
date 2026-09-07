package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "captures")
data class CaptureEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val recognizedText: String? = null,
    val parsedAmount: Double? = null,
    val parsedCustomerName: String? = null,
    val isConfirmed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
