package com.khatabook.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.khatabook.app.notification.LowStockTracker
import com.khatabook.app.notification.NotificationHelper
import com.khatabook.app.notification.DailySummaryReceiver
import com.khatabook.app.notification.DailySummaryManager
import com.khatabook.app.ui.components.SuccessToastHost
import com.khatabook.app.ui.language.LanguageManager
import com.khatabook.app.ui.navigation.KhataAdaptiveNavigation
import com.khatabook.app.ui.navigation.KhataNavGraph
import com.khatabook.app.ui.navigation.Screen
import com.khatabook.app.ui.responsive.LocalWindowSize
import com.khatabook.app.ui.theme.KhataTheme
import com.khatabook.app.security.SecurityManager
import com.khatabook.app.security.LockScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity — Hosts the entire Compose UI with adaptive layout.
 *
 * LIFECYCLE:
 * - Created on cold start (after SplashActivity)
 * - Resumes directly on warm resume (no splash)
 * - Handles config changes (rotation, language)
 *
 * ADAPTIVE LAYOUT:
 * ┌─────────────────────────────────────────────────────┐
 * │ COMPACT/MEDIUM (< 840dp):                          │
 * │ ┌───────────────────────────────────────────────┐  │
 * │ │              Content                          │  │
 * │ │         (NavGraph screens)                    │  │
 * │ ├───────────────────────────────────────────────┤  │
 * │ │  Home | Customers | Khata | Scan | Settings   │  │
 * │ └───────────────────────────────────────────────┘  │
 * ├─────────────────────────────────────────────────────┤
 * │ EXPANDED/LARGE (>= 840dp):                         │
 * │ ┌────┬──────────────────────────────────────────┐  │
 * │ │    │              Content                      │  │
 * │ │ H  │         (NavGraph screens)                │  │
 * │ │ C  │                                           │  │
 * │ │ K  │                                           │  │
 * │ │ S  │                                           │  │
 * │ │    │                                           │  │
 * │ └────┴──────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────┘
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var lowStockTracker: LowStockTracker
    @Inject lateinit var securityManager: SecurityManager
    private var isLocked = mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not — we handle gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channels
        NotificationHelper.createNotificationChannel(this)
        NotificationHelper.createDailySummaryChannel(this)

        // Schedule daily summary alarm
        if (DailySummaryManager.isSummaryEnabled(this)) {
            DailySummaryReceiver.scheduleAlarm(this)
        }

        // Request POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Initialize lock state
        isLocked.value = securityManager.isPinEnabled && securityManager.isLocked

        // Lifecycle observer for auto-lock
        lifecycle.addObserver(object : LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> securityManager.onAppBackground()
                Lifecycle.Event.ON_RESUME -> {
                    if (securityManager.onAppForeground()) {
                        isLocked.value = true
                    }
                }
                else -> {}
            }
        })

        setContent {
            val languageManager: LanguageManager = hiltViewModel()
            val currentLanguage by languageManager.currentLanguage

            // Observe low stock count for badge
            val lowStockCount by lowStockTracker.lowStockCount.collectAsState()
            val locked by isLocked

            // Handle deep link from notification
            LaunchedEffect(intent) {
                if (intent?.getStringExtra("navigate_to") == "low_stock") {
                    // Navigation will be handled by NavGraph on next recomposition
                }
            }

            KhataTheme(language = currentLanguage) {
                if (locked && securityManager.isPinEnabled) {
                    LockScreen(
                        securityManager = securityManager,
                        onUnlocked = {
                            securityManager.unlock()
                            isLocked.value = false
                        }
                    )
                } else {
                    KhataApp(
                        languageManager = languageManager,
                        lowStockCount = lowStockCount
                    )
                }
            }
        }
    }
}

/**
 * Main app composable with adaptive navigation.
 */
@Composable
private fun KhataApp(
    languageManager: LanguageManager,
    lowStockCount: Int = 0
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val windowSize = LocalWindowSize.current

    // Routes where navigation should be visible
    val showNavigation = currentRoute in listOf(
        Screen.Home.route,
        Screen.Customers.route,
        Screen.Khata.route,
        Screen.Camera.route,
        Screen.More.route,
        Screen.Inventory.route,
        Screen.Suppliers.route
    )

    // Routes that are full-screen (no navigation)
    val isFullScreen = currentRoute in listOf(
        Screen.LanguageSelection.route
    )

    Box(modifier = Modifier.fillMaxSize()) {
        if (windowSize.showNavigationRail && showNavigation && !isFullScreen) {
            // ═══ EXPANDED/LARGE: Navigation Rail Layout ═══
            Row(modifier = Modifier.fillMaxSize()) {
                KhataAdaptiveNavigation(navController = navController, lowStockCount = lowStockCount)
                Scaffold(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) { innerPadding ->
                    KhataNavGraph(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(innerPadding),
                        languageManager = languageManager
                    )
                }
            }
        } else {
            // ═══ COMPACT/MEDIUM: Bottom Bar Layout ═══
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    if (showNavigation && !isFullScreen) {
                        KhataAdaptiveNavigation(navController = navController, lowStockCount = lowStockCount)
                    }
                }
            ) { innerPadding ->
                KhataNavGraph(
                    navController = navController,
                    startDestination = Screen.Home.route,
                    modifier = Modifier.padding(innerPadding),
                    languageManager = languageManager
                )
            }
        }

        // Global success toast overlay (floats above every screen)
        SuccessToastHost()
    }
}
