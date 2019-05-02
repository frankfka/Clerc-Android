package com.paywithclerc.paywithclerc.service.stripe

import android.content.Context
import android.util.Log
import com.stripe.android.EphemeralKeyProvider
import androidx.annotation.Size
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.service.BackendService
import com.stripe.android.EphemeralKeyUpdateListener

/**
 * Class to abstract away some of the Stripe-specific inheritance
 *
 * onResult -> success, error
 */
class EphemeralKeyService(val context: Context, private val requestTag: String? = null, val onResult: (Boolean, Error?) -> (Unit)): EphemeralKeyProvider {

    /**
     * Gets an ephemeral key from backend for the given customer
     */
    override fun createEphemeralKey(@Size(min = 4) apiVersion: String, keyUpdateListener: EphemeralKeyUpdateListener) {

        // Call backend service to create the ephemeral key
        BackendService.createEphemeralKey(apiVersion, context, requestTag) { success, key, error ->
            if (success && key != null) {
                // Update Stripe listener on ephemeral key
                keyUpdateListener.onKeyUpdate(key)
                // Update the callback
                onResult(true, null)
            } else {
                Log.e(TAG, "Ephemeral key retrieval failed with error: ${error?.message}")
                // TODO call onKeyUpdateFailure
                keyUpdateListener.onKeyUpdateFailure(0, error?.message ?: "") // TODO Does this work?
                onResult(false, error)
            }
        }

    }

    companion object {
        const val TAG = "EphemeralKeyService"
    }

}