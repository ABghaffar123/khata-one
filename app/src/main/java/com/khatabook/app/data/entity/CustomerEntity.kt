package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val whatsappNumber: String? = null,   // Separate WhatsApp number (optional)
    val photoUri: String? = null,          // Customer photo path/URI (optional)
    val address: String? = null,
    val notes: String? = null,
    val totalCredit: Double = 0.0,
    val totalPayment: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balance: Double get() = totalCredit - totalPayment
    val hasDue: Boolean get() = balance > 0

    /** WhatsApp number to use: falls back to phone if whatsappNumber is null */
    val effectiveWhatsappNumber: String get() = whatsappNumber ?: phone

    val initials: String
        get() {
            val parts = name.split(" ").filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "?"
                parts.size == 1 -> parts[0].first().uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }
}
