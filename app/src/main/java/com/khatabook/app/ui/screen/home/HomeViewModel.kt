package com.khatabook.app.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CustomerEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CustomerRepository
import com.khatabook.app.data.repository.ProductRepository
import com.khatabook.app.data.repository.TransactionRepository
import com.khatabook.app.util.PkDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val totalDues: Double = 0.0,
    val todayCollected: Double = 0.0,
    val thisMonthTotal: Double = 0.0,
    val topDebtors: List<CustomerEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val overdueCount: Int = 0,
    val notifications: List<NotificationItem> = emptyList(),
    val notificationBadgeCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val customerRepository: CustomerRepository,
    private val transactionRepository: TransactionRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Customer lookup map so UI can show photos in Recent Activity
    private val _customerMap = MutableStateFlow<Map<Long, CustomerEntity>>(emptyMap())

    fun customerById(id: Long): CustomerEntity? = _customerMap.value[id]

    init {
        // ═══ Load customers map for avatar lookups ═══
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                _customerMap.value = customers.associateBy { it.id }
            }
        }

        // ═══ Load main stats ═══
        combine(
            customerRepository.getTotalDues(),
            transactionRepository.getTodayCollected(),
            transactionRepository.getThisMonthTotal(),
            customerRepository.getTopDebtors(),
            transactionRepository.getRecentTransactions(10)
        ) { dues, today, month, debtors, recent ->
            HomeUiState(
                totalDues = dues ?: 0.0,
                todayCollected = today ?: 0.0,
                thisMonthTotal = month ?: 0.0,
                topDebtors = debtors,
                recentTransactions = recent,
                isLoading = false
            )
        }.catch { e ->
            emit(HomeUiState(isLoading = false))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        ).let { flow ->
            viewModelScope.launch {
                flow.collect { state ->
                    _uiState.update { current ->
                        current.copy(
                            totalDues = state.totalDues,
                            todayCollected = state.todayCollected,
                            thisMonthTotal = state.thisMonthTotal,
                            topDebtors = state.topDebtors,
                            recentTransactions = state.recentTransactions,
                            isLoading = state.isLoading
                        )
                    }
                }
            }
        }

        // ═══ Load unified notifications ═══
        loadNotifications()
    }

    private fun loadNotifications() {
        // Observe all transactions for due customer notifications
        viewModelScope.launch {
            combine(
                transactionRepository.getAllTransactions(),
                customerRepository.getTopDebtors(),
                productRepository.getAllActiveProducts()
            ) { transactions, debtors, products ->
                buildNotificationList(transactions, debtors, products)
            }.catch { e ->
                emit(emptyList())
            }.collect { notifications ->
                _uiState.update { it.copy(
                    notifications = notifications,
                    notificationBadgeCount = notifications.size,
                    overdueCount = notifications.count { n -> n.type == NotificationType.CUSTOMER_DUE }
                ) }
            }
        }
    }

    private fun buildNotificationList(
        transactions: List<TransactionEntity>,
        debtors: List<CustomerEntity>,
        products: List<com.khatabook.app.data.entity.ProductEntity>
    ): List<NotificationItem> {
        val notifications = mutableListOf<NotificationItem>()
        val nowUtc = PkDateTime.nowUtc()

        // ═══ 1. Daily Summary notification (after 9 PM, if not downloaded) ═══
        val (hour, minute) = Pair(21, 0) // Default 9 PM — matches DailySummaryManager
        val now = java.util.Calendar.getInstance()
        val isPastSummaryTime = now.get(java.util.Calendar.HOUR_OF_DAY) > hour ||
            (now.get(java.util.Calendar.HOUR_OF_DAY) == hour && now.get(java.util.Calendar.MINUTE) >= minute)

        if (isPastSummaryTime) {
            notifications.add(NotificationItem(
                id = "daily_summary",
                type = NotificationType.DAILY_SUMMARY,
                title = "\uD83D\uDD14 Daily Summary Ready",
                subtitle = "Tap to view your daily business summary",
                priority = 10,
                timestamp = nowUtc
            ))
        }

        // ═══ 2. Customer due / overdue notifications ═══
        val dueCustomerMap = mutableMapOf<Long, MutableList<TransactionEntity>>()
        for (txn in transactions) {
            if (txn.type == TransactionType.CREDIT_GIVEN && txn.dueDate != null) {
                dueCustomerMap.getOrPut(txn.customerId) { mutableListOf() }.add(txn)
            }
        }

        for (customer in debtors) {
            val customerTxns = dueCustomerMap[customer.id] ?: emptyList()
            val overdueTxns = customerTxns.filter { it.dueDate != null && it.dueDate <= nowUtc }
            val totalDue = customer.balance

            if (overdueTxns.isNotEmpty()) {
                // Most overdue first
                val mostOverdue = overdueTxns.maxByOrNull { it.dueDate!! }!!
                val daysOverdue = ((nowUtc - mostOverdue.dueDate!!) / (1000 * 60 * 60 * 24)).toInt()
                notifications.add(NotificationItem(
                    id = "due_customer_${customer.id}",
                    type = NotificationType.CUSTOMER_DUE,
                    title = "\u23F0 ${customer.name}'s due date passed",
                    subtitle = "Rs ${String.format("%,.0f", totalDue)} overdue ($daysOverdue days)",
                    priority = 100 + daysOverdue, // More overdue = higher priority
                    timestamp = mostOverdue.dueDate!!
                ))
            } else if (customer.hasDue) {
                // Has dues but not overdue yet
                notifications.add(NotificationItem(
                    id = "due_customer_${customer.id}",
                    type = NotificationType.CUSTOMER_DUE,
                    title = "\uD83D\uDCB3 ${customer.name} — due pending",
                    subtitle = "Rs ${String.format("%,.0f", totalDue)} due",
                    priority = 50,
                    timestamp = customerTxns.maxByOrNull { it.date }?.date ?: nowUtc
                ))
            }
        }

        // ═══ 3. Low stock notifications ═══
        val lowStockProducts = products.filter { it.isLowStock }
        for (product in lowStockProducts) {
            notifications.add(NotificationItem(
                id = "low_stock_${product.id}",
                type = NotificationType.LOW_STOCK,
                title = "\u26A0\uFE0F Low Stock: ${product.name}",
                subtitle = "Only ${product.stockQuantity.toLong()} ${product.unit.displayName} left",
                priority = 80,
                timestamp = product.updatedAt
            ))
        }

        // Sort: most urgent first
        return notifications.sortedByDescending { it.priority }
    }

    /**
     * Dismiss a notification from the list.
     */
    fun dismissNotification(notificationId: String) {
        _uiState.update { state ->
            val updated = state.notifications.filter { it.id != notificationId }
            state.copy(
                notifications = updated,
                notificationBadgeCount = updated.size
            )
        }
    }
}
