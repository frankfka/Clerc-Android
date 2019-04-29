package com.paywithclerc.paywithclerc.activity

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.UtilityService
import com.paywithclerc.paywithclerc.service.ViewService
import kotlinx.android.synthetic.main.activity_shopping.*

class ShoppingActivity : AppCompatActivity() {

    private var store: Store? = null
    private var items: List<Product> = ArrayList()
    private var quantities: List<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        // Get the store item
        val scannedStore = intent.getParcelableExtra<Store>(ActivityConstants.STORE_OBJ_KEY)
        if (scannedStore != null) {
            store = scannedStore
            // Initialize UI with store information
            shoppingStoreTitle.text = store!!.name
            // Set up on-click listeners
            shoppingCancelButton.setOnClickListener {
                exitShopping()
            }
            shoppingCheckoutButton.setOnClickListener {
                Log.e(TAG, "Checkout clicked")
            }
            shoppingClearCartButton.setOnClickListener {
                Log.e(TAG, "Clear cart")
            }
            // Run update UI once
            updateUI()
        } else {
            Log.e(TAG, "No store item was passed to this activity")
            // Finish the activity so we don't run into any unknown flows
            finish()
        }
    }

    /**
     * Override so that we can show a confirmation dialog
     */
    override fun onBackPressed() {
        exitShopping()
    }

    // Shows confirmation dialog
    private fun exitShopping() {
        ViewService.showConfirmDialog(this, "Exit Shopping", "Are you sure you want to stop shopping?",
            confirmClickListener = DialogInterface.OnClickListener { dialog, which ->
                // Navigate back
                super.onBackPressed()
            })
    }

    // Update UI based on state
    private fun updateUI() {
        // Update cost label
        shoppingTotalAmount.text = ViewService.getFormattedCost(UtilityService.getTotalCost(items, quantities))
        // Update Recycler Stuff?
    }

    companion object {

        const val TAG = "Shopping Activity"

    }

}
