package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction categories organized into two visual groups.
 *
 * Customer Transactions:
 * - CREDIT_GIVEN: customer required (mandatory). Increases customer due.
 * - PAYMENT_RECEIVED: customer required (mandatory). Decreases customer due.
 * - CASH_SALE: customer optional. Adds to cash-in, no due impact.
 * - SALE_RETURN: customer optional. Reduces cash-in or customer due.
 *
 * Business Transactions:
 * - STOCK_PURCHASED: supplier optional. Increases inventory, increases supplier payable.
 * - PURCHASE_RETURN: supplier optional. Decreases supplier payable or adds cash back.
 * - EXPENSE: no customer/supplier. Decreases cash.
 * - OWNER_WITHDRAWAL: no customer/supplier. Decreases cash, tracked separately.
 * - OWNER_INVESTMENT: no customer/supplier. Increases cash, tracked separately.
 */
enum class TransactionType(val index: Int) {
    // ── Customer Transactions ──
    CREDIT_GIVEN(0),
    PAYMENT_RECEIVED(1),
    CASH_SALE(2),
    SALE_RETURN(3),

    // ── Business Transactions ──
    STOCK_PURCHASED(4),
    PURCHASE_RETURN(5),
    EXPENSE(6),
    OWNER_WITHDRAWAL(7),
    OWNER_INVESTMENT(8);

    /** Whether this type increases cash-in */
    val increasesCash: Boolean get() = this in listOf(PAYMENT_RECEIVED, CASH_SALE, PURCHASE_RETURN, OWNER_INVESTMENT)

    /** Whether this type decreases cash */
    val decreasesCash: Boolean get() = this in listOf(CREDIT_GIVEN, STOCK_PURCHASED, EXPENSE, OWNER_WITHDRAWAL)

    /** Whether this type affects customer due */
    val affectsCustomerDue: Boolean get() = this in listOf(CREDIT_GIVEN, PAYMENT_RECEIVED, SALE_RETURN)

    /** Whether this type affects supplier payable */
    val affectsSupplierPayable: Boolean get() = this in listOf(STOCK_PURCHASED, PURCHASE_RETURN)

    /** Whether customer selection is required */
    val customerRequired: Boolean get() = this in listOf(CREDIT_GIVEN, PAYMENT_RECEIVED)

    /** Whether customer selection is optional */
    val customerOptional: Boolean get() = this in listOf(CASH_SALE, SALE_RETURN)

    /** Whether supplier selection is optional */
    val supplierOptional: Boolean get() = this in listOf(STOCK_PURCHASED, PURCHASE_RETURN)

    /** Whether no party (customer/supplier) field should be shown */
    val noPartyField: Boolean get() = this in listOf(EXPENSE, OWNER_WITHDRAWAL, OWNER_INVESTMENT)

    /** Whether due date field should be shown (only for Credit Given) */
    val showsDueDate: Boolean get() = this == CREDIT_GIVEN

    // Legacy compatibility
    val isCredit: Boolean get() = this == CREDIT_GIVEN
    val isPayment: Boolean get() = this == PAYMENT_RECEIVED
}

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId"), Index("date")]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String = "",
    val type: TransactionType,
    val amount: Double,
    val description: String? = null,
    val dueDate: Long? = null,        // Optional due date for Credit Given
    val accountId: Long? = null,       // Cash account this transaction affects
    val supplierId: Long? = null,      // Supplier ID for Stock Purchased / Purchase Return
    val supplierName: String? = null,  // Supplier name for display
    val date: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isCredit: Boolean get() = type == TransactionType.CREDIT_GIVEN
    val isPayment: Boolean get() = type == TransactionType.PAYMENT_RECEIVED
}
