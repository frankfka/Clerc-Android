package com.paywithclerc.paywithclerc.service

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.paywithclerc.paywithclerc.constant.FirestoreConstants
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store

object FirestoreService {

    private const val TAG = "FirestoreService"

    /**
     * Saves a user to firestore and creates a new Stripe customer if one does not exist
     *
     * onResult -> (success, customer object, error object)
     */
    fun loadCustomer(user: FirebaseUser, onResult: (Boolean, Customer?, Error?) -> Unit) {
        val db = getFirestore()
        // Try to get the document for the user
        db.collection(FirestoreConstants.CUSTOMERS_COL)
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    // User exists - get their data
                    val docData = document.data!! // Data should exist if the doc exists
                    val stripeID = docData["stripeId"] as String?
                    // stripeID should always exist, but we check just in case
                    if (stripeID != null) {
                        val customer = Customer(user.uid, stripeID, user.displayName, user.email)
                        onResult(true, customer, null)
                    } else {
                        // Something is really wrong - user doc exists but stripeID doesn't
                        Log.e(TAG, "User document with ID ${user.uid} exists but without Stripe ID - Look into this!")
                        onResult(false, null, Error("User document exists but no Stripe ID was found"))
                    }
                } else {
                    // User does not exist - create Stripe customer first
                    Log.i(TAG, "Customer ${user.uid} does not exist in Firebase, creating Stripe customer")
//                    StripeService.shared.createCustomer() { (success, stripeId) in
//                    if success && stripeId != nil {
//                        // Stripe customer created, save to Firebase
//
//                        // We just save Stripe ID for now (safer) - do we want to save name/email?
//                        let userData: [String: Any] = ["stripeId": stripeId!]
//                        userDocRef.setData(userData) { error in
//                                if error == nil {
//                                    // Successful - return customer object
//                                    completion(true, Customer(firebaseID: user.uid, stripeID: stripeId!, name: user.displayName, email: user.email))
//                                } else {
//                                    // Unsuccessful
//                                    completion(false, nil)
//                                }
//                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Customer document retrieval failed with $exception")
                onResult(false, null, Error("$exception"))
            }

    }

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
                Log.e(TAG, "Store document retrieval failed with $exception")
                onResult(false, null, Error("$exception"))
            }
    }

    // Gets a Firestore database instance
    private fun getFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

}