package com.paywithclerc.paywithclerc.service

import android.content.Context
import android.util.Log
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.service.stripe.EphemeralKeyService
import com.stripe.android.CustomerSession

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
     * Ends a customer session - call this when customer logs out
     */
    fun endCustomerSession() {
        Customer.current = null
        CustomerSession.endCustomerSession()
    }

}