package com.paywithclerc.paywithclerc.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.paywithclerc.paywithclerc.constant.FirestoreConstants
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store

object FirestoreService {

    private const val TAG = "FirestoreService"

    /**
     * Retrieves a store with the given ID & calls the onResult callback when retrieved
     *
     * onResult -> (success, store object, error object)
     */
    fun getStore(id: String, onResult: (Boolean, Store?, Error?) -> Unit) {
        val db = getFirestore()
        db.collection(FirestoreConstants.STORES_COL)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val docData = document.data!! // Data should exist if the doc exists
                    val storeName = docData["name"] as String?
                    // Check that the fields are actually retrievable
                    if (storeName != null) {
                        onResult(true, Store(id, storeName), null)
                    } else {
                        onResult(false, null, Error("Store $id found but was missing parameters"))
                    }
                } else {
                    Log.e(TAG, "No such document was found")
                    onResult(false, null, Error("No store found for id $id"))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Document retrieval failed with $exception")
                onResult(false, null, Error("$exception"))
            }
    }

    // Gets a Firestore database instance
    private fun getFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}