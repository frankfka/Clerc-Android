package com.paywithclerc.paywithclerc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.paywithclerc.paywithclerc.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startShoppingButton.setOnClickListener {
            Log.i("logtag", "Clicked!")
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivity(intent)
        }
    }
}
