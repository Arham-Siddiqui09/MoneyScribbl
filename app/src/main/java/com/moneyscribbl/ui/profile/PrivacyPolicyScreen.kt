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
                text = "Last updated: 17 July 2026",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            SectionTitle("1. Introduction")
            BodyText(
                "Welcome to Money Scribbl. We are committed to protecting your personal information " +
                "and your right to privacy. This Privacy Policy explains how we handle information " +
                "when you use our mobile application (\"App\"). Please read this policy carefully. " +
                "If you do not agree with the terms of this policy, please discontinue use of the App."
            )

            SectionTitle("2. Information We Collect")
            BodyText(
                "Money Scribbl is designed as a fully offline, local-first application. We collect " +
                "only the information that you choose to enter manually into the App, including:\n\n" +
                "• Transaction records (amount, date, category, description)\n" +
                "• Budget and savings goals you configure\n" +
                "• Folder and category names you create\n" +
                "• Profile preferences such as your name, currency selection, and theme\n" +
                "• A profile photo, if you choose to add one (stored only on your device)\n\n" +
                "We do not collect any personal data automatically at this time."
            )

            SectionTitle("3. How Your Data Is Stored")
            BodyText(
                "Currently, all data you enter into Money Scribbl is stored exclusively on your device using " +
                "Android's local storage (Room database and DataStore). Your financial records, " +
                "budgets, folders, and preferences never leave your device.\n\n" +
                "We do not operate any servers. We do not have the ability to access, view, or " +
                "retrieve your data under any circumstances, because it is never transmitted to us."
            )

            SectionTitle("4. Internet and Network Access")
            BodyText(
                "Money Scribbl currently does not require an internet connection to function and " +
                "does not connect to any external servers or cloud services for core features."
            )

            SectionTitle("5. Advertising")
            BodyText(
                "The App does not currently display advertisements. " +
                "If advertisements are introduced in a future version, these Terms of Service " +
                "and the Privacy Policy will be updated accordingly before those features are released."
            )

            SectionTitle("6. Data Sharing and Third Parties")
            BodyText(
                "We do not share, sell, rent, or trade any of your personal information with " +
                "third parties for commercial purposes. Because all data is stored locally on " +
                "your device, there is no data for us to share. We have no access to your information."
            )

            SectionTitle("7. Data Security")
            BodyText(
                "Since all your data is stored locally on your own device, the security of your " +
                "data depends on the security settings of your device (such as screen lock, " +
                "device encryption, and app permissions). We strongly recommend keeping your " +
                "device secured with a PIN, password, or biometrics."
            )

            SectionTitle("8. Data Deletion")
            BodyText(
                "You have full control over your data at all times. You can delete any transaction, " +
                "folder, or category directly within the App. To delete all data, you can clear " +
                "the App's storage from your device's Settings > Apps > Money Scribbl > Storage > " +
                "Clear Data. Users can also uninstall the App at any time to remove all associated data. " +
                "This action is permanent and irreversible."
            )

            SectionTitle("9. Children's Privacy")
            BodyText(
                "Although the App does not intentionally collect personal information from children, " +
                "it is intended for personal financial management and is not specifically designed " +
                "for children under 13."
            )

            SectionTitle("10. Changes to This Policy")
            BodyText(
                "We may update this Privacy Policy from time to time. Any changes will be " +
                "reflected in this Privacy Policy with an updated \"Last updated\" date " +
                "at the top of this page. We encourage you to review this policy periodically."
            )

            SectionTitle("11. Permissions")
            BodyText(
                "Money Scribbl may request certain permissions depending on the features you use.\n\n" +
                "• Photos/Media – to let you select an existing profile image.\n\n" +
                "Permissions are requested only when required and can be revoked at any time " +
                "through your device settings."
            )

            SectionTitle("12. Contact Us")
            BodyText(
                "If you have any questions or concerns about this Privacy Policy or our data " +
                "practices, please contact us at:\n\n" +
                "Email: moneyscribbl@gmail.com"
            )

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
