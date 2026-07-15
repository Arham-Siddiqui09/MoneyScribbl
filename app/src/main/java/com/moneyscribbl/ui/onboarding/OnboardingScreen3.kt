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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.*

@Composable
fun OnboardingScreen3(
    onSkip: () -> Unit = {},
    onNext: () -> Unit = {}
) {
    val gradientColors = listOf(Color(0xFF5B4FE9), Color(0xFF8B5CF6))
    val gradientBrush = Brush.linearGradient(colors = gradientColors)

    val colorFood = Color(0xFF5B4FE9)
    val colorTravel = Color(0xFF8B5CF6)
    val colorOther = Color(0xFFC4B5FD)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {


        // Heading
        Text(
            text = "See where it\nactually goes",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            color = AppTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Body Text
        Text(
            text = "Log an expense in two taps. MoneyScribbl\nsorts it into categories and shows you\nthe pattern.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = Color(0xFF6B6980),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Donut Chart Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFF1F1F5), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(80.dp)) {
                        val strokeWidth = 14.dp.toPx()
                        
                        // Food Segment
                        drawArc(
                            color = colorFood,
                            startAngle = -90f,
                            sweepAngle = 170f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        
                        // Travel Segment
                        drawArc(
                            color = colorTravel,
                            startAngle = 100f,
                            sweepAngle = 90f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        
                        // Other Segment
                        drawArc(
                            color = colorOther,
                            startAngle = 210f,
                            sweepAngle = 40f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(32.dp))
                
                // Legend
                Column(modifier = Modifier.weight(1f)) {
                    LegendItem(color = colorFood, label = "Shopping", amount = "₹4,200")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = colorTravel, label = "Travel", amount = "₹2,600")
                    Spacer(modifier = Modifier.height(12.dp))
                    LegendItem(color = colorOther, label = "Other", amount = "₹1,150")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Feature Row
        FeatureCard(
            title = "Set a monthly goal",
            description = "Get a gentle nudge before you\noverspend.",
            iconSymbol = "⏱" // Clock icon
        )

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun LegendItem(color: Color, label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            fontFamily = Inter,
            fontSize = 13.sp,
            color = Color(0xFF6B6980),
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = amount,
            fontFamily = IBMPlexMono,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = AppTextPrimary
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    description: String,
    iconSymbol: String
) {
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
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon Chip
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEFE8FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconSymbol, 
                    fontSize = 20.sp,
                    color = Color(0xFF5B4FE9)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = AppTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontFamily = Inter,
                    fontSize = 13.sp,
                    color = Color(0xFF6B6980),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen3() {
    moneyscribblTheme {
        OnboardingScreen3()
    }
}
