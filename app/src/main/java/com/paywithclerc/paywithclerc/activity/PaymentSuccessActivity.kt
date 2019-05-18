package com.paywithclerc.paywithclerc.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.BackendService
import com.paywithclerc.paywithclerc.service.UtilityService
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.view.adapter.ItemListAdapter
import kotlinx.android.synthetic.main.activity_checkout.*
import kotlinx.android.synthetic.main.activity_payment_success.*

class PaymentSuccessActivity : AppCompatActivity() {

    private var store: Store? = null
    private var items: List<Product>? = null
    private var quantities: List<Int>? = null
    private var txnId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        // Get the extras to populate the views
        store = intent.getParcelableExtra(ActivityConstants.STORE_OBJ_KEY)
        items = intent.getParcelableArrayListExtra(ActivityConstants.ITEMS_KEY)
        quantities = intent.getIntegerArrayListExtra(ActivityConstants.QTYS_KEY)
        txnId = intent.getStringExtra(ActivityConstants.TXN_ID_KEY)

        // If anything was not passed, just go to home
        if (store != null && items != null && quantities != null && txnId != null) {
            initializeUI()
        } else {
            Log.e(LOGTAG, "Required intent extras were not passed")
            goToHome()
        }

    }

    // Since all prior activities are cleared, set Home as the "previous" activity
    override fun onBackPressed() {
        goToHome()
    }

    private fun initializeUI() {

        // Calculate totals
        val subtotal = UtilityService.getTotalCost(items!!, quantities!!)
        val taxes = UtilityService.getTaxes(subtotal, store!!)

        // Initialize Text
        paymentSuccessSubtotal.text = ViewService.getFormattedCost(subtotal)
        paymentSuccessTaxes.text = ViewService.getFormattedCost(taxes)
        paymentSuccessTotalAmount.text = ViewService.getFormattedCost(subtotal + taxes)
        paymentSuccessStoreNameText.text = store!!.name
        // Custom retailer text
        if (store!!.successMessage != null) {
            paymentSuccessHeaderDescription.text = store!!.successMessage
        }

        // RecyclerView
        val itemListAdapter = ItemListAdapter(items!!, quantities!!) { } // Not passing an on-click function
        paymentSuccessItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PaymentSuccessActivity)
            adapter = itemListAdapter
        }

        // Dismiss the activity when pressed
        paymentSuccessDismissButton.setOnClickListener {
            goToHome()
        }
        // Email Receipt
        paymentSuccessEmailReceiptButton.setOnClickListener {
            // Start loading HUD
            val loadingHUD = ViewService.showLoadingHUD(this, paymentSuccessMainConstraintLayout)
            // Disable email button
            paymentSuccessEmailReceiptButton.isEnabled = false
            BackendService.emailReceipt(txnId!!, this, LOGTAG) { success ->
                // End loading HUD
                ViewService.dismissLoadingHUD(loadingHUD)
                if (success) {
                    // Show success
                    ViewService.showInfoDialog(this, "Email Receipt",
                        "An email has been sent. If you do not see it in your inbox, please check your spam folder.")
                } else {
                    // Show error dialog & reenable email button to try again
                    Log.e(LOGTAG, "Could not send email receipt")
                    paymentSuccessEmailReceiptButton.isEnabled = true
                    ViewService.showErrorHUD(this, paymentSuccessMainConstraintLayout)
                }
            }
        }
    }

    /**
     * Go to home activity
     */
    private fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    companion object {
        const val LOGTAG = "PaymentSuccessActivity"
    }

}
