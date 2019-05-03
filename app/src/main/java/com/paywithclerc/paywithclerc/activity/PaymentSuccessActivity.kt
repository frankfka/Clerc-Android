package com.paywithclerc.paywithclerc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.paywithclerc.paywithclerc.R
import kotlinx.android.synthetic.main.activity_payment_success.*

class PaymentSuccessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // Dismiss the activity when pressed
        paymentSuccessDismissButton.setOnClickListener {
            goToHome()
        }

    }

    // Since all prior activities are cleared, set Home as the "previous" activity
    override fun onBackPressed() {
        goToHome()
    }

    /**
     * Go to home activity
     */
    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
