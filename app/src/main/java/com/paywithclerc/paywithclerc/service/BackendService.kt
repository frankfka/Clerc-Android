package com.paywithclerc.paywithclerc.service

import android.content.Context
import android.util.Log
import androidx.annotation.Size
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.paywithclerc.paywithclerc.constant.BackendConstants
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.JWT
import com.paywithclerc.paywithclerc.model.Store
import org.json.JSONObject

object BackendService {

    const val TAG = "PAYWITHCLERCAPP: BackendService"

    /**
     * Calls backend to send a receipt to the customer
     *
     * onResult -> success
     */
    fun emailReceipt(txnId: String, context: Context, requestTag: String? = null, onResult: (Boolean) -> (Unit)) {
        // Get an instance of the networking service
        val networkService = NetworkService.getInstance(context)
        val currentCustomer = Customer.current

        // Check that current customer exists
        if (currentCustomer == null) {
            Log.e(TAG, "No current customer!")
            onResult(false)
            return
        }

        // Get JWT, if successful, call backend to create an ephemeral key
        JWT.getToken(context, requestTag) { success, jwt, error ->

            if (success && jwt != null) {

                val sendEmailParams = hashMapOf(
                    "token" to jwt.token,
                    "txn_id" to txnId,
                    "customer_name" to (currentCustomer.name ?: ""),
                    "customer_email" to (currentCustomer.email ?: "")
                )

                // JWT retrieved successfully
                val sendEmailRequest = JsonObjectRequest(Request.Method.POST, BackendConstants.EMAIL_RECEIPT_URL, JSONObject(sendEmailParams),
                    Response.Listener {
                        onResult(true)
                    },
                    Response.ErrorListener {
                        // Error from backend
                        Log.e(TAG, "Backend network call errored while sending receipt: ${it.networkResponse}")
                        onResult(false)
                    })
                // Add the request to the network queue
                networkService.addToRequestQueue(sendEmailRequest, requestTag)

            } else {
                // JWT retrieval failed
                Log.e(TAG, "Could not send email because JWT retrieval failed: $error")
                onResult(false)
            }

        }
    }

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
                onResult(false, null, Error("${error?.networkResponse?.data}"))
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
     * Calls backend to charge a given customer
     *
     * onResult -> success, txn ID, error
     */
    fun completeCharge(context: Context, amount: Long, store: Store,
                       paymentSource: String, requestTag: String? = null,
                       onResult: (Boolean, String?, Error?) -> Unit) {

        val networkService = NetworkService.getInstance(context)
        val currentCustomer = Customer.current

        // Check that current customer exists
        if (currentCustomer == null) {
            Log.e(TAG, "No current customer!")
            onResult(false, null, Error("No current customer"))
            return
        }

        // Get JWT, if successful, call backend to charge
        JWT.getToken(context, requestTag) { success, jwt, error ->

            if (success && jwt != null) {
                // JWT retrieval success, we can now get an ephemeral key
                val chargeParams = hashMapOf(
                    "token" to jwt.token,
                    "customer_id" to currentCustomer.stripeID,
                    "amount" to amount,
                    "source" to paymentSource,
                    "firebase_store_id" to store.id
                )
                // POST the request to our backend server
                val chargeCustomerRequest = JsonObjectRequest(Request.Method.POST,
                    BackendConstants.CHARGE_URL,
                    JSONObject(chargeParams),
                    Response.Listener { response ->
                        // Check that backend returned properly
                        if (!response.isNull("charge_id")) {
                            // Success - call the callback
                            onResult(true, response.getString("charge_id"), null)
                        } else {
                            // Charge ID not given
                            Log.e(TAG, "Got a success code but charge ID (txn ID) was not given in returned json")
                            onResult(false, null, Error("Incorrect backend response. Response: ${response.toString(2)}"))
                        }
                    },
                    Response.ErrorListener { networkError ->
                        // Error from backend
                        Log.e(TAG, "Backend network call errored while charging customer")
                        onResult(false, null, Error("${networkError?.message}"))
                    })
                // Disable retries
                chargeCustomerRequest.retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                // Add the request to the network queue
                networkService.addToRequestQueue(chargeCustomerRequest, requestTag)

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
                onResult(false, null, Error("${error?.networkResponse?.data}"))
            })
        // Add the request to the network queue
        networkService.addToRequestQueue(getJWTRequest, requestTag)

    }

    // This is used to compute Stripe cost in cents
    fun getStripeCost(cost: Double): Long {
        return (cost*100).toLong()
    }

}