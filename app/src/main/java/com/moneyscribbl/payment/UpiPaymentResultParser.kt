package com.moneyscribbl.payment

import android.content.Intent
import android.util.Log
import java.util.Locale

private const val UPI_RESULT_TAG = "UpiPaymentResult"

data class UpiPaymentResult(
    val status: String?,
    val transactionId: String?,
    val responseCode: String?,
    val approvalRefNo: String?,
    val rawResponse: String?
) {
    val isSuccess: Boolean
        get() = status.equals("success", ignoreCase = true) ||
            status.equals("submitted", ignoreCase = true)
}

object UpiPaymentResultParser {

    fun parse(data: Intent?): UpiPaymentResult {
        val rawResponse = data?.getStringExtra("response")
            ?: data?.getStringExtra("Response")
            ?: data?.dataString
            ?: data?.extras
                ?.keySet()
                ?.firstNotNullOfOrNull { key -> data.extras?.getString(key) }

        val values = rawResponse
            ?.split("&")
            ?.mapNotNull { part ->
                val pieces = part.split("=", limit = 2)
                if (pieces.isEmpty() || pieces[0].isBlank()) return@mapNotNull null
                pieces[0].lowercase(Locale.US) to pieces.getOrNull(1).orEmpty()
            }
            ?.toMap()
            .orEmpty()

        return UpiPaymentResult(
            status = values["status"]?.ifBlank { null },
            transactionId = values["txnid"]?.ifBlank { null }
                ?: values["txnid"]?.ifBlank { null }
                ?: values["transactionid"]?.ifBlank { null },
            responseCode = values["responsecode"]?.ifBlank { null },
            approvalRefNo = values["approvalrefno"]?.ifBlank { null }
                ?: values["txnref"]?.ifBlank { null },
            rawResponse = rawResponse
        )
    }

    fun log(result: UpiPaymentResult) {
        Log.d(
            UPI_RESULT_TAG,
            "status=${result.status}, txnId=${result.transactionId}, responseCode=${result.responseCode}, approvalRefNo=${result.approvalRefNo}, raw=${result.rawResponse}"
        )
    }
}

