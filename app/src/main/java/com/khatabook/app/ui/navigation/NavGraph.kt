package com.khatabook.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.khatabook.app.ui.language.KhataLanguages
import com.khatabook.app.ui.language.LanguageManager
import com.khatabook.app.ui.responsive.LocalResponsiveTypography
import com.khatabook.app.ui.responsive.LocalWindowSize
import com.khatabook.app.ui.security.SecuritySettingsScreen
import com.khatabook.app.ui.security.SecurityViewModel
import com.khatabook.app.ui.theme.ThemeSelectionScreen
import com.khatabook.app.ui.theme.ThemeSelectionViewModel
import com.khatabook.app.ui.screen.customers.AddCustomerScreen
import com.khatabook.app.ui.screen.customers.CustomerDetailScreen
import com.khatabook.app.ui.screen.customers.CustomersScreen
import com.khatabook.app.ui.screen.home.HomeScreen
import com.khatabook.app.ui.screen.inventory.AddProductScreen
import com.khatabook.app.ui.screen.inventory.BarcodeScannerScreen
import com.khatabook.app.ui.screen.inventory.InventoryScreen
import com.khatabook.app.ui.screen.inventory.LowStockScreen
import com.khatabook.app.ui.screen.more.MoreScreen
import com.khatabook.app.ui.screen.reports.ReportsScreen
import com.khatabook.app.ui.screen.reports.DailySummaryScreen
import com.khatabook.app.ui.screen.settings.AccountSettingsScreen
import com.khatabook.app.ui.screen.settings.LanguageSettingsScreen
import com.khatabook.app.ui.screen.suppliers.AddSupplierScreen
import com.khatabook.app.ui.screen.suppliers.SupplierDetailScreen
import com.khatabook.app.ui.screen.suppliers.SuppliersScreen
import com.khatabook.app.ui.screen.invoice.InvoicePreviewScreen
import com.khatabook.app.ui.screen.transactions.AddTransactionScreen
import com.khatabook.app.ui.screen.transactions.EditTransactionScreen
import com.khatabook.app.ui.screen.home.DueCustomersListScreen

@Composable
fun KhataNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier,
    languageManager: LanguageManager? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(200)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = {
            fadeOut(animationSpec = tween(200)) + slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(300)
            )
        }
    ) {
        // ═══ Main Tabs ═══
        composable(Screen.Home.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            HomeScreen(
                onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                onNavigateToNewEntry = { navController.navigate(Screen.NewTransaction.createRoute()) },
                onNavigateToScan = { navController.navigate(Screen.Camera.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToDailySummary = { navController.navigate(Screen.DailySummary.route) },
                onNavigateToDueCustomers = { navController.navigate(Screen.DueCustomers.route) },
                onNavigateToLowStock = { navController.navigate(Screen.LowStock.route) }
            )
        }

        composable(Screen.Customers.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            CustomersScreen(
                onNavigateToDetail = { navController.navigate(Screen.CustomerDetail.createRoute(it)) },
                onNavigateToAdd = { navController.navigate(Screen.AddCustomer.route) }
            )
        }

        composable(Screen.Inventory.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            InventoryScreen(
                onNavigateToAdd = { navController.navigate(Screen.AddProduct.route) },
                onNavigateToDetail = { navController.navigate(Screen.ProductDetail.createRoute(it)) }
            )
        }

        composable(Screen.Suppliers.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            SuppliersScreen(
                onNavigateToDetail = { navController.navigate(Screen.SupplierDetail.createRoute(it)) },
                onNavigateToAdd = { navController.navigate(Screen.AddSupplier.route) }
            )
        }

        composable(Screen.Khata.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            AdaptivePlaceholderScreen("Khata Register", "📒")
        }

        composable(Screen.Camera.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            AdaptivePlaceholderScreen("Scan Khata", "📷")
        }

        composable(Screen.Reports.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            ReportsScreen(
                onNavigateToDailySummary = { navController.navigate(Screen.DailySummary.route) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DailySummary.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            DailySummaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.DueCustomers.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            DueCustomersListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCustomerDetail = { navController.navigate(Screen.CustomerDetail.createRoute(it)) }
            )
        }

        composable(Screen.More.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            val currentLang = languageManager?.currentLanguage?.collectAsState()?.value ?: "en"
            val langLabel = KhataLanguages.allLanguages.firstOrNull { it.code == currentLang }?.displayName ?: currentLang
            MoreScreen(
                currentLanguageLabel = langLabel,
                onOpenReports = { navController.navigate(Screen.Reports.route) },
                onOpenCashAccounts = { navController.navigate(Screen.AccountSettings.route) },
                onOpenDailySummary = { navController.navigate(Screen.DailySummary.route) },
                onOpenLanguage = { navController.navigate(Screen.LanguageSettings.route) },
                onOpenTheme = { navController.navigate(Screen.ThemeSettings.route) },
                onOpenSecurity = { navController.navigate(Screen.SecuritySettings.route) }
            )
        }

        // ═══ Customer Flow ═══
        composable(Screen.CustomerDetail.route, arguments = listOf(navArgument("customerId") { type = NavType.LongType })) {
            CustomerDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNewTransaction = { navController.navigate(Screen.NewTransaction.createRoute(it)) },
                onNavigateToEditTransaction = { navController.navigate(Screen.EditTransaction.createRoute(it)) }
            )
        }

        composable(Screen.AddCustomer.route) {
            AddCustomerScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.EditCustomer.route, arguments = listOf(navArgument("customerId") { type = NavType.LongType })) {
            AdaptivePlaceholderScreen("Edit Customer", "✏️")
        }

        // ═══ Transaction Flow ═══
        composable(Screen.NewTransaction.route, arguments = listOf(navArgument("customerId") { type = NavType.LongType; defaultValue = -1L })) {
            AddTransactionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToInvoice = { transactionId ->
                    navController.navigate(Screen.InvoicePreview.createRoute(transactionId))
                }
            )
        }

        composable(Screen.EditTransaction.route, arguments = listOf(navArgument("transactionId") { type = NavType.LongType })) {
            EditTransactionScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══ Invoice ═══
        composable(Screen.InvoicePreview.route, arguments = listOf(navArgument("transactionId") { type = NavType.LongType })) {
            InvoicePreviewScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ═══ Inventory Flow ═══
        composable(Screen.AddProduct.route) {
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScanBarcode = { navController.navigate(Screen.BarcodeScan.route) }
            )
        }

        composable(Screen.BarcodeScan.route) {
            BarcodeScannerScreen(
                onNavigateBack = { navController.popBackStack() },
                onBarcodeScanned = { barcode ->
                    // Pop back to AddProduct and pass barcode
                    navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.ProductDetail.route, arguments = listOf(navArgument("productId") { type = NavType.LongType })) {
            AdaptivePlaceholderScreen("Product Detail", "📦")
        }

        composable(Screen.LowStock.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            LowStockScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ═══ Supplier Flow ═══
        composable(Screen.SupplierDetail.route, arguments = listOf(navArgument("supplierId") { type = NavType.LongType })) {
            SupplierDetailScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.AddSupplier.route) {
            AddSupplierScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ═══ OCR Flow ═══
        composable(Screen.OcrScan.route) { AdaptivePlaceholderScreen("OCR Scan", "📷") }
        composable(Screen.OcrReview.route, arguments = listOf(navArgument("captureId") { type = NavType.LongType })) {
            AdaptivePlaceholderScreen("OCR Review", "📝")
        }

        // ═══ Search ═══
        composable(Screen.Search.route) { AdaptivePlaceholderScreen("Search", "🔍") }

        // ═══ Settings Sub-screens ═══
        composable(Screen.LanguageSettings.route) {
            val currentLang = languageManager?.currentLanguage?.collectAsState()?.value ?: "en"
            LanguageSettingsScreen(
                currentLanguage = currentLang,
                onLanguageSelected = { code -> languageManager?.setLanguage(code) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ThemeSettings.route) {
            ThemeSelectionScreen(
                viewModel = viewModel(),
                onBack = { navController.popBackStack() },
                onApply = { navController.popBackStack() }
            )
        }
        composable(Screen.SecuritySettings.route) {
            SecuritySettingsScreen(
                viewModel = viewModel(),
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.BackupCenter.route) { AdaptivePlaceholderScreen("Backup Center", "💾") }
        composable(Screen.About.route) { AdaptivePlaceholderScreen("About", "ℹ️") }

        composable(Screen.AccountSettings.route, enterTransition = { fadeIn(tween(200)) }, exitTransition = { fadeOut(tween(200)) }) {
            AccountSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun AdaptivePlaceholderScreen(title: String, icon: String = "🚧") {
    val typography = LocalResponsiveTypography.current
    val windowSize = LocalWindowSize.current
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
            text = "$icon\n$title\n\nComing Soon",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = if (windowSize.isCompact) typography.h3 else typography.h2),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            textAlign = TextAlign.Center
        )
    }
}
