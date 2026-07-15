package com.moneyscribbl.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                // .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = (-16).dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Text("Privacy Policy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle("1. Introduction")
            BodyText("Welcome to moneyscribbl. We are committed to protecting your personal information and your right to privacy. If you have any questions or concerns about this privacy notice, or our practices with regards to your personal information, please contact us.")

            SectionTitle("2. Information We Collect")
            BodyText("We collect personal information that you voluntarily provide to us when you register on the App, express an interest in obtaining information about us or our products and Services, when you participate in activities on the App or otherwise when you contact us.\n\nThe personal information that we collect depends on the context of your interactions with us and the App, the choices you make and the products and features you use.")

            SectionTitle("3. SMS and Financial Data")
            BodyText("With your explicit permission, moneyscribbl can read your SMS messages to automatically detect and import your bank transactions. This data is processed locally on your device to help you track your expenses. We do not transmit your SMS content to our servers or share it with third parties. Your financial data remains private and secure on your device.")

            SectionTitle("4. How We Use Your Information")
            BodyText("We use personal information collected via our App for a variety of business purposes described below. We process your personal information for these purposes in reliance on our legitimate business interests, in order to enter into or perform a contract with you, with your consent, and/or for compliance with our legal obligations.")

            SectionTitle("5. Security of Your Information")
            BodyText("We aim to protect your personal information through a system of organizational and technical security measures. We have implemented appropriate technical and organizational security measures designed to protect the security of any personal information we process. However, despite our safeguards and efforts to secure your information, no electronic transmission over the Internet or information storage technology can be guaranteed to be 100% secure.")

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
    )
}

