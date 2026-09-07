package com.khatabook.app.ui.screen.reports

import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.CashAccountEntity
import com.khatabook.app.data.entity.TransactionEntity
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CashAccountRepository
import com.khatabook.app.data.repository.ProductRepository
import com.khatabook.app.data.repository.SupplierTransactionRepository
import com.khatabook.app.data.repository.TransactionRepository
import com.khatabook.app.util.PkDateTime
import com.khatabook.app.util.ReportExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

enum class DateRange {
    TODAY, THIS_WEEK, THIS_MONTH, CUSTOM
}

data class CashBookState(
    val totalCashSales: Double = 0.0,
    val totalPaymentsReceived: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalCreditGiven: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalPaymentsToSuppliers: Double = 0.0,
    val openingCash: Double = 0.0,
    val stockValue: Double = 0.0,
    val potentialRevenue: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val todayTransactions: Int = 0,
    val totalTransactionCount: Int = 0,
    val dateRange: DateRange = DateRange.TODAY,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val selectedAccountId: Long? = null, // null = All Accounts
    val searchQuery: String = "",
    val selectedTransactionType: TransactionType? = null, // null = All Types
    val isLoading: Boolean = true
) {
    /** Closing cash = Opening + Cash In - Cash Out */
    val closingCash: Double
        get() = openingCash + (totalCashSales + totalPaymentsReceived) - (totalExpenses + totalPaymentsToSuppliers)

    /** Cash in hand for display */
    val cashInHand: Double
        get() = closingCash
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    application: Application,
    private val transactionRepository: TransactionRepository,
    private val supplierTransactionRepository: SupplierTransactionRepository,
    private val productRepository: ProductRepository,
    private val cashAccountRepository: CashAccountRepository
) : AndroidViewModel(application) {

    private val _cashBook = MutableStateFlow(CashBookState())
    val cashBook: StateFlow<CashBookState> = _cashBook.asStateFlow()

    private val _accounts = MutableStateFlow<List<CashAccountEntity>>(emptyList())
    val accounts: StateFlow<List<CashAccountEntity>> = _accounts.asStateFlow()

    private val _filteredTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val filteredTransactions: StateFlow<List<TransactionEntity>> = _filteredTransactions.asStateFlow()

    private val prefs = application.getSharedPreferences("khata_prefs", Context.MODE_PRIVATE)

    init {
        // Load accounts
        viewModelScope.launch {
            cashAccountRepository.getAllActiveAccounts().collect { accountList ->
                _accounts.value = accountList
            }
        }
        loadCashBook()
    }

    fun setDateRange(range: DateRange) {
        _cashBook.update { it.copy(dateRange = range, customStartDate = null, customEndDate = null) }
        loadCashBook()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _cashBook.update { it.copy(dateRange = DateRange.CUSTOM, customStartDate = start, customEndDate = end) }
        loadCashBook()
    }

    fun setSelectedAccount(accountId: Long?) {
        _cashBook.update { it.copy(selectedAccountId = accountId) }
        loadCashBook()
    }

    fun setSearchQuery(query: String) {
        _cashBook.update { it.copy(searchQuery = query) }
        loadCashBook()
    }

    fun setSelectedTransactionType(type: TransactionType?) {
        _cashBook.update { it.copy(selectedTransactionType = type) }
        loadCashBook()
    }

    fun updateOpeningCash(amount: Double) {
        _cashBook.update { it.copy(openingCash = amount) }
        // Persist to SharedPreferences using PKT today key
        val todayKey = "opening_cash_${PkDateTime.pktTodayKey()}"
        prefs.edit().putFloat(todayKey, amount.toFloat()).apply()
    }

    private fun loadCashBook() {
        viewModelScope.launch {
            val range = getDateRange()

            combine(
                transactionRepository.getRecentTransactions(500),
                supplierTransactionRepository.getTodayPurchases(),
                supplierTransactionRepository.getTodayPayments(),
                productRepository.getTotalStockValue(),
                productRepository.getTotalPotentialRevenue()
            ) { txns, todayPurchases, todayPayments, stockVal, revenue ->
                var dateTxns = txns.filter { it.date in range.first..range.second }

                // Filter by account if selected
                val selectedAccountId = _cashBook.value.selectedAccountId
                if (selectedAccountId != null) {
                    dateTxns = dateTxns.filter { it.accountId == selectedAccountId }
                }

                // ═══ Search + Type Filter ═══
                val query = _cashBook.value.searchQuery.trim().lowercase()
                val typeFilter = _cashBook.value.selectedTransactionType
                var displayTxns = dateTxns
                if (query.isNotEmpty()) {
                    displayTxns = displayTxns.filter {
                        it.customerName.lowercase().contains(query) ||
                        (it.description?.lowercase()?.contains(query) == true)
                    }
                }
                if (typeFilter != null) {
                    displayTxns = displayTxns.filter { it.type == typeFilter }
                }
                _filteredTransactions.value = displayTxns

                val cashSales = dateTxns.filter { it.type == TransactionType.CASH_SALE }.sumOf { it.amount }
                val paymentsReceived = dateTxns.filter { it.type == TransactionType.PAYMENT_RECEIVED }.sumOf { it.amount }
                val expenses = dateTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val creditGiven = dateTxns.filter { it.type == TransactionType.CREDIT_GIVEN }.sumOf { it.amount }

                val stockValue = stockVal ?: 0.0
                val potentialRevenue = revenue ?: 0.0
                val estimatedProfit = potentialRevenue - stockValue

                // Load opening cash from prefs using PKT today key
                val todayKey = "opening_cash_${PkDateTime.pktTodayKey()}"
                val savedOpeningCash = prefs.getFloat(todayKey, 0f).toDouble()

                CashBookState(
                    totalCashSales = cashSales,
                    totalPaymentsReceived = paymentsReceived,
                    totalExpenses = expenses,
                    totalCreditGiven = creditGiven,
                    totalPurchases = todayPurchases ?: 0.0,
                    totalPaymentsToSuppliers = todayPayments ?: 0.0,
                    openingCash = savedOpeningCash,
                    stockValue = stockValue,
                    potentialRevenue = potentialRevenue,
                    estimatedProfit = estimatedProfit,
                    todayTransactions = dateTxns.size,
                    totalTransactionCount = dateTxns.size,
                    dateRange = _cashBook.value.dateRange,
                    customStartDate = _cashBook.value.customStartDate,
                    customEndDate = _cashBook.value.customEndDate,
                    selectedAccountId = _cashBook.value.selectedAccountId,
                    searchQuery = _cashBook.value.searchQuery,
                    selectedTransactionType = _cashBook.value.selectedTransactionType,
                    isLoading = false
                )
            }.collect { state ->
                _cashBook.value = state
            }
        }
    }

    /**
     * Get date range based on selected filter, all in PKT timezone.
     * TODAY = start/end of today in Asia/Karachi
     * THIS_WEEK = start/end of current week in Asia/Karachi
     * THIS_MONTH = start/end of current month in Asia/Karachi
     * CUSTOM = user-selected range (already UTC millis)
     */
    private fun getDateRange(): Pair<Long, Long> {
        return when (_cashBook.value.dateRange) {
            DateRange.TODAY -> PkDateTime.pktTodayRange()
            DateRange.THIS_WEEK -> PkDateTime.pktWeekRange()
            DateRange.THIS_MONTH -> PkDateTime.pktMonthRange()
            DateRange.CUSTOM -> {
                val start = _cashBook.value.customStartDate ?: PkDateTime.nowUtc()
                val end = _cashBook.value.customEndDate ?: PkDateTime.nowUtc()
                Pair(start, end)
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EXPORT
    // ═══════════════════════════════════════════════════════════════

    private val shopName: String
        get() = prefs.getString("shop_name", "Khata One") ?: "Khata One"

    /**
     * Export current report as PDF.
     * Returns the file and share intent.
     */
    fun exportPdf(): Pair<File, Intent>? {
        val state = _cashBook.value
        if (state.isLoading) return null

        return try {
            val file = ReportExporter.generatePdf(getApplication(), state, shopName)
            val intent = ReportExporter.createShareIntent(
                getApplication(), file, "application/pdf"
            )
            Pair(file, intent)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Export current report as CSV.
     * Returns the file and share intent.
     */
    fun exportCsv(): Pair<File, Intent>? {
        val state = _cashBook.value
        if (state.isLoading) return null

        return try {
            val file = ReportExporter.generateCsv(getApplication(), state, shopName)
            val intent = ReportExporter.createShareIntent(
                getApplication(), file, "text/csv"
            )
            Pair(file, intent)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get PDF file for opening directly.
     */
    fun getPdfFile(): File? {
        val state = _cashBook.value
        if (state.isLoading) return null
        return try {
            ReportExporter.generatePdf(getApplication(), state, shopName)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get CSV file for opening directly.
     */
    fun getCsvFile(): File? {
        val state = _cashBook.value
        if (state.isLoading) return null
        return try {
            ReportExporter.generateCsv(getApplication(), state, shopName)
        } catch (e: Exception) {
            null
        }
    }
}
