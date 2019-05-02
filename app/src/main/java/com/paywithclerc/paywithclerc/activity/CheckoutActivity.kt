package com.paywithclerc.paywithclerc.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.service.stripe.EphemeralKeyService
import com.stripe.android.CustomerSession
import com.stripe.android.model.Source
import com.stripe.android.view.PaymentMethodsActivity
import kotlinx.android.synthetic.main.activity_checkout.*


class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        CustomerSession.initCustomerSession(
            EphemeralKeyService(this, TAG) { success, error ->
                if (success) {
                    // TODO deal with success
                    Log.e(TAG, "Success getting eph key")

                    checkoutSelectPaymentButton.setOnClickListener {
                        val payIntent = Intent(this, PaymentMethodsActivity::class.java)
                        startActivityForResult(payIntent, ActivityConstants.CHECKOUT_PAYMENT_SRC_INTENT);
                    }
                } else {
                    // TODO deal with error
                    Log.e(TAG, "Error getting eph key: ${error?.message}")
                }
            }
        )
    }

    /**
     * Called on return from external activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityConstants.CHECKOUT_PAYMENT_SRC_INTENT && resultCode == Activity.RESULT_OK) {
            val selectedSource = data!!.getStringExtra(PaymentMethodsActivity.EXTRA_SELECTED_PAYMENT)
            Log.e(TAG, selectedSource)
            val source = Source.fromString(selectedSource)
            // This is the customer-selected source.
            // Note: it isn't possible for a null or non-card source to be returned at this time.
        } else {
            // TODO Deal with error
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // TODO disable all network calls
    }

    companion object {
        const val TAG = "CheckoutActivity"
    }

}
