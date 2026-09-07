package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SupplierTransactionType(val index: Int) {
    PURCHASE(0),      // Udhaar Liya (Credit taken)
    PAYMENT(1);       // Payment Given (Cash paid)

    val isPurchase: Boolean get() = this == PURCHASE
    val isPayment: Boolean get() = this == PAYMENT
}

@Entity(
    tableName = "supplier_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId"), Index("date")]
)
data class SupplierTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val supplierName: String,
    val type: SupplierTransactionType,
    val amount: Double,
    val description: String? = null,
    val invoiceNumber: String? = null,
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
