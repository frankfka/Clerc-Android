package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.service.FirebaseAuthService
import android.content.Intent

/**
 * This activity contains the splash screen & will redirect users
 * to the correct activity (Home or Login) depending on current state
 */
class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        // Hide the app bar
        supportActionBar?.hide()
    }

    // Do any required loading here
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and send to appropriate activity
        var intent: Intent = if (FirebaseAuthService.getCurrentUser() != null) {
            // Go to home
            Intent(this, MainActivity::class.java)
        } else {
            // Go to login
            Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

}
