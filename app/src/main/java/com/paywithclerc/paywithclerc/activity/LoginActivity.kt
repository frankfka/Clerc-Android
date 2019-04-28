package com.paywithclerc.paywithclerc.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.service.FirebaseAuthService
import com.paywithclerc.paywithclerc.service.FirestoreService
import com.paywithclerc.paywithclerc.service.NetworkService
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.view.hud.ErrorHUD
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Add listener to login button
        loginButton.setOnClickListener {
            Log.d(TAG, "Login clicked")
            // Choose authentication providers
            val providers = arrayListOf(AuthUI.IdpConfig.GoogleBuilder().build())
            // Create and launch sign-in intent
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build(),
                ActivityConstants.FIREBASE_LOGIN_INTENT)
        }
    }

    // Remove all pending requests if the activity gets destroyed
    override fun onDestroy() {
        super.onDestroy()
        NetworkService.getInstance(this).removeFromRequestQueue(TAG)
    }

    /**
     * Called on return of an activity
     *
     * FIREBASE_LOGIN_INTENT: returns from Firebase Auth UI. Will attempt to initialize the customer from
     *                          Firestore or hit our backend for Stripe information
     *
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Returning from Firebase Auth Sign-in UI
        if (requestCode == ActivityConstants.FIREBASE_LOGIN_INTENT) {
            // Get response from data
            val authResponse = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Try to initialize user with Firestore information
                val currentUser = FirebaseAuthService.getCurrentUser()!!
                FirestoreService.loadCustomer(currentUser, this, TAG) { success, customer, error ->
                    if (success && customer != null) {
                        Log.i(TAG, "Customer ${currentUser.uid} successfully loaded with Stripe info")
                        // Customer successfully loaded - go to home screen
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        // Failed because the customer could not be loaded from firestore/with stripe
                        Log.e(TAG, "Customer logged in, but Stripe initialization failed with error: ${error?.message}")
                        ViewService.showErrorHUD(this, loginParentConstraintLayout)
                    }
                }
            } else {
                if (authResponse == null) {
                    // User cancelled - do nothing
                } else {
                    // An error occured with Firebase Auth
                    Log.e(TAG, "Firebase Auth failed. Code: ${authResponse.error?.errorCode}")
                    ViewService.showErrorHUD(this, loginParentConstraintLayout)
                }
            }
        }
    }

    companion object {

        private const val TAG = "LoginActivity"

    }

}
