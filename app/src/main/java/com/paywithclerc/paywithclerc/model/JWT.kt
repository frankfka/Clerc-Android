package com.paywithclerc.paywithclerc.model

import android.content.Context
import android.util.Log
import com.paywithclerc.paywithclerc.service.BackendService
import java.time.Duration
import java.time.Instant

data class JWT(val token: String) {

    // All methods are wrapped in a companion object to make it static
    companion object {

        private const val TAG = "PAYWITHCLERCAPP: JWT"
        private var current: JWT? = null
        private var lastUpdated: Instant? = null
        private var expiryTime: Duration = Duration.ofSeconds(30L)

        /**
         * Retrieves the current JWT token or calls backend for a new token & passes it to the completion handler
         *
         * If null, something failed in retrieval
         */
        fun getToken(context: Context, requestTag: String? = null,
                     onResult: (Boolean, JWT?, Error?) -> Unit) {

            // Check the current token if it exists: lastUpdated + expiryTime must be before now time
            if (current != null && lastUpdated != null
                && lastUpdated!!.plus(expiryTime).isAfter(Instant.now())) {
                // We can use the current token
                Log.i(TAG, "Using current token. Expiry: ${lastUpdated!!.plus(expiryTime)}")
                onResult(true, current, null)
            } else {
                // Need to retrieve a new token from backend
                Log.i(TAG, "Token does not exist or is expired, creating new token")
                BackendService.getNewJWT(context, requestTag) { success, tokenString, error ->

                    if (success && tokenString != null) {
                        // Get JWT as a success
                        Log.i(TAG, "New JWT token retrieved successfully")
                        val newJWT = JWT(tokenString)
                        updateJWTState(newJWT)
                        onResult(true, newJWT, null)
                    } else {
                        // Error in getting JWT
                        Log.e(TAG, "Could not retrieve new JWT")
                        updateJWTState(null)
                        onResult(false, null, error)
                    }

                }
            }

        }

        private fun updateJWTState(newToken: JWT?) {
            if (newToken != null) {
                current = newToken
                lastUpdated = Instant.now()
            } else {
                current = null
                lastUpdated = null
            }
        }
    }

}