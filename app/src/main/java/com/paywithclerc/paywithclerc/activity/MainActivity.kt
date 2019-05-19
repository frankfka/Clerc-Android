package com.paywithclerc.paywithclerc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import kotlinx.android.synthetic.main.activity_main.*
import android.app.Activity
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.FirestoreService
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.model.Transaction
import com.paywithclerc.paywithclerc.view.adapter.TransactionsListAdapter
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort

class MainActivity : AppCompatActivity() {

    private lateinit var txnsListAdapter: TransactionsListAdapter
    private lateinit var pastTransactions: RealmResults<Transaction>

    /**
     * Constructor for the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get the transactions but limit it to a specific number, since recyclerview in nestedscrollview has performance issues
        pastTransactions = Realm.getDefaultInstance().where(Transaction::class.java)
            .sort("txnDate", Sort.DESCENDING)
            .limit(NUM_PAST_TXNS)
            .findAll()
        // Add a change listener so that we are notified of list changes
        pastTransactions.addChangeListener { results ->
            pastTransactions = results
            updateUI()
        }

        // Initialize UI
        initializeUI()

    }

    override fun onResume() {
        super.onResume()
        // Update UI so that new transactions are reflected
        updateUI()
    }

    private fun initializeUI() {
        // Initialize the RecyclerView - will be empty if no transactions are found
        txnsListAdapter = TransactionsListAdapter(pastTransactions) { } // Do nothing on tap
        mainPrimaryPurchasesRecycler.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = txnsListAdapter
        }
        // Set listeners
        startShoppingButton.setOnClickListener {
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivityForResult(intent, ActivityConstants.STORE_BARCODE_INTENT)
        }
        mainPrimaryTransactionsViewAllButton.setOnClickListener {
            val intent = Intent(this, AllTransactionsActivity::class.java)
            startActivity(intent)
        }
        // Run update UI
        updateUI()
    }

    private fun updateUI() {
        // Set view visibility
        if (pastTransactions.isEmpty()) {
            mainNoTxnsLayout.isVisible = true
            mainPrimaryConstraintLayout.isVisible = false
        } else {
            mainNoTxnsLayout.isVisible = false
            mainPrimaryConstraintLayout.isVisible = true
            txnsListAdapter.notifyDataSetChanged()
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
        private const val NUM_PAST_TXNS = 10L // TODO Hack until we decide what to do with the home screen
    }
}
