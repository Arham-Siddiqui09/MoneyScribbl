package com.moneyscribbl.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyscribbl.R
import com.moneyscribbl.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen4(
    name: String = "",
    onNameChange: (String) -> Unit = {},
    profileImageUri: String? = null,
    onProfileImageSelected: (String?) -> Unit = {},
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
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        
        val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let {
                coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val file = java.io.File(context.filesDir, "profile_pic_${System.currentTimeMillis()}.jpg")
                        val outputStream = java.io.FileOutputStream(file)
                        inputStream?.copyTo(outputStream)
                        inputStream?.close()
                        outputStream.close()
                        onProfileImageSelected(file.absolutePath)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }



        // Heading
        Text(
            text = "What should we\ncall you?",
            fontFamily = SpaceGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            color = AppTextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtext
        Text(
            text = "This stays on your device — it's just how\nMoneyScribbl greets you.",
            fontFamily = Inter,
            fontSize = 15.sp,
            color = Color(0xFF6B6980),
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Centered Avatar Picker
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                // Dashed border background
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFFC9C2F2),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    )
                }

                // Avatar fill
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF4F2FF))
                        .clickable {
                            launcher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (profileImageUri != null) {
                        coil.compose.AsyncImage(
                            model = java.io.File(profileImageUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        // Fallback person icon
                        Text(
                            text = "👤",
                            fontSize = 40.sp,
                            color = Color(0xFF5B4FE9)
                        )
                    }
                }

                // Camera Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .background(Color.White, CircleShape)
                        .padding(2.dp) // creates the "cut out" border effect
                        .background(gradientBrush, CircleShape)
                        .shadow(4.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📷", fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Name Input Field
        Text(
            text = "YOUR NAME",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = Color(0xFF94A3B8)
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 8.dp, 
                    shape = RoundedCornerShape(12.dp),
                    spotColor = VioletAccent.copy(alpha = 0.1f),
                    ambientColor = VioletAccent.copy(alpha = 0.05f)
                )
                .background(Color.White, RoundedCornerShape(12.dp))
                .border(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "👤", fontSize = 18.sp, color = Color(0xFF5B4FE9))
                Spacer(modifier = Modifier.width(12.dp))
                
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = TextStyle(
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = AppTextPrimary
                    ),
                    cursorBrush = SolidColor(Color(0xFF5B4FE9)),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (name.isEmpty()) {
                            Text(
                                text = "Enter your name",
                                style = TextStyle(
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 16.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "You can change your name or photo anytime from Profile.",
            fontFamily = Inter,
            fontSize = 11.5.sp,
            color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen4() {
    moneyscribblTheme {
        OnboardingScreen4()
    }
}
