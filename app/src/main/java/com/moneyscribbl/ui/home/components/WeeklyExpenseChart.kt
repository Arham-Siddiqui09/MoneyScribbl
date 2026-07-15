package com.moneyscribbl.ui.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.AppSurface
import com.moneyscribbl.ui.theme.moneyscribblTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun WeeklyExpenseChart(
    data: List<Float>,
    labels: List<String>,
    currentDayIndex: Int,
    isLineGraph: Boolean = false, // Ignored as per spec to maintain consistency
    currencyCode: String = "INR",
    modifier: Modifier = Modifier
) {
    val maxValue = remember(data) { data.maxOrNull() ?: 0f }
    val maxChartHeightDp = 118.dp
    val floorHeightDp = 3.dp
    val minVisibleHeightDp = 6.dp
    
    val safeCurrentIndex = currentDayIndex.coerceIn(0, (data.size - 1).coerceAtLeast(0))
    val isMonth = data.size > 7
    val barWidth = if (isMonth) 20.dp else 24.dp
    val labelFontSize = if (isMonth) 10.sp else 11.sp

    // Total computation
    val totalAmount = remember(data, currencyCode) {
        val sum = data.sum()
        val locale = when (currencyCode) {
            "USD" -> Locale.US
            "EUR" -> Locale.forLanguageTag("en-IE")
            "GBP" -> Locale.UK
            else -> Locale.forLanguageTag("en-IN")
        }
        val format = NumberFormat.getCurrencyInstance(locale)
        format.maximumFractionDigits = 0
        format.format(sum)
    }
    
    // Period text
    val periodText = if (isMonth) "Total this period" else "Total this week"

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp) // Total height for chart area including labels
        ) {
            // Background Gridlines
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxChartHeightDp)
                    .align(Alignment.BottomCenter)
                    .offset(y = (-20).dp) // Adjust to match bar baseline
            ) {
                val gridColor = Color.LightGray.copy(alpha = 0.5f)
                val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                // 100%
                drawLine(gridColor, Offset(0f, 0f), Offset(size.width, 0f), pathEffect = pathEffect)
                // 50%
                drawLine(gridColor, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), pathEffect = pathEffect)
                // 0%
                drawLine(gridColor, Offset(0f, size.height), Offset(size.width, size.height), pathEffect = pathEffect)
            }

            // Bars
            val scrollState = androidx.compose.foundation.rememberScrollState()
            androidx.compose.runtime.LaunchedEffect(scrollState.maxValue) {
                scrollState.scrollTo(scrollState.maxValue)
            }
            
            val columnWidthDp = 32.dp
            val spacingDp = if (isMonth) 12.dp else 24.dp
            val gradientColors = remember {
                listOf(
                    Color(0xFF7C6BFF).copy(alpha = 0.3f),
                    Color(0xFF7C6BFF).copy(alpha = 0.0f)
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .horizontalScroll(scrollState)
                    .padding(end = 2.dp)
                    .drawWithCache {
                        if (!isLineGraph || data.isEmpty()) {
                            return@drawWithCache onDrawBehind {}
                        }

                        val columnWidthPx = columnWidthDp.toPx()
                        val spacingPx = spacingDp.toPx()
                        val baselineY = size.height - 22.dp.toPx()
                        
                        val points = data.mapIndexed { index, value ->
                            val isZero = value <= 0f
                            val barHeightDp = if (isZero || maxValue <= 0f) {
                                floorHeightDp
                            } else {
                                val ratio = kotlin.math.sqrt(value) / kotlin.math.sqrt(maxValue)
                                maxOf(minVisibleHeightDp, maxChartHeightDp * ratio)
                            }
                            val barHeightPx = barHeightDp.toPx()
                            
                            val x = index * (columnWidthPx + spacingPx) + (columnWidthPx / 2f)
                            val y = baselineY - barHeightPx
                            Offset(x, y)
                        }
                        
                        val path = androidx.compose.ui.graphics.Path()
                        path.moveTo(points.first().x, points.first().y)
                        
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cx = (p1.x + p2.x) / 2f
                            path.cubicTo(
                                x1 = cx, y1 = p1.y,
                                x2 = cx, y2 = p2.y,
                                x3 = p2.x, y3 = p2.y
                            )
                        }
                        
                        val fillPath = androidx.compose.ui.graphics.Path().apply {
                            addPath(path)
                            lineTo(points.last().x, baselineY)
                            lineTo(points.first().x, baselineY)
                            close()
                        }
                        
                        val fillBrush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = gradientColors,
                            startY = points.minOf { it.y },
                            endY = baselineY
                        )
                        
                        val strokeStyle = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 3.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        val dotColors = points.mapIndexed { index, _ -> 
                            val isCurrent = index == safeCurrentIndex
                            if (isCurrent) Color(0xFF5B4CFC) else Color(0xFF7C6BFF)
                        }
                        val dotRadii = points.mapIndexed { index, _ -> 
                            val isCurrent = index == safeCurrentIndex
                            (if (isCurrent) 4.dp else 2.dp).toPx()
                        }

                        onDrawBehind {
                            drawPath(
                                path = fillPath,
                                brush = fillBrush
                            )
                            
                            drawPath(
                                path = path,
                                color = Color(0xFF7C6BFF),
                                style = strokeStyle
                            )
                            
                            points.forEachIndexed { index, point ->
                                drawCircle(
                                    color = Color.White,
                                    radius = dotRadii[index] + 1.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = dotColors[index],
                                    radius = dotRadii[index],
                                    center = point
                                )
                            }
                        }
                    },
                horizontalArrangement = Arrangement.spacedBy(spacingDp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Cache the gradient brush for the current/active bar — avoids allocating a new
                // Brush object on every recomposition (layout-relative gradient, safe to reuse)
                val currentBarBrush = remember {
                    Brush.verticalGradient(colors = listOf(Color(0xFF7C6BFF), Color(0xFF5B4CFC)))
                }

                data.forEachIndexed { index, value ->
                    val isCurrent = index == safeCurrentIndex
                    val isZero = value <= 0f
                    
                    val barHeight = if (isZero || maxValue <= 0f) {
                        floorHeightDp
                    } else {
                        val ratio = kotlin.math.sqrt(value) / kotlin.math.sqrt(maxValue)
                        maxOf(minVisibleHeightDp, maxChartHeightDp * ratio)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.width(columnWidthDp)
                    ) {
                        // Amount Label
                        if (!isZero) {
                            val compactValue = when {
                                value >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", value / 1_000_000f)
                                value >= 1_000 -> String.format(java.util.Locale.US, "%.1fK", value / 1_000f)
                                else -> value.toInt().toString()
                            }.replace(".0", "")
                            
                            Text(
                                text = "₹$compactValue",
                                fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                                modifier = Modifier.wrapContentWidth(unbounded = true),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        } else {
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Bar
                        Box(
                            modifier = Modifier
                                .width(barWidth)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                .then(
                                    if (isLineGraph) {
                                        Modifier // Make transparent for line graph
                                    } else if (isCurrent) {
                                        Modifier.background(currentBarBrush)
                                    } else if (isZero) {
                                        Modifier.background(Color(0xFFEEEEF5))
                                    } else {
                                        Modifier.background(Color(0xFFD8D2FF))
                                    }
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Period Label
                        Text(
                            text = labels.getOrElse(index) { "" },
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = labelFontSize,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = if (isCurrent) Color(0xFF5B4CFC) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.height(16.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Footer Strip
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = periodText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = totalAmount,
                    fontFamily = com.moneyscribbl.ui.theme.IBMPlexMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F6F3)
@Composable
private fun WeeklyExpenseChartPreview() {
    moneyscribblTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Expense Trend",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                WeeklyExpenseChart(
                    data = listOf(0f, 0f, 0f, 0f, 0f, 2052f, 20f),
                    labels = listOf("Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue"),
                    currentDayIndex = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

