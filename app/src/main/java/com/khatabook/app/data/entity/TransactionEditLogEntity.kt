package com.khatabook.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class EditAction(val index: Int) {
    EDITED(0),
    DELETED(1),
    CREATED(2);

    companion object {
        fun fromIndex(index: Int): EditAction = entries[index]
    }
}

@Entity(
    tableName = "transaction_edit_logs",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("timestamp")]
)
data class TransactionEditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: Long,
    val action: EditAction,
    val fieldName: String? = null,       // e.g. "amount", "description", "date", "type"
    val oldValue: String? = null,        // serialized old value
    val newValue: String? = null,        // serialized new value
    val changedBy: String = "User",      // who made the change
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null            // optional note from user
)
