package com.paywithclerc.paywithclerc.service

import android.content.Context
import android.util.Log
import androidx.annotation.Size
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.paywithclerc.paywithclerc.constant.BackendConstants
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.JWT
import org.json.JSONObject

object BackendService {

    const val TAG = "BackendService"

    /**
     * Calls backend to create a new customer & passes its Stripe ID to the callback
     *
     * onResult -> success, customer ID, error
     */
    fun createCustomer(context: Context, requestTag: String? = null, onResult: (Boolean, String?, Error?) -> (Unit)) {
        // Get an instance of the networking service
        val networkService = NetworkService.getInstance(context)
        val createCustomerRequest = JsonObjectRequest(Request.Method.POST, BackendConstants.CREATE_CUST_URL, null,
            Response.Listener { response ->
                // Check that backend returned properly
                if (!response.isNull("customer_id")) {
                    // Success - call the callback
                    onResult(true, response.getString("customer_id"), null)
                } else {
                    // Customer ID not given
                    Log.e(TAG, "Backend did not return JSON with customer ID field")
                    onResult(false, null, Error("Incorrect backend response. Response: ${response.toString(2)}"))
                }
            },
            Response.ErrorListener { error ->
                // Error from backend
                Log.e(TAG, "Backend network call errored while creating customer")
                onResult(false, null, Error("${error.networkResponse.data}"))
            })
        // Add the request to the network queue
        networkService.addToRequestQueue(createCustomerRequest, requestTag)
    }

    /**
     * Calls backend to retrieve an ephemeral key for the customer & conforms to Stripe SDK's requirements
     * Used by EphemeralKeyService to interact with the SDK
     *
     * onResult -> success, ephemeralKey, error
     */
    fun createEphemeralKey(@Size(min = 4) apiVersion: String, context: Context, requestTag: String? = null,
                           onResult: (Boolean, String?, Error?) -> Unit) {

        val networkService = NetworkService.getInstance(context)
        val currentCustomer = Customer.current

        // Check that current customer exists
        if (currentCustomer == null) {
            Log.e(TAG, "No current customer!")
            onResult(false, null, Error("No current customer"))
            return
        }

        // Get JWT, if successful, call backend to create an ephemeral key
        JWT.getToken(context, requestTag) { success, jwt, error ->

            if (success && jwt != null) {
                // JWT retrieval success, we can now get an ephemeral key
                val createKeyParams = hashMapOf("stripe_version" to apiVersion,
                                                "token" to jwt.token,
                                                "customer_id" to currentCustomer.stripeID)
                // POST the request to our backend server
                val createEphemeralKeyRequest = JsonObjectRequest(Request.Method.POST,
                    BackendConstants.CREATE_EPH_KEY_URL,
                    JSONObject(createKeyParams),
                    Response.Listener { response ->
                        // Stripe SDK just wants a raw string
                        val rawString = response.toString()
                        onResult(true, rawString, null)
                    },
                    Response.ErrorListener { networkError ->
                        // Error from backend
                        Log.e(TAG, "Backend network call errored while creating ephemeral key")
                        onResult(false, null, Error("${networkError.message}"))
                    })
                // Add the request to the network queue
                networkService.addToRequestQueue(createEphemeralKeyRequest, requestTag)

            } else {
                // JWT retrieval failed
                Log.e(TAG, "Could not create ephemeral key because JWT retrieval failed")
                onResult(false, null, Error("${error?.message}"))
            }

        }
    }

    /**
     * Calls backend to create a new JWT token
     *
     * onResult -> success, token string, error
     */
    fun getNewJWT(context:Context, requestTag: String? = null,
                  onResult: (Boolean, String?, Error?) -> Unit) {

        val networkService = NetworkService.getInstance(context)
        val currentCustomer = Customer.current

        // Check that current customer exists
        if (currentCustomer == null) {
            Log.e(TAG, "No current customer!")
            onResult(false, null, Error("No current customer"))
            return
        }

        val getJWTParams = hashMapOf("user_id" to currentCustomer.firebaseID)
        // POST the request to our backend server
        val getJWTRequest = JsonObjectRequest(Request.Method.POST,
            BackendConstants.JWT_URL,
            JSONObject(getJWTParams),
            Response.Listener { response ->
                // Check that backend returned properly
                if (!response.isNull("token")) {
                    // Success - call the callback
                    onResult(true, response.getString("token"), null)
                } else {
                    // Token not given
                    Log.e(TAG, "Backend did not return JSON with token field")
                    onResult(false, null, Error("Incorrect backend response while retrieving JWT. Response: ${response.toString(2)}"))
                }
            },
            Response.ErrorListener { error ->
                // Error from backend
                Log.e(TAG, "Backend network call errored while requesting new JWT token")
                onResult(false, null, Error("${error.networkResponse.data}"))
            })
        // Add the request to the network queue
        networkService.addToRequestQueue(getJWTRequest, requestTag)

    }

    // This is used to compute Stripe cost in cents
    fun getStripeCost(cost: Double): Int {
        return (cost*100).toInt()
    }

}