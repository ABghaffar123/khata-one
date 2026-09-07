package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val barcode: String? = null,
    val unit: ProductUnit = ProductUnit.PIECE,
    val salePrice: Double,
    val purchasePrice: Double,
    val stockQuantity: Double = 0.0,
    val lowStockThreshold: Double = 5.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val stockValue: Double get() = stockQuantity * purchasePrice
    val potentialRevenue: Double get() = stockQuantity * salePrice
    val estimatedProfit: Double get() = potentialRevenue - stockValue
    val isLowStock: Boolean get() = stockQuantity <= lowStockThreshold
}

enum class ProductUnit(val displayName: String) {
    KG("kg"),
    GRAM("g"),
    LITER("L"),
    ML("ml"),
    PACKET("packet"),
    PIECE("piece"),
    BOTTLE("bottle"),
    BOX("box"),
    BAG("bag"),
    DOZEN("dozen"),
    PAIR("pair"),
    METER("m")
}
