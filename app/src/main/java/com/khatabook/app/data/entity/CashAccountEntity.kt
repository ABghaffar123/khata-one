package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a cash account (Counter Cash, Bank Account, Mobile Wallet, etc.)
 * Each transaction can be linked to one of these accounts.
 */
@Entity(tableName = "cash_accounts")
data class CashAccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: CashAccountType = CashAccountType.CASH,
    val balance: Double = 0.0,
    val icon: String = "account_balance_wallet", // Material icon name
    val color: Long = 0xFF4CAF50, // ARGB color as Long
    val isDefault: Boolean = false,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class CashAccountType(val index: Int, val displayName: String) {
    CASH(0, "Cash"),
    BANK(1, "Bank Account"),
    MOBILE_WALLET(2, "Mobile Wallet"),
    CREDIT_CARD(3, "Credit Card"),
    OTHER(4, "Other");

    companion object {
        fun fromIndex(index: Int): CashAccountType = when (index) {
            0 -> CASH
            1 -> BANK
            2 -> MOBILE_WALLET
            3 -> CREDIT_CARD
            else -> OTHER
        }
    }
}
