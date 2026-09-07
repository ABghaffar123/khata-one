package com.khatabook.app.notification

import android.content.Context
import com.khatabook.app.data.entity.ProductEntity
import com.khatabook.app.data.repository.ProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks low stock products and triggers local notifications.
 *
 * - Observes product list from Room DB
 * - When a product falls below its threshold, fires a notification
 * - Maintains a reactive StateFlow of low stock count for UI badges
 */
@Singleton
class LowStockTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val productRepository: ProductRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    // Track which products we've already notified about (avoid duplicate alerts per session)
    private val notifiedProducts = mutableSetOf<Long>()

    init {
        // Create notification channel on startup
        NotificationHelper.createNotificationChannel(context)

        // Observe product changes and check for low stock
        scope.launch {
            productRepository.getAllActiveProducts().collect { products ->
                val lowStock = products.filter { it.isLowStock }
                _lowStockCount.value = lowStock.size

                // Check each low stock product
                lowStock.forEach { product ->
                    if (product.id !in notifiedProducts) {
                        // Only notify if stock changed recently (within last 5 seconds)
                        // This prevents spamming notifications on app start
                        val recentUpdate = System.currentTimeMillis() - product.updatedAt < 5000
                        if (recentUpdate) {
                            NotificationHelper.showLowStockNotification(
                                context = context,
                                productName = product.name,
                                currentQty = product.stockQuantity,
                                threshold = product.lowStockThreshold,
                                unit = product.unit.displayName
                            )
                            notifiedProducts.add(product.id)
                        }
                    }
                }

                // Clean up notified products that are no longer low stock
                val lowStockIds = lowStock.map { it.id }.toSet()
                notifiedProducts.retainAll(lowStockIds)

                // Show summary if multiple products are low
                if (lowStock.size >= 3) {
                    NotificationHelper.showLowStockSummaryNotification(context, lowStock.size)
                }
            }
        }
    }

    /**
     * Force-check a specific product after stock change.
     * Called by InventoryViewModel after deductStock/addStock operations.
     */
    fun checkProductStock(product: ProductEntity) {
        if (product.isLowStock && product.id !in notifiedProducts) {
            NotificationHelper.showLowStockNotification(
                context = context,
                productName = product.name,
                currentQty = product.stockQuantity,
                threshold = product.lowStockThreshold,
                unit = product.unit.displayName
            )
            notifiedProducts.add(product.id)
        } else if (!product.isLowStock) {
            // Product was restocked above threshold, remove from notified set
            notifiedProducts.remove(product.id)
        }
    }

    /**
     * Reset notification tracking (e.g., after app restart)
     */
    fun resetNotifications() {
        notifiedProducts.clear()
    }
}
