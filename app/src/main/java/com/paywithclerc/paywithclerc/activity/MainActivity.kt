package com.paywithclerc.paywithclerc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.FirestoreService
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.R


class MainActivity : AppCompatActivity() {

    /**
     * Constructor for the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startShoppingButton.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivityForResult(intent, ActivityConstants.STORE_BARCODE_INTENT)
        }
    }

    /**
     * Called on return from an activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Returning from scanning a store barcode
        if (requestCode == ActivityConstants.STORE_BARCODE_INTENT) {
            // Check for the result
            if (resultCode == Activity.RESULT_OK) {
                val barcode = data!!.getStringExtra(ActivityConstants.BARCODE_EXTRA_KEY)
                returnedFromScanningBarcode(barcode)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                Log.e(TAG, "Cancelled scanning barcode")
            }
        }

        // Returning from another activity

    }

    /**
     * Called when we return from barcode scanning
     * There may be an error, in which case the barcode is null
     */
    private fun returnedFromScanningBarcode(barcode: String?) {
        if (barcode != null) {
            // Start loading animation
            val loadingHUD = ViewService.showLoadingHUD(this, mainParentConstraintLayout, "Searching for Store…")
            // Try to get from Firestore
            FirestoreService.getStore(barcode) { success: Boolean, store: Store?, error: Error? ->
                // When done, dismiss loading
                ViewService.dismissLoadingHUD(loadingHUD)
                // Check that we have a success
                if (success && store != null) {
                    // Go to shopping screen
                    val intent = Intent(this, ShoppingActivity::class.java)
                    intent.putExtra(ActivityConstants.STORE_OBJ_KEY, store)
                    startActivity(intent)
                } else {
                    Log.e(TAG, "Error while getting store from barcode $barcode, Error: ${error?.message}")
                    // Show error HUD
                    ViewService.showErrorHUD(this, mainParentConstraintLayout, "No store found. Please try again.")
                }
            }
        } else {
            // An error occured while scanning
            Log.e(TAG, "Returned from scanning barcode but the barcode is somehow null")
            ViewService.showErrorHUD(this, mainParentConstraintLayout)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
