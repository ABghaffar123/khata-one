package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val shopName: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val totalCreditTaken: Double = 0.0,  // Udhaar Liya (we owe)
    val totalPaymentGiven: Double = 0.0, // Payment Given (we paid)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val balance: Double get() = totalCreditTaken - totalPaymentGiven
    val hasPayable: Boolean get() = balance > 0

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
