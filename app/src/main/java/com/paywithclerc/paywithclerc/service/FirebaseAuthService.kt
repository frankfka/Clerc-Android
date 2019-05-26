package com.paywithclerc.paywithclerc.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object FirebaseAuthService {

    private const val TAG = "PAYWITHCLERCAPP: FirestoreService"

    /**
     * Returns the current signed-in user or null
     */
    fun getCurrentUser(): FirebaseUser? {
        return getFirebaseAuth().currentUser
    }

    // Gets a Firestore database instance
    private fun getFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

}