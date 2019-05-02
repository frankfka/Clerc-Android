package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.service.FirebaseAuthService
import android.content.Intent
import android.util.Log
import com.paywithclerc.paywithclerc.model.Customer
import com.paywithclerc.paywithclerc.service.FirestoreService
import com.paywithclerc.paywithclerc.service.NetworkService

/**
 * This activity contains the splash screen & will redirect users
 * to the correct activity (Home or Login) depending on current state
 */
class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkService.getInstance(this).removeFromRequestQueue(TAG)
    }

    // Do any required loading here
    public override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuthService.getCurrentUser()
        if (currentUser != null) {
            // Check if user is signed in (non-null) and send to appropriate activity if their stripe info is loaded
            FirestoreService.loadCustomer(currentUser, this, TAG) { success, customer, error ->
                if (success && customer != null) {
                    Log.i(TAG, "Customer ${currentUser.uid} successfully loaded with Stripe info")
                    // Initialize the current customer object
                    Customer.current = customer
                    // Customer successfully loaded - go to home screen
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Failed because the customer could not be loaded from firestore/with stripe
                    Log.e(TAG, "Customer logged in, but Stripe initialization failed with error: ${error?.message}")
                    // Go back to login
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        } else {
            // No user signed in - go to Login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    companion object {

        const val TAG = "LaunchActivity"

    }

}
