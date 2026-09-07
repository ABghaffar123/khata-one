package com.khatabook.app.ui.screen.home

/**
 * Unified notification item for the combined bell icon.
 * Covers: Daily Summary, Customer Due/Overdue, Low Stock alerts.
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val title: String,
    val subtitle: String,
    val priority: Int = 0, // Higher = more urgent, shown first
    val timestamp: Long = System.currentTimeMillis(),
    val isHandled: Boolean = false
)

enum class NotificationType {
    DAILY_SUMMARY,
    CUSTOMER_DUE,
    LOW_STOCK
}
