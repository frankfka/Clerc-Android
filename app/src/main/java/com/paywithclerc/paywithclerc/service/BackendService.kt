package com.paywithclerc.paywithclerc.service

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.paywithclerc.paywithclerc.constant.BackendConstants
import com.paywithclerc.paywithclerc.model.Error

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
                Log.e(TAG, "Backend network call errored")
                onResult(false, null, Error("Backend network call errored: ${error.networkResponse.data}"))
            })
        // If a request tag is given - add it to the request
        if (requestTag != null) {
            createCustomerRequest.tag = requestTag
        }
        // Add the request to the network queue
        networkService.addToRequestQueue(createCustomerRequest)
    }

    // This is used to compute Stripe cost in cents
    fun getStripeCost(cost: Double): Int {
        return (cost*100).toInt()
    }



}