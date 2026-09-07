package com.khatabook.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════
 * ONBOARDING FLOW — Premium onboarding with HorizontalPager
 * ═══════════════════════════════════════════════════════════════════
 *
 * Layout:
 *   - Skip button (top right)
 *   - HorizontalPager (full screen)
 *   - Page indicator (below pager)
 *   - Continue/Get Started button (bottom)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingFlow(
    viewModel: OnboardingViewModel,
    onFinish: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val accentColor = Color(0xFF1A73E8)

    // HorizontalPager state
    val pagerState = rememberPagerState(
        pageCount = { state.totalPages }
    )

    // Sync pager with ViewModel
    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ═══════════════════════════════════════════════════
        // SKIP BUTTON (top right)
        // ═══════════════════════════════════════════════════
        AnimatedVisibility(
            visible = !state.isLastPage,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            TextButton(
                onClick = { viewModel.skip() }
            ) {
                Text(
                    text = "Skip →",
                    fontSize = 14.sp,
                    color = Color(0xFF999999)
                )
            }
        }

        // ═══════════════════════════════════════════════════
        // HORIZONTAL PAGER
        // ═══════════════════════════════════════════════════
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp, bottom = 200.dp),
            userScrollEnabled = true
        ) { page ->
            val screen = OnboardingScreens.allScreens[page]

            OnboardingScreenContent(
                screen = screen,
                selectedLanguageCode = state.selectedLanguageCode,
                onLanguageSelected = { viewModel.selectLanguage(it) }
            )
        }

        // ═══════════════════════════════════════════════════
        // BOTTOM SECTION (Indicator + Button)
        // ═══════════════════════════════════════════════════
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Page indicator
            OnboardingPageIndicator(
                currentPage = state.currentPage,
                totalPages = state.totalPages,
                activeColor = accentColor
            )

            // Continue / Get Started button
            Button(
                onClick = {
                    if (state.isLastPage) {
                        onFinish(state.selectedLanguageCode)
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(state.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                enabled = if (state.isFirstPage) state.isLanguageSelected else true
            ) {
                Text(
                    text = if (state.isLastPage) {
                        if (state.selectedLanguageCode == "ur") "شروع کریں"
                        else if (state.selectedLanguageCode == "ur-roman") "Shuru Karein"
                        else "Get Started"
                    } else {
                        if (state.selectedLanguageCode == "ur") "جاری رہیں"
                        else if (state.selectedLanguageCode == "ur-roman") "Jaari Rahein"
                        else "Continue"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
