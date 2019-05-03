package com.paywithclerc.paywithclerc.service.stripe

import android.content.Context
import android.util.Log
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.BackendService
import com.stripe.android.PaymentCompletionProvider
import com.stripe.android.PaymentResultListener
import com.stripe.android.PaymentSessionData

/**
 * Subclass of PaymentCompletionProvider required for Stripe SDK
 *
 * OnResult is a custom callback that gives us txn ID to the caller
 */
class PaymentCompletionService(val context: Context, val store: Store, val requestTag: String? = null,
                               val onResult: (Boolean, String?, Error?) -> Unit): PaymentCompletionProvider {

    override fun completePayment(data: PaymentSessionData, listener: PaymentResultListener) {
        // Do state checking just in case
        if (data.isPaymentReadyToCharge
            && data.selectedPaymentMethodId != null
            && data.paymentResult == PaymentResultListener.INCOMPLETE
            && data.cartTotal > 0) {

            // Now check that we have all that's needed to call backend
            BackendService.completeCharge(context, data.cartTotal, store, data.selectedPaymentMethodId!!, requestTag) { success, txnId, error ->
                // Pass the result to both the Stripe listener & the custom callback
                if (success && txnId != null) {
                    Log.i(TAG, "Charge completed successfully with ID $txnId")
                    listener.onPaymentResult(PaymentResultListener.SUCCESS)
                    onResult(success, txnId, error)
                } else {
                    Log.e(TAG, "Charge was not successful. Error: ${error?.message}")
                    listener.onPaymentResult(PaymentResultListener.SUCCESS)
                    onResult(success, txnId, error)
                }
            }

        } else {
            listener.onPaymentResult(PaymentResultListener.ERROR)
        }
    }

    companion object {
        const val TAG = "PaymentCompletionService"
    }

}