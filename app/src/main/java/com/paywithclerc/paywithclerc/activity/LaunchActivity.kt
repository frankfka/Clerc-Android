package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paywithclerc.paywithclerc.R
import android.content.Intent
import android.util.Log
import com.paywithclerc.paywithclerc.constant.StripeConstants
import com.paywithclerc.paywithclerc.model.Transaction
import com.paywithclerc.paywithclerc.service.*
import com.stripe.android.PaymentConfiguration
import io.realm.Realm

/**
 * This activity contains the splash screen & will redirect users
 * to the correct activity (Home or Login) depending on current state
 */
class LaunchActivity : AppCompatActivity() {

    /**
     * Does all the setup & loading for the application, then redirects to the proper activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        // Configure realm
        RealmService.initializeRealm(this)
        // Configure Stripe
        PaymentConfiguration.init(StripeConstants.PUBLISHABLE_KEY)
        // Try to load current user
        val currentUser = FirebaseAuthService.getCurrentUser()
        if (currentUser != null) {
            // Check if user is signed in (non-null) and send to appropriate activity if their stripe info is loaded
            FirestoreService.loadCustomer(currentUser, this, TAG) { success, customer, error ->
                if (success && customer != null) {
                    Log.i(TAG, "Customer ${currentUser.uid} successfully loaded from firestore with Stripe info")
                    // Load the customer session
                    SessionService.loadCustomerSession(customer, this, TAG) { sessionLoadSuccess, sessionLoadError ->
                        if (sessionLoadSuccess) {
                            // Customer successfully loaded - go to home screen
                            Log.i(TAG, "Customer session loaded")
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            // Something went wrong, redirect back to login
                            Log.e(TAG, "Initializing customer session failed with error ${sessionLoadError?.message}")
                            // Go back to login
                            goToLogin()
                        }
                    }
                } else {
                    // Failed because the customer could not be loaded from firestore/with stripe
                    Log.e(TAG, "Customer logged in, but Stripe initialization failed with error: ${error?.message}")
                    // Go back to login
                    goToLogin()
                }
            }
        } else {
            // No user signed in - go to Login
            goToLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkService.getInstance(this).removeFromRequestQueue(TAG)
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {

        const val TAG = "PAYWITHCLERCAPP: LaunchActivity"

    }

}
