package com.moneyscribbl

import android.content.Intent
import com.moneyscribbl.payment.UpiAppResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpiAppResolverTest {

    @Test
    fun buildPaymentUri_includesRequiredUpiParameters() {
        val uri = UpiAppResolver.buildPaymentUri(
            upiId = "merchant@paytm",
            name = "Corner Store",
            amount = 145.5,
            note = "April groceries"
        )

        assertNotNull(uri)
        assertEquals("upi", uri?.scheme)
        assertEquals("pay", uri?.authority)
        assertEquals("merchant@paytm", uri?.getQueryParameter("pa"))
        assertEquals("Corner Store", uri?.getQueryParameter("pn"))
        assertEquals("145.50", uri?.getQueryParameter("am"))
        assertEquals("INR", uri?.getQueryParameter("cu"))
        assertEquals("April groceries", uri?.getQueryParameter("tn"))
    }

    @Test
    fun launchUpiPayment_buildsActionViewIntentForPaymentScreen() {
        val intent = UpiAppResolver.launchUpiPayment(
            upiId = "merchant@ybl",
            name = "Cafe Delight",
            amount = "99.00",
            note = "Coffee & snacks"
        )

        assertNotNull(intent)
        assertEquals(Intent.ACTION_VIEW, intent?.action)
        assertEquals("upi", intent?.data?.scheme)
        assertEquals("pay", intent?.data?.authority)
        assertEquals("merchant@ybl", intent?.data?.getQueryParameter("pa"))
        assertEquals("Cafe Delight", intent?.data?.getQueryParameter("pn"))
        assertEquals("99.00", intent?.data?.getQueryParameter("am"))
        assertEquals("INR", intent?.data?.getQueryParameter("cu"))
        assertEquals("Coffee & snacks", intent?.data?.getQueryParameter("tn"))
        assertTrue(intent?.categories?.contains(Intent.CATEGORY_BROWSABLE) == true)
    }

    @Test
    fun buildPaymentUri_returnsNullForInvalidInput() {
        assertNull(
            UpiAppResolver.buildPaymentUri(
                upiId = "invalid upi id",
                name = "Merchant",
                amount = 200.0,
                note = "Dinner"
            )
        )
        assertNull(
            UpiAppResolver.buildPaymentUri(
                upiId = "merchant@upi",
                name = "Merchant",
                amount = 0.0,
                note = "Dinner"
            )
        )
    }

    @Test
    fun createPaymentRequest_normalizesInputs() {
        val request = UpiAppResolver.createPaymentRequest(
            upiId = " merchant@paytm ",
            name = " Corner Store ",
            amount = "145.5",
            note = "  Monthly rent  "
        )

        assertNotNull(request)
        assertEquals("merchant@paytm", request?.upiId)
        assertEquals("Corner Store", request?.payeeName)
        assertEquals("145.50", request?.amount)
        assertEquals("Monthly rent", request?.note)
    }
}

