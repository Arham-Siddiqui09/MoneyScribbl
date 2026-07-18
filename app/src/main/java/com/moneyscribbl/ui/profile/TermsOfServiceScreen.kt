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
            BodyText(
                "These Terms of Service (\"Terms\") constitute a legally binding agreement between " +
                "you (\"User\") and the developer of Money Scribbl (\"we\", \"us\", or \"our\") " +
                "regarding your use of the Money Scribbl mobile application (\"App\").\n\n" +
                "By downloading or using the App, you confirm that you are at least 13 years of " +
                "age, that you have read and understood these Terms, and that you agree to be " +
                "bound by them. If you do not agree, please do not use the App."
            )

            SectionTitle("2. Description of the App")
            BodyText(
                "Money Scribbl is a personal finance tracking application that allows you to:\n\n" +
                "• Manually record income and expense transactions\n" +
                "• Organise transactions into categories and folders\n" +
                "• Set and track budget goals and savings targets\n" +
                "• View summaries, charts, and spending insights\n\n" +
                "The App operates entirely offline. All data is stored locally on your device. " +
                "The App does not require an account, login, or internet connection to function."
            )

            SectionTitle("3. License to Use")
            BodyText(
                "We grant you a limited, non-exclusive, non-transferable, revocable license to " +
                "download and use the App strictly for your own personal, non-commercial, " +
                "You may not:\n" +
                "• Copy or redistribute the App.\n" +
                "• Modify or create derivative works.\n" +
                "• Reverse engineer, decompile, or attempt to extract the source code, except where permitted by applicable law.\n" +
                "• Use the App for any unlawful purpose."
            )

            SectionTitle("4. User Responsibilities")
            BodyText(
                "You are solely responsible for:\n\n" +
                "• The accuracy of all financial data you enter into the App\n" +
                "• Maintaining backups of your data, as the App does not back up data to any cloud\n" +
                "• Keeping your device secured to prevent unauthorized access to your financial records\n" +
                "• Any decisions you make based on information displayed by the App\n\n" +
                "The App is a personal record-keeping tool. It is not a financial advisor, " +
                "banking service, or payment platform. It does not process real money or " +
                "connect to your bank accounts."
            )

            SectionTitle("5. No Financial Advice")
            BodyText(
                "Money Scribbl provides tools for personal expense tracking and budgeting. " +
                "Nothing in the App constitutes financial, investment, tax, or legal advice. " +
                "You should consult a qualified professional before making any financial decisions. " +
                "We are not liable for any financial decisions you make based on the data or " +
                "summaries displayed in the App."
            )

            SectionTitle("6. Disclaimer of Warranties")
            BodyText(
                "The App is provided \"as is\" and \"as available\" without warranties of any kind, " +
                "either express or implied. We do not warrant that the App will be error-free, " +
                "uninterrupted, secure, or free of bugs or viruses. We do not warrant the " +
                "accuracy of any calculations or summaries generated by the App.\n\n" +
                "Your use of the App is at your sole risk. Users are responsible for reviewing " +
                "the accuracy of the information they enter into the App."
            )

            SectionTitle("7. Limitation of Liability")
            BodyText(
                "To the maximum extent permitted by applicable law, we shall not be liable for " +
                "any indirect, incidental, special, consequential, or punitive damages, including " +
                "but not limited to loss of data, loss of profits, or financial loss, arising " +
                "from your use of or inability to use the App, even if we have been advised of " +
                "the possibility of such damages.\n\n" +
                "In particular, we are not responsible for any data loss caused by device " +
                "failure, App uninstallation, or clearing of App storage."
            )

            SectionTitle("8. Third-Party Services")
            BodyText(
                "The App may allow you to initiate payments through third-party UPI applications " +
                "(such as Google Pay, PhonePe, or Paytm) using Android's standard Intent system. " +
                "Money Scribbl does not process or handle any payment itself. Any payment made " +
                "through a third-party app is subject to that app's own terms and policies. " +
                "We are not responsible for the functioning of those third-party services."
            )

            SectionTitle("9. Intellectual Property")
            BodyText(
                "Money Scribbl, including its design, code, graphics, trademarks, and content, " +
                "is owned by the developer and is protected by applicable intellectual property laws. " +
                "These Terms do not grant you ownership of the App or any intellectual property rights."
            )

            SectionTitle("10. Modifications to the App and Terms")
            BodyText(
                "We reserve the right to modify, update, or discontinue the App at any time " +
                "without prior notice. We may also update these Terms periodically. Continued " +
                "use of the App after any such changes constitutes your acceptance of the " +
                "new Terms. The \"Last updated\" date at the top of this page will reflect " +
                "when the Terms were last revised."
            )

            SectionTitle("11. Governing Law")
            BodyText(
                "These Terms shall be governed by the laws of India. Any disputes arising under " +
                "these Terms shall be subject to the competent courts having jurisdiction in India."
            )

            SectionTitle("12. Contact Us")
            BodyText(
                "If you have any questions about these Terms of Service, please contact us at:\n\n" +
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
