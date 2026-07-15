package com.moneyscribbl.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.*

@Composable
fun OnboardingScreen2(
    onSkip: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val gradientColors = listOf(Color(0xFF5B4FE9), Color(0xFF8B5CF6))
    val gradientBrush = Brush.linearGradient(colors = gradientColors)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Eyebrow badge
        Surface(
            color = Color(0xFFDFF9F4),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.FlashOn,
                    contentDescription = null,
                    tint = Color(0xFF0F9D8C),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Unique to Money Scribbl",
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.5.sp,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF0F9D8C)
                )
            }
        }

        // Hero Visual
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .align(Alignment.CenterHorizontally)
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.QrCode,
                contentDescription = "QR Code Scanner",
                tint = Color.White,
                modifier = Modifier.size(92.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Heading
        Text(
            text ="Pay via UPI —\nlogged the moment you do",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = AppTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Body Text
        Text(
            text = "Tap Pay, and we open your UPI app to scan and complete the payment. The moment you're done, it's already in your ledger — no manual entry.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = Color(0xFF6B6980),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Primary Feature Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF2DD4BF).copy(alpha = 0.3f),
                    ambientColor = Color(0xFF2DD4BF).copy(alpha = 0.1f)
                )
                .border(1.5.dp, Color(0xFF2DD4BF), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Chip
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFDFF9F4), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FlashOn,
                        contentDescription = null,
                        tint = Color(0xFF0F9D8C),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Works with your apps",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AppTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pay through the UPI app you already use — we just log it for you.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF6B6980),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Secondary Feature Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F1F5), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Chip
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEFE8FF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = Color(0xFF5B4FE9),
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Local by default",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = AppTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tracking and budgets work with no internet. UPI payments open your own payment app when you choose to pay.",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = Color(0xFF6B6980),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen2() {
    moneyscribblTheme {
        OnboardingScreen2()
    }
}
