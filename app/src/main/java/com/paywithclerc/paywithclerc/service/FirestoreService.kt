package com.paywithclerc.paywithclerc.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.paywithclerc.paywithclerc.constant.FirestoreConstants
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store

object FirestoreService {

    private const val TAG = "FirestoreService"

    fun getStore(id: String, onResult: (Boolean, Store?, Error?) -> Unit) {
        val db = getFirestore()
        db.collection(FirestoreConstants.STORES_COL)
            .document(id)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.e(TAG, "DocumentSnapshot data: ${document.data}")
                    onResult(true, null, null)
                } else {
                    Log.e(TAG, "No such document")
                    onResult(false, null, null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "get failed with ", exception)
                onResult(false, null, null)
            }
    }

    private fun getFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}