package com.moneyscribbl

import android.content.Intent
import com.moneyscribbl.payment.UpiPaymentResultParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiPaymentResultParserTest {

    @Test
    fun parse_readsStandardUpiResponseFields() {
        val intent = Intent().putExtra(
            "response",
            "Status=SUCCESS&txnId=TXN123&responseCode=00&ApprovalRefNo=APR456"
        )

        val result = UpiPaymentResultParser.parse(intent)

        assertEquals("SUCCESS", result.status)
        assertEquals("TXN123", result.transactionId)
        assertEquals("00", result.responseCode)
        assertEquals("APR456", result.approvalRefNo)
        assertTrue(result.isSuccess)
    }

    @Test
    fun parse_readsMixedCaseTransactionIdKeys() {
        val intent = Intent().putExtra(
            "response",
            "status=FAILURE&TxnId=ABC999&responseCode=U16"
        )

        val result = UpiPaymentResultParser.parse(intent)

        assertEquals("ABC999", result.transactionId)
        assertEquals("U16", result.responseCode)
    }
}

