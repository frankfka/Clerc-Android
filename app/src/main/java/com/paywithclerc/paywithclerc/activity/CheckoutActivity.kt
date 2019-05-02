package com.paywithclerc.paywithclerc.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.service.stripe.EphemeralKeyService
import com.stripe.android.CustomerSession
import com.stripe.android.PaymentSession
import com.stripe.android.PaymentSessionConfig
import com.stripe.android.PaymentSessionData
import com.stripe.android.model.Source
import com.stripe.android.view.PaymentMethodsActivity
import kotlinx.android.synthetic.main.activity_checkout.*

/**
 * This combines Stripe's PaymentSession & the activity. In the future we'd want to refactor this out
 */
class CheckoutActivity : AppCompatActivity() {

    private var mPaymentSession: PaymentSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        setupPaymentSession()

    }

    /**
     * Called on return from external activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // TODO this supposedly handles all data changes?
        if (data != null) {
            mPaymentSession?.handlePaymentData(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO disable all network calls
        mPaymentSession?.onDestroy()
    }

    private fun setupPaymentSession() {
        mPaymentSession = PaymentSession(this)
        val paymentSessionInitialized = mPaymentSession!!.init(
            object : PaymentSession.PaymentSessionListener {
                override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                    if (isCommunicating) {
                        Log.e(TAG, "is communicating")
                    } else {
                        Log.e(TAG, "is not communicating")
                    }
                }

                override fun onError(errorCode: Int, @Nullable errorMessage: String?) {
                    Log.e(TAG, "Error: $errorMessage")
                }

                override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                    Log.e(TAG, "Data changed: ${data.isPaymentReadyToCharge}, ${data.paymentResult}, ${data.selectedPaymentMethodId}")
                }
            }, PaymentSessionConfig.Builder()
                .setShippingInfoRequired(false)
                .setShippingMethodsRequired(false)
                .build()
        )
        if (paymentSessionInitialized) {
            Log.e(TAG, "Session initialized")
        }
    }

    companion object {
        const val TAG = "CheckoutActivity"
    }

}
