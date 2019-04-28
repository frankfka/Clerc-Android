package com.paywithclerc.paywithclerc.service

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Singleton used for networking
 */
class NetworkService constructor(context: Context) {

    companion object {
        @Volatile
        private var SHARED: NetworkService? = null
        fun getInstance(context: Context) =
            SHARED ?: synchronized(this) {
                SHARED ?: NetworkService(context).also {
                    SHARED = it
                }
            }
    }

    // Shared Request Queue
    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    // Adds a request to the request queue
    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    // Removes all requests with a given tag from the queue
    // Should be used when we exit from activities so that requests don't stay in purgatory
    fun removeFromRequestQueue(tag: String) {
        requestQueue.cancelAll(tag)
    }

}