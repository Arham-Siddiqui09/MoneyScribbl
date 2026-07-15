package com.moneyscribbl.payment

import android.net.Uri

data class ParsedUpiQr(
    val payeeVpa: String,
    val payeeName: String,
    val amount: Double?,
    val note: String?,
    val hasEmbeddedAmount: Boolean,
    val rawUri: String
)

object UpiQrParser {

    fun parse(rawValue: String): ParsedUpiQr? {
        val trimmed = rawValue.trim()
        if (!trimmed.startsWith("upi://pay", ignoreCase = true)) return null

        val uri = Uri.parse(trimmed)
        val payeeVpa = uri.getQueryParameter("pa")?.trim().orEmpty()
        if (!UpiAppResolver.isValidUpiId(payeeVpa)) return null

        val payeeName = uri.getQueryParameter("pn")?.trim().takeUnless { it.isNullOrBlank() }
            ?: "UPI Merchant"
        val amount = uri.getQueryParameter("am")?.trim()?.toDoubleOrNull()?.takeIf { it > 0.0 }
        val note = uri.getQueryParameter("tn")?.trim().takeUnless { it.isNullOrBlank() }

        return ParsedUpiQr(
            payeeVpa = payeeVpa,
            payeeName = payeeName,
            amount = amount,
            note = note,
            hasEmbeddedAmount = uri.getQueryParameter("am") != null,
            rawUri = trimmed
        )
    }
}

