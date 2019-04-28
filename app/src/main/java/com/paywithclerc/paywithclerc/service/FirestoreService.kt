package com.paywithclerc.paywithclerc.service

import android.content.Context
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
    fun loadCustomer(user: FirebaseUser, context: Context, networkTag: String? = null, onResult: (Boolean, Customer?, Error?) -> Unit) {
        val db = getFirestore()
        // Try to get the document for the user
        val userDocRef = db.collection(FirestoreConstants.CUSTOMERS_COL).document(user.uid)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.data != null) {
                    // User exists - get their data
                    val docData = document.data!!
                    val stripeId = docData["stripeId"] as String?
                    // stripeID should always exist, but we check just in case
                    if (stripeId != null) {
                        val customer = Customer(user.uid, stripeId, user.displayName, user.email)
                        onResult(true, customer, null)
                    } else {
                        // Something is really wrong - user doc exists but stripeID doesn't
                        Log.e(TAG, "User document with ID ${user.uid} exists but without Stripe ID - Look into this!")
                        onResult(false, null, Error("User document exists but no Stripe ID was found"))
                    }
                } else {
                    // User does not exist - create Stripe customer first
                    Log.i(TAG, "Customer ${user.uid} does not exist in Firebase, creating Stripe customer")
                    BackendService.createCustomer(context, networkTag) { success, stripeId, error ->
                        // Check the status of create customer call
                        if (success && stripeId != null) {
                            // Stripe ID created successfully - save to firebase
                            val userData = HashMap<String, Any>()
                            userData["stripeId"] = stripeId
                            userDocRef.set(userData)
                                .addOnSuccessListener {
                                    // Successfully written to Firebase
                                    Log.i(TAG, "Customer successfully created with new stripe ID")
                                    val customer = Customer(user.uid, stripeId, user.displayName, user.email)
                                    onResult(true, customer, null)
                                }
                                .addOnFailureListener {
                                    // Adding stripe ID to firestore failed
                                    Log.e(TAG, "Was not able to add new stripe ID to the Firestore Customer. Error: ${error?.message}")
                                    onResult(false, null, error)
                                }
                        } else {
                            // Backend call failed
                            Log.e(TAG, "Call to our backend to create new customer failed.")
                            onResult(false, null, error)
                        }
                    }
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
                if (document != null && document.data != null) {
                    val docData = document.data!!
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