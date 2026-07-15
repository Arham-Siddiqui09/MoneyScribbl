package com.moneyscribbl.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.ui.theme.IBMPlexMono
import com.moneyscribbl.ui.theme.SpaceGrotesk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// â”€â”€ Brand constants â€” intentionally NOT wired to MaterialTheme. â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
// This screen is a brand moment that must look the same regardless of
// the system dark/light setting.
private object SplashColors {
    val Background1 = Color(0xFF160F2E)
    val Background2 = Color(0xFF1E1442)
    val Background3 = Color(0xFF5B4FE9)
    val OnSurface   = Color(0xFFF4F2FB)
    val Accent      = Color(0xFF34D8A8)
}

// Easing equivalent to CSS cubic-bezier(.65, 0, .35, 1) â€” smooth deceleration.
private val ScribbleEasing = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

/**
 * Full-screen brand splash screen.
 *
 * Animation timeline (all times in ms from composition):
 *  0 ms       â€” wordmark visible (static)
 *  350â€“1450 ms â€” scribble draws left-to-right
 *  1300â€“1900 ms â€” tagline fades in + slides up
 *  1500â€“2100 ms â€” footer fades in + slides up
 *  2200 ms    â€” navigate to home
 *
 * Colors are kept as explicit [SplashColors] constants and do NOT go through
 * MaterialTheme.colorScheme so the screen is always dark regardless of theme.
 */
@Composable
fun SplashScreen(onNavigate: () -> Unit) {

    // â”€â”€ Animatable values â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    val scribbleProgress = remember { Animatable(0f) }
    val taglineAlpha     = remember { Animatable(0f) }
    val taglineOffset    = remember { Animatable(6f) }   // dp offset
    val footerAlpha      = remember { Animatable(0f) }
    val footerOffset     = remember { Animatable(6f) }   // dp offset

    // â”€â”€ Background gradient brush â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    val gradientBrush = Brush.linearGradient(
        colors = listOf(SplashColors.Background1, SplashColors.Background2, SplashColors.Background3)
    )

    // â”€â”€ Animation sequence â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    LaunchedEffect(Unit) {
        // Scribble: 350 ms delay, 1100 ms duration
        launch {
            delay(350)
            scribbleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 1100,
                    easing         = ScribbleEasing
                )
            )
        }

        // Tagline: 1300 ms delay, 600 ms duration
        launch {
            delay(1300)
            launch {
                taglineAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600)
                )
            }
            launch {
                taglineOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 600, easing = ScribbleEasing)
                )
            }
        }

        // Footer: 1500 ms delay, 600 ms duration
        launch {
            delay(1500)
            launch {
                footerAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 600)
                )
            }
            launch {
                footerOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 600, easing = ScribbleEasing)
                )
            }
        }

        // Navigation: after 2200 ms total
        launch {
            delay(2200)
            onNavigate()
        }
    }

    // â”€â”€ Root container â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawRect(gradientBrush) }
    ) {

        // â”€â”€ Center content â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Column(
            modifier             = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.Center
        ) {

            // "MONEY" super-label
            Text(
                text  = "MONEY",
                style = TextStyle(
                    fontFamily    = IBMPlexMono,
                    fontWeight    = FontWeight.Medium,
                    fontSize      = 17.sp,
                    letterSpacing = 4.sp,
                    color         = SplashColors.OnSurface.copy(alpha = 0.55f)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // "Scribbl₹" wordmark â€” must not wrap on 360dp screens
            Text(
                text     = "Scribbl₹",
                maxLines = 1,
                style    = TextStyle(
                    fontFamily = SpaceGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 52.sp,
                    color      = SplashColors.OnSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Animated scribble canvas
            ScribbleCanvas(
                progress = scribbleProgress.value,
                modifier = Modifier
                    .width(230.dp)
                    .height(34.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline â€” animated fade + slide
            Text(
                text     = "jot it down. watch it add up.",
                modifier = Modifier.graphicsLayer {
                    alpha         = taglineAlpha.value
                    translationY  = taglineOffset.value.dp.toPx()
                },
                style    = TextStyle(
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Normal,
                    fontSize   = 13.sp,
                    color      = SplashColors.OnSurface.copy(alpha = 0.55f)
                )
            )
        }

        // â”€â”€ Footer â€” anchored 56dp from bottom â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .graphicsLayer {
                    alpha        = footerAlpha.value
                    translationY = footerOffset.value.dp.toPx()
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // Three dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    Canvas(modifier = Modifier.size(6.dp)) {
                        drawCircle(
                            color = if (index == 0)
                                SplashColors.Accent
                            else
                                SplashColors.OnSurface.copy(alpha = 0.55f)
                        )
                    }
                }
            }

            // "LOCAL · PRIVATE · YOURS"
            Text(
                text  = "LOCAL · PRIVATE · YOURS",
                style = TextStyle(
                    fontFamily    = IBMPlexMono,
                    fontWeight    = FontWeight.Medium,
                    fontSize      = 11.sp,
                    letterSpacing = (0.08 * 11).sp,
                    color         = SplashColors.OnSurface.copy(alpha = 0.55f)
                )
            )
        }
    }
}

// â”€â”€ Scribble Canvas â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

/**
 * Draws an S-wave scribble path from left to right based on [progress] (0â†’1).
 *
 * The path lives in a 230Ã—34dp coordinate space and is a single continuous
 * cubic bezier wave. [PathMeasure] is used to extract the visible segment so
 * the stroke appears to draw itself on.
 */
@Composable
private fun ScribbleCanvas(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val scaleX = size.width  / 230f
        val scaleY = size.height / 34f

        // Build the full scribble path in dp coordinates, then scale to px.
        // Single S-wave: starts left, dips up, crosses over, dips down, levels out.
        val fullPath = Path().apply {
            moveTo(6f * scaleX, 20f * scaleY)
            // First hill: sweeps up from (6,20) through control points to (95,18)
            cubicTo(
                25f * scaleX,  0f * scaleY,
                55f * scaleX,  0f * scaleY,
                70f * scaleX,  4f * scaleY
            )
            cubicTo(
                88f * scaleX, 12f * scaleY,
                82f * scaleX, 32f * scaleY,
                95f * scaleX, 18f * scaleY
            )
            // Second hill: crosses down then back up toward the end
            cubicTo(
                115f * scaleX,  0f * scaleY,
                138f * scaleX, 34f * scaleY,
                150f * scaleX, 34f * scaleY
            )
            cubicTo(
                162f * scaleX, 34f * scaleY,
                168f * scaleX, 14f * scaleY,
                180f * scaleX, 14f * scaleY
            )
            // Tail: eases out to the right edge
            cubicTo(
                196f * scaleX, 14f * scaleY,
                210f * scaleX, 16f * scaleY,
                224f * scaleX, 16f * scaleY
            )
        }

        // Use PathMeasure to extract the visible segment [0 â†’ progress]
        val measure    = PathMeasure()
        measure.setPath(fullPath, forceClosed = false)
        val segmentEnd = measure.length * progress

        val visiblePath = Path()
        if (segmentEnd > 0f) {
            measure.getSegment(
                startDistance = 0f,
                stopDistance  = segmentEnd,
                destination   = visiblePath,
                startWithMoveTo = true
            )
        }

        drawPath(
            path  = visiblePath,
            color = SplashColors.Accent,
            style = Stroke(
                width    = with(density) { 5.dp.toPx() },
                cap      = StrokeCap.Round,
                join     = StrokeJoin.Round
            )
        )
    }
}

