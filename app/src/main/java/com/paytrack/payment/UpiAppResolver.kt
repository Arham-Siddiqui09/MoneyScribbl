package com.paytrack.payment

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import com.paytrack.data.UpiAppInfo
import java.util.Locale

private const val UPI_LAUNCH_TAG = "UpiLaunch"

data class UpiPaymentRequest(
    val upiId: String,
    val payeeName: String,
    val amount: String,
    val note: String,
    val rawUri: String? = null
)

object UpiAppResolver {
    private val preferredPackages = listOf(
        "com.google.android.apps.nbu.paisa.user",
        "com.phonepe.app",
        "net.one97.paytm",
        "in.org.npci.upiapp"
    )

    fun resolve(context: Context): List<UpiAppInfo> {
        val intent = launchUpiPayment(
            upiId = "paytrack@test",
            name = "PayTrack",
            amount = "1.00",
            note = "UPI app check"
        ) ?: return emptyList()

        val packageManager = context.packageManager
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        return resolveInfos
            .map { info ->
                UpiAppInfo(
                    label = info.loadLabel(packageManager)?.toString().orEmpty().ifBlank { info.activityInfo.packageName },
                    packageName = info.activityInfo.packageName,
                    icon = info.loadIcon(packageManager)
                )
            }
            .distinctBy(UpiAppInfo::packageName)
            .sortedWith(
                compareBy<UpiAppInfo> { preferredPackages.indexOf(it.packageName).takeIf { index -> index >= 0 } ?: Int.MAX_VALUE }
                    .thenBy(UpiAppInfo::label)
            )
    }

    fun launchUpiPayment(
        upiId: String,
        name: String,
        amount: String,
        note: String,
        rawUri: String? = null
    ): Intent? {
        val paymentRequest = createPaymentRequest(
            upiId = upiId,
            name = name,
            amount = amount,
            note = note,
            rawUri = rawUri
        ) ?: return null
        val paymentUri = buildPaymentUri(paymentRequest) ?: return null

        logPaymentUri(paymentUri, targetPackage = null)
        return Intent(Intent.ACTION_VIEW, paymentUri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
    }

    fun launchIntent(
        context: Context,
        packageName: String,
        upiId: String,
        name: String,
        amount: String,
        note: String?,
        rawUri: String? = null
    ): Intent? {
        val intent = launchUpiPayment(
            upiId = upiId,
            name = name,
            amount = amount,
            note = note.orEmpty(),
            rawUri = rawUri
        )?.setPackage(packageName)?.also {
            logPaymentUri(it.data, targetPackage = packageName)
        }
            ?: return null

        val matchesPackage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.resolveActivity(
                intent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(0)
            ) != null
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.resolveActivity(intent, 0) != null
        }

        return intent.takeIf { matchesPackage }
    }

    fun createChooserIntent(
        context: Context,
        upiId: String,
        name: String,
        amount: String,
        note: String?,
        title: String = "Pay with",
        rawUri: String? = null
    ): Intent? {
        val baseIntent = launchUpiPayment(
            upiId = upiId,
            name = name,
            amount = amount,
            note = note.orEmpty(),
            rawUri = rawUri
        ) ?: return null

        val hasHandler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(
                baseIntent,
                android.content.pm.PackageManager.ResolveInfoFlags.of(0)
            ).isNotEmpty()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(baseIntent, 0).isNotEmpty()
        }

        return Intent.createChooser(baseIntent, title).takeIf { hasHandler }
    }

    fun createPaymentRequest(
        upiId: String,
        name: String,
        amount: String,
        note: String,
        rawUri: String? = null
    ): UpiPaymentRequest? {
        val normalizedUpiId = upiId.trim()
        val normalizedName = name.trim()
        val normalizedAmount = amount.trim().toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
        val normalizedNote = note.trim().ifBlank { "Payment via PayTrack" }
        if (!isValidUpiId(normalizedUpiId) || normalizedName.isBlank()) return null

        return UpiPaymentRequest(
            upiId = normalizedUpiId,
            payeeName = normalizedName,
            amount = String.format(Locale.US, "%.2f", normalizedAmount),
            note = normalizedNote,
            rawUri = rawUri
        )
    }

    fun buildPaymentUri(request: UpiPaymentRequest): Uri? {
        val normalizedAmount = request.amount.toDoubleOrNull()?.takeIf { it > 0.0 } ?: return null
        
        // We force a pure P2P intent by stripping out merchant codes (mc), 
        // transaction references (tr), and signatures (sign). 
        // If we pass these for a merchant QR, the UPI app's Risk Policy will completely 
        // block the transaction because our app is an unverified third-party.
        return buildPaymentUri(
            upiId = request.upiId,
            name = request.payeeName,
            amount = normalizedAmount,
            note = request.note
        )
    }

    fun buildPaymentUri(
        upiId: String,
        name: String,
        amount: Double,
        note: String?
    ): Uri? {
        val normalizedUpiId = upiId.trim()
        val normalizedName = name.trim()
        val normalizedNote = note?.trim().orEmpty()
        if (!isValidUpiId(normalizedUpiId)) return null
        if (normalizedName.isBlank() || amount <= 0.0) return null

        val builder = Uri.Builder()
            .scheme("upi")
            .authority("pay")
            .appendQueryParameter("pa", normalizedUpiId)
            .appendQueryParameter("pn", normalizedName)
            .appendQueryParameter("am", String.format(Locale.US, "%.2f", amount))
            .appendQueryParameter("cu", "INR")

        if (normalizedNote.isNotBlank()) {
            builder.appendQueryParameter("tn", normalizedNote)
        }

        return builder.build().takeIf(::isValidUpiPaymentUri)
    }

    fun isValidUpiId(upiId: String): Boolean {
        if (upiId.isBlank() || upiId.contains(" ")) return false
        return Regex("^[A-Za-z0-9._-]{2,256}@[A-Za-z0-9.-]{2,64}$").matches(upiId)
    }

    private fun isValidUpiPaymentUri(uri: Uri): Boolean {
        return uri.scheme.equals("upi", ignoreCase = true) &&
            uri.authority.equals("pay", ignoreCase = true) &&
            !uri.getQueryParameter("pa").isNullOrBlank() &&
            !uri.getQueryParameter("pn").isNullOrBlank() &&
            !uri.getQueryParameter("am").isNullOrBlank() &&
            uri.getQueryParameter("cu").equals("INR", ignoreCase = true)
    }

    private fun logPaymentUri(uri: Uri?, targetPackage: String?) {
        if (uri == null) return
        Log.d(UPI_LAUNCH_TAG, "targetPackage=$targetPackage uri=$uri")
    }
}
