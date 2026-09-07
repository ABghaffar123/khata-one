package com.khatabook.app.ui.screen.reports

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khatabook.app.data.entity.TransactionType
import com.khatabook.app.data.repository.CashAccountRepository
import com.khatabook.app.data.repository.ProductRepository
import com.khatabook.app.data.repository.SupplierTransactionRepository
import com.khatabook.app.data.repository.TransactionRepository
import com.khatabook.app.notification.DailySummaryManager
import com.khatabook.app.util.PkDateTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class DailySummaryState(
    val date: String = "",
    val openingCash: Double = 0.0,
    val cashIn: Double = 0.0,
    val cashOut: Double = 0.0,
    val cashInHand: Double = 0.0,
    val cashSales: Double = 0.0,
    val paymentsReceived: Double = 0.0,
    val creditGiven: Double = 0.0,
    val expenses: Double = 0.0,
    val isDownloaded: Boolean = false,
    val downloadedHistory: List<DailySummaryManager.SummaryRecord> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DailySummaryViewModel @Inject constructor(
    application: Application,
    private val transactionRepository: TransactionRepository,
    private val supplierTransactionRepository: SupplierTransactionRepository,
    private val productRepository: ProductRepository,
    private val cashAccountRepository: CashAccountRepository
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(DailySummaryState())
    val state: StateFlow<DailySummaryState> = _state.asStateFlow()

    private val prefs = application.getSharedPreferences("khata_prefs", Context.MODE_PRIVATE)

    init {
        loadSummary()
        loadHistory()
    }

    fun loadSummary() {
        viewModelScope.launch {
            val today = PkDateTime.pktTodayRange()
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())

            combine(
                transactionRepository.getRecentTransactions(500),
                supplierTransactionRepository.getTodayPurchases(),
                supplierTransactionRepository.getTodayPayments(),
                productRepository.getTotalStockValue(),
                productRepository.getTotalPotentialRevenue()
            ) { txns, todayPurchases, todayPayments, stockVal, revenue ->
                val dateTxns = txns.filter { it.date in today.first..today.second }

                val cashSales = dateTxns.filter { it.type == TransactionType.CASH_SALE }.sumOf { it.amount }
                val paymentsReceived = dateTxns.filter { it.type == TransactionType.PAYMENT_RECEIVED }.sumOf { it.amount }
                val expenses = dateTxns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                val creditGiven = dateTxns.filter { it.type == TransactionType.CREDIT_GIVEN }.sumOf { it.amount }

                val todayKey = "opening_cash_${PkDateTime.pktTodayKey()}"
                val openingCash = prefs.getFloat(todayKey, 0f).toDouble()
                val cashIn = cashSales + paymentsReceived
                val cashOut = expenses + (todayPayments ?: 0.0)
                val cashInHand = openingCash + cashIn - cashOut

                DailySummaryState(
                    date = dateStr,
                    openingCash = openingCash,
                    cashIn = cashIn,
                    cashOut = cashOut,
                    cashInHand = cashInHand,
                    cashSales = cashSales,
                    paymentsReceived = paymentsReceived,
                    creditGiven = creditGiven,
                    expenses = expenses,
                    isDownloaded = DailySummaryManager.isSummaryDownloadedToday(getApplication()),
                    isLoading = false
                )
            }.collect { state ->
                _state.value = state
            }
        }
    }

    private fun loadHistory() {
        val history = DailySummaryManager.getDownloadedSummaries(getApplication())
        _state.update { it.copy(downloadedHistory = history) }
    }

    fun downloadPdf(): File? {
        val s = _state.value
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = DailySummaryManager.generatePdf(
            getApplication(), s.date, s.openingCash, s.cashIn, s.cashOut, s.cashInHand,
            s.cashSales, s.paymentsReceived, s.creditGiven, s.expenses
        )
        if (file != null) {
            DailySummaryManager.markSummaryDownloaded(getApplication(), dateKey, file.absolutePath, "pdf")
            _state.update { it.copy(isDownloaded = true) }
            loadHistory()
        }
        return file
    }

    fun downloadImage(): File? {
        val s = _state.value
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val file = DailySummaryManager.generateImage(
            getApplication(), s.date, s.openingCash, s.cashIn, s.cashOut, s.cashInHand,
            s.cashSales, s.paymentsReceived, s.creditGiven, s.expenses
        )
        if (file != null) {
            DailySummaryManager.markSummaryDownloaded(getApplication(), dateKey, file.absolutePath, "png")
            _state.update { it.copy(isDownloaded = true) }
            loadHistory()
        }
        return file
    }

    fun refresh() {
        loadSummary()
        loadHistory()
    }
}
