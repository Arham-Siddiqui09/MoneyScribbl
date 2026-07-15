package com.moneyscribbl.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.*

@Composable
fun OnboardingScreen1(
    onSkip: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val gradientColors = listOf(Color(0xFF5B4FE9), Color(0xFF8B5CF6))
    val gradientBrush = Brush.linearGradient(colors = gradientColors)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        // Hero Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(24.dp),
                    spotColor = VioletAccent.copy(alpha = 0.6f),
                    ambientColor = VioletAccent.copy(alpha = 0.1f)
                )
                .clip(RoundedCornerShape(24.dp))
                .background(gradientBrush),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "₹",
                color = Color.White,
                fontSize = 42.sp,
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Headline
        Box(contentAlignment = Alignment.Center) {
            // Scribble underline
            Canvas(
                modifier = Modifier
                    .offset(x = (-18).dp, y = 14.dp) // Adjusted to be under "the way you'd"
                    .size(width = 130.dp, height = 12.dp)
            ) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.4f)
                    quadraticTo(
                        size.width * 0.3f, size.height * 0.9f,
                        size.width * 0.5f, size.height * 0.6f
                    )
                    quadraticTo(
                        size.width * 0.7f, size.height * 0.2f,
                        size.width, size.height * 0.7f
                    )
                }
                drawPath(
                    path = path,
                    brush = gradientBrush,
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = AppTextPrimary)) {
                        append("Money, tracked\nthe way ")
                    }
                    withStyle(SpanStyle(brush = gradientBrush)) {
                        append("you'd\n")
                    }
                    withStyle(SpanStyle(brush = gradientBrush)) {
                        append("write it down")
                    }
                },
                fontFamily = SpaceGrotesk,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 42.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Subtext
        Text(
            text = "A calm, personal ledger for your\neveryday spending — built for how\nyou actually think about money.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = Color(0xFF6B6980),
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Ledger Preview Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(1.dp, Color(0xFFF1F1F5), RoundedCornerShape(16.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Chip
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFE8FF), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "☕", fontSize = 18.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Chai break",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = AppTextPrimary,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "−₹20",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = IncomeGreen
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen1() {
    moneyscribblTheme {
        OnboardingScreen1()
    }
}
