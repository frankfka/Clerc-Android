package com.paywithclerc.paywithclerc.service

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import com.paywithclerc.paywithclerc.activity.CheckoutActivity
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.service.stripe.EphemeralKeyService
import com.stripe.android.CustomerSession
import com.stripe.android.PaymentSession
import com.stripe.android.PaymentSessionConfig
import com.stripe.android.PaymentSessionData

object SessionService {

    const val TAG = "SessionService"

    /**
     * Loads a customer session including the Stripe session
     */
    fun loadCustomerSession(customer: Customer, context: Context, requestTag: String? = null,
                            onResult: (Boolean, Error?) -> Unit) {
        // Initialize the current customer object
        Customer.current = customer
        CustomerSession.initCustomerSession(
            EphemeralKeyService(context, requestTag) { success, error ->
                if (success) {
                    Log.i(TAG, "Ephemeral key retrieved successfully")
                    onResult(true, null)
                } else {
                    Log.e(TAG, "Ephemeral key retrieval failed")
                    onResult(false, error)
                }
            }
        )
    }

    /**
     * Loads a Stripe payment session
     *
     * Returns a payment session if payment session was successfully loaded
     */
    fun loadPaymentSession(hostActivity: Activity, paymentSessionListener: PaymentSession.PaymentSessionListener): PaymentSession? {
        // Create a session and its config
        val paymentSession = PaymentSession(hostActivity)
        val paymentSessionConfig = PaymentSessionConfig.Builder()
            .setShippingInfoRequired(false)
            .setShippingMethodsRequired(false)
            .build()
        // Try to initialize - paymentSessionInitialized = true if successful
        val paymentSessionInitialized = paymentSession.init(paymentSessionListener, paymentSessionConfig)
        return if (paymentSessionInitialized) {
            Log.i(TAG, "Payment session initialized successfully")
            paymentSession
        } else {
            Log.e(TAG, "Payment session was not successfully initialized")
            null
        }
    }

    /**
     * Ends a customer session - call this when customer logs out
     */
    fun endCustomerSession() {
        Customer.current = null
        CustomerSession.endCustomerSession()
    }

}