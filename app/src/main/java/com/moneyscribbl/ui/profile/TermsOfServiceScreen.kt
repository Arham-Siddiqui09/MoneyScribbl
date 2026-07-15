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
fun TermsOfServiceScreen(
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
                Text("Terms of Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle("1. Agreement to Terms")
            BodyText("These Terms of Service constitute a legally binding agreement made between you and moneyscribbl, concerning your access to and use of the moneyscribbl mobile application. You agree that by accessing the App, you have read, understood, and agreed to be bound by all of these Terms of Service. IF YOU DO NOT AGREE WITH ALL OF THESE TERMS OF SERVICE, THEN YOU ARE EXPRESSLY PROHIBITED FROM USING THE APP AND YOU MUST DISCONTINUE USE IMMEDIATELY.")

            SectionTitle("2. User Representations")
            BodyText("By using the App, you represent and warrant that: (1) all registration information you submit will be true, accurate, current, and complete; (2) you will maintain the accuracy of such information and promptly update such registration information as necessary; (3) you have the legal capacity and you agree to comply with these Terms of Service.")

            SectionTitle("3. Prohibited Activities")
            BodyText("You may not access or use the App for any purpose other than that for which we make the App available. The App may not be used in connection with any commercial endeavors except those that are specifically endorsed or approved by us.")

            SectionTitle("4. Modifications and Interruptions")
            BodyText("We reserve the right to change, modify, or remove the contents of the App at any time or for any reason at our sole discretion without notice. However, we have no obligation to update any information on our App. We also reserve the right to modify or discontinue all or part of the App without notice at any time.")

            SectionTitle("5. Governing Law")
            BodyText("These Terms shall be governed by and defined following the laws of your jurisdiction. moneyscribbl and yourself irrevocably consent that the courts of your jurisdiction shall have exclusive jurisdiction to resolve any dispute which may arise in connection with these terms.")

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

