package com.moneyscribbl.ui.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.AppSurface
import com.moneyscribbl.ui.theme.SpaceGrotesk
import com.moneyscribbl.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingFlowScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    val name by viewModel.name.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()

    val gradientColors = listOf(Color(0xFF5B4FE9), Color(0xFF8B5CF6))
    val gradientBrush = Brush.linearGradient(colors = gradientColors)

    // Calculate progress (0f to 1f)
    val progress = (pagerState.currentPage + 1) / 4f
    // Animate progress
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppSurface)
            .systemBarsPadding()
            .padding(top = 8.dp)
    ) {
        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(4.dp)
                .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .background(gradientBrush, RoundedCornerShape(2.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            userScrollEnabled = true
        ) { page ->
            when (page) {
                0 -> OnboardingScreen1()
                1 -> OnboardingScreen2()
                2 -> OnboardingScreen3()
                3 -> OnboardingScreen4(
                    name = name,
                    onNameChange = { viewModel.updateName(it) },
                    profileImageUri = profileImageUri,
                    onProfileImageSelected = { viewModel.updateProfileImage(it) }
                )
            }
        }

        // Bottom Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Page Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isActive = i == pagerState.currentPage
                    val width by animateDpAsState(targetValue = if (isActive) 24.dp else 6.dp, label = "dot_width")
                    val color by animateColorAsState(targetValue = if (isActive) Color(0xFF5B4FE9) else Color(0xFFE2E8F0), label = "dot_color")

                    Box(
                        modifier = Modifier
                            .width(width)
                            .height(6.dp)
                            .background(color, RoundedCornerShape(3.dp))
                    )
                    if (i < 3) Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA Button
            val buttonText = if (pagerState.currentPage == 0) "Get Started" else "Continue"
            val onBtnClick: () -> Unit = {
                if (pagerState.currentPage < 3) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                } else {
                    viewModel.completeOnboarding(onFinish)
                }
            }

            Button(
                onClick = onBtnClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = Color(0xFF8B5CF6).copy(alpha = 0.4f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = buttonText,
                        fontFamily = SpaceGrotesk,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
