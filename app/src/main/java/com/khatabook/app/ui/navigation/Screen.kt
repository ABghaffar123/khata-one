package com.khatabook.app.ui.navigation

/**
 * Navigation routes for Khata One.
 *
 * Using sealed class for type-safe navigation.
 * Each screen has a route string for NavGraph.
 */
sealed class Screen(val route: String) {

    // ═══ Main Tabs (Bottom Navigation) ═══
    data object Home : Screen("home")
    data object Customers : Screen("customers")
    data object Inventory : Screen("inventory")
    data object Suppliers : Screen("suppliers")
    data object More : Screen("more")
    data object Khata : Screen("khata")
    data object Camera : Screen("camera")
    data object Settings : Screen("settings")
    data object Reports : Screen("reports")

    // ═══ Customer Flow ═══
    data object CustomerDetail : Screen("customer/{customerId}") {
        fun createRoute(customerId: Long) = "customer/$customerId"
    }
    data object AddCustomer : Screen("add_customer")
    data object EditCustomer : Screen("edit_customer/{customerId}") {
        fun createRoute(customerId: Long) = "edit_customer/$customerId"
    }

    // ═══ Transaction Flow ═══
    data object NewTransaction : Screen("new_transaction?customerId={customerId}") {
        fun createRoute(customerId: Long? = null) =
            if (customerId != null) "new_transaction?customerId=$customerId"
            else "new_transaction"
    }
    data object TransactionDetail : Screen("transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "transaction/$transactionId"
    }
    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }

    // ═══ Invoice ═══
    data object InvoicePreview : Screen("invoice/{transactionId}") {
        fun createRoute(transactionId: Long) = "invoice/$transactionId"
    }

    // ═══ Inventory Flow ═══
    data object AddProduct : Screen("add_product")
    data object ProductDetail : Screen("product/{productId}") {
        fun createRoute(productId: Long) = "product/$productId"
    }
    data object BarcodeScan : Screen("barcode_scan")
    data object LowStock : Screen("low_stock")

    // ═══ Supplier Flow ═══
    data object SupplierDetail : Screen("supplier/{supplierId}") {
        fun createRoute(supplierId: Long) = "supplier/$supplierId"
    }
    data object AddSupplier : Screen("add_supplier")

    // ═══ Khata Register ═══
    data object KhataRegister : Screen("khata_register")
    data object KhataDateDetail : Screen("khata_date/{date}") {
        fun createRoute(date: Long) = "khata_date/$date"
    }

    // ═══ OCR Flow ═══
    data object OcrScan : Screen("ocr_scan")
    data object OcrReview : Screen("ocr_review/{captureId}") {
        fun createRoute(captureId: Long) = "ocr_review/$captureId"
    }

    // ═══ Search ═══
    data object Search : Screen("search")

    // ═══ Reports ═══
    data object ReportDetail : Screen("report_detail/{reportType}") {
        fun createRoute(reportType: String) = "report_detail/$reportType"
    }
    data object DailySummary : Screen("daily_summary")
    data object DueCustomers : Screen("due_customers")

    // ═══ Settings Sub-screens ═══
    data object LanguageSettings : Screen("language_settings")
    data object ThemeSettings : Screen("theme_settings")
    data object SecuritySettings : Screen("security_settings")
    data object BackupCenter : Screen("backup_center")
    data object About : Screen("about")
    data object AccountSettings : Screen("account_settings")

    // ═══ Language Selection (First Launch) ═══
    data object LanguageSelection : Screen("language_selection")
}
