package com.paywithclerc.paywithclerc.activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.Nullable
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.view.hud.LoadingHUD
import com.stripe.android.*
import kotlinx.android.synthetic.main.activity_checkout.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.paywithclerc.paywithclerc.constant.StripeConstants
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.*
import com.paywithclerc.paywithclerc.service.stripe.PaymentCompletionService
import com.paywithclerc.paywithclerc.view.adapter.ItemListAdapter
import com.stripe.android.CustomerSession.CustomerRetrievalListener
import com.stripe.android.model.Customer


/**
 * This combines Stripe's PaymentSession & the activity. In the future we'd want to refactor this out
 */
class CheckoutActivity : AppCompatActivity() {

    // View variables
    private var loadingHUD: LoadingHUD? = null
    private var itemListAdapter: ItemListAdapter? = null

    // UI States
    private var shouldEnablePayButton = false
    private var shouldEnableCancelButton = true
    private var shouldEnableEditPayment = true

    // Passed from prior activity
    private var items: MutableList<Product>? = null
    private var quantities: MutableList<Int>? = null
    private var store: Store? = null
    private var amount: Double = 0.0

    // Payment States
    private var paymentSession: PaymentSession? = null
    private var isLoading = false
    private var paymentReadyToCharge = false
    private var paymentResult = PaymentResultListener.INCOMPLETE
    private var selectedPaymentId: String? = null
    private var errorMsg: String? = null // Indicates error state if non-null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Get the passed cart info
        store = intent.getParcelableExtra(ActivityConstants.STORE_OBJ_KEY)
        items = intent.getParcelableArrayListExtra(ActivityConstants.ITEMS_KEY)
        quantities = intent.getIntegerArrayListExtra(ActivityConstants.QTYS_KEY)
        // First thing to do is to set up the Payment Session - this is done synchronously
        setupPaymentSession()
        // Check that we have a success, otherwise just finish the activity
        if (store != null && paymentSession != null && items != null && quantities != null
            && items!!.size > 0 && quantities!!.size > 0 && items!!.size == quantities!!.size) {
            // Pass total amount to the payment session
            amount = UtilityService.getTotalCost(items!!, quantities!!)
            // Stripe cost is in cents
            val cartTotal = BackendService.getStripeCost(amount)
            paymentSession!!.setCartTotal(cartTotal)
            // Initialize UI
            initializeUI()
            // Update UI
            updateUI()
        } else {
            // Something failed
            Log.e(TAG, "Initialization of checkout failed. Either payment session did not initialize or items/qtys did not pass correctly")
            // Just finish activity for now - TODO show some sort of error
            finish()
        }

    }

    /**
     * Called on return from external activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // This handles all data changes from Stripe standard UI
        if (data != null) {
            paymentSession?.handlePaymentData(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // This ensures that the payment session ends successfully
        paymentSession?.onDestroy()
        // End all network requests - prevents charging if we exit the activity
        NetworkService.getInstance(this).removeFromRequestQueue(TAG)
    }

    /**
     * Override so that we can show a confirmation dialog
     */
    override fun onBackPressed() {
        exitCheckout()
    }

    private fun initializeUI() {
        // Update total cost
        checkoutTotalAmount.text = ViewService.getFormattedCost(UtilityService.getTotalCost(items!!, quantities!!))

        // Initialize the RecyclerView
        itemListAdapter = ItemListAdapter(items!!, quantities!!) { } // Not passing an on-click function
        checkoutCartItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CheckoutActivity)
            adapter = itemListAdapter
        }

        // Set Listeners
        checkoutCancelButton.setOnClickListener {
            exitCheckout()
        }
        checkoutPaymentMethodEditButton.setOnClickListener {
            paymentSession?.presentPaymentMethodSelection()
        }
        // PAY NOW BUTTON
        checkoutPayNowButton.setOnClickListener {
            // Ask for a confirmation
            ViewService.showConfirmDialog(this, "Confirm Payment", "Confirm payment of ${ViewService.getFormattedCost(amount)} to ${store?.name}",
                confirmClickListener = DialogInterface.OnClickListener { _, _ ->
                    // Payment has been confirmed

                    // First thing - disable all buttons
                    buttonsEnabled(editPaymentEnabled = false, payEnabled = false, cancelEnabled = false)
                    // Call Stripe to complete payment after checking that we have a valid state
                    if (paymentReadyToCharge && paymentResult != PaymentResultListener.SUCCESS) {
                        // Call the our PaymentCompletionService class to complete the payment via backend
                        val paymentCompletionService = PaymentCompletionService(this, store!!, TAG) { success, txnId, error ->
                            if (success && txnId != null) {
                                // Add to Firebase - NOTE: We force unwrap everything here - to be at this step
                                // it isn't possible for any of these items to be null
                                FirestoreService.writeTransaction(
                                    customerId = com.paywithclerc.paywithclerc.model.Customer.current!!.firebaseID,
                                    storeId = store!!.id,
                                    amount = amount,
                                    items = items!!,
                                    quantities = quantities!!,
                                    txnId = txnId) { firebaseTxnWriteSuccess, firebaseTxnWriteError ->
                                    // Not much need for a callback at this point
                                    Log.i(TAG, "Firestore transaction write. Success: $firebaseTxnWriteSuccess, Error: ${firebaseTxnWriteError?.message}")
                                }
                                // Add to Realm
                                RealmService.addTransaction(txnId, store!!.name, amount, StripeConstants.DEFAULT_CURRENCY)
                            } else {
                                // Errors handled elsewhere - just log
                                Log.e(TAG, "Transaction failed with error: ${error?.message}")
                            }
                        }
                        // Set to loading
                        isLoading = true
                        updateUI()
                        paymentSession?.completePayment(paymentCompletionService)
                    }
            })
        }

        // Run Update UI once
        updateUI()
    }

    // Calculates & updates views based on current state
    private fun updateUI() {

        // Calculate state for buttons
        updateViewState()

        // Loading UI
        if (isLoading && loadingHUD == null) { // Hack to make sure that we only have one loading HUD
            loadingHUD = ViewService.showLoadingHUD(this, checkoutParentConstraintLayout, "Please Wait")
        } else {
            ViewService.dismissLoadingHUD(loadingHUD)
            loadingHUD = null
        }

        // Payment Method
        if (selectedPaymentId != null) {
            // Get & display the source name
            CustomerSession.getInstance().retrieveCurrentCustomer(object : CustomerRetrievalListener {
                override fun onCustomerRetrieved(customer: Customer) {
                    // Fetches and displays the payment method
                    val displaySource = customer.getSourceById(selectedPaymentId!!)
                    val source = displaySource?.asSource()
                    // Check that we can extract a source & that the source metadata is populated
                    if (source != null && source.sourceTypeData.isNotEmpty()) {
                        val sourceData = source.sourceTypeData
                        // Display "Visa 4242"
                        checkoutPaymentMethod.text = "${sourceData["brand"]} ${sourceData["last4"]}"
                    } else {
                        // Somehow there is no source to extract details from - just display something generic
                        checkoutPaymentMethod.text = getString(R.string.checkout_payment_method_generic_text)
                    }
                }
                override fun onError(errorCode: Int, errorMessage: String?, stripeError: StripeError?) {
                    Log.e(TAG, "Error while retrieving customer: ${stripeError?.message}, $errorMessage")
                    errorMsg = errorMessage
                    updateUI()
                }
            })
        } else {
            // No payment method selected
            checkoutPaymentMethod.text = resources.getText(R.string.checkout_payment_method_none)
        }

        // Error UI
        if (errorMsg != null) {
            ViewService.showErrorHUD(this, checkoutParentConstraintLayout, "Something went wrong. Please try again.")
        }

        // Update buttons enabled/disabled
        buttonsEnabled(
            editPaymentEnabled = shouldEnableEditPayment,
            payEnabled = shouldEnablePayButton,
            cancelEnabled = shouldEnableCancelButton
        )

    }

    // Calculates the different states of the input
    private fun updateViewState() {
        // Disable all if loading
        if (isLoading) {
            // Disable all buttons
            shouldEnableCancelButton = false
            shouldEnableEditPayment = false
            shouldEnablePayButton = false
        } else {
            // Enable payment button if payment is ready to charge & current payment result is not success
            shouldEnablePayButton = paymentReadyToCharge && paymentResult != PaymentResultListener.SUCCESS
            shouldEnableCancelButton = true
            shouldEnableEditPayment = true
        }
    }

    // Calls SessionService to create a Payment Session
    private fun setupPaymentSession() {

        // Session listener will be called whenever some state changes
        // We call updateUI every time something changes
        val sessionListener = object : PaymentSession.PaymentSessionListener {

            // Determines whether Stripe is communicating
            override fun onCommunicatingStateChanged(isCommunicating: Boolean) {
                isLoading = isCommunicating
                updateUI()
            }

            // Determines if there was an error
            override fun onError(errorCode: Int, @Nullable errorMessage: String?) {
                Log.e(TAG, "Stripe Error $errorCode: $errorMessage")
                errorMsg = errorMessage
                updateUI()
            }

            // Called whenever some sort of data is changed regarding payments
            override fun onPaymentSessionDataChanged(data: PaymentSessionData) {
                paymentReadyToCharge = data.isPaymentReadyToCharge
                paymentResult = data.paymentResult
                selectedPaymentId = data.selectedPaymentMethodId

                when (paymentResult) {
                    PaymentResultListener.SUCCESS -> { // Payment succeeded
                        // Navigate to success activity
                        Log.i(TAG, "Payment was successful")
                        val paymentSuccessIntent = Intent(this@CheckoutActivity, PaymentSuccessActivity::class.java)
                        // Set flags to clear all previous activities - using bitwise OR
                        paymentSuccessIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(paymentSuccessIntent)
                    }
                    PaymentResultListener.ERROR -> { // Payment failed
                        Log.e(TAG, "Payment failed")
                        // Show some error message to the user
                        isLoading = false
                        updateUI()
                        ViewService.showErrorHUD(this@CheckoutActivity, checkoutParentConstraintLayout, "Payment Failed. Please try again.")
                    }
                    else -> // Normal updating payment method, etc.
                        updateUI()
                }
            }

        }
        // Try to initialize the payment session
        paymentSession = SessionService.loadPaymentSession(this, sessionListener)

    }

    // Helper method for cancel/back pressed dialogs
    private fun exitCheckout() {
        ViewService.showConfirmDialog(this, "Exit Checkout", "Are you sure you want to quit checkout?",
            confirmClickListener = DialogInterface.OnClickListener { _, _ ->
                // Navigate back
                super.onBackPressed()
            })
    }

    // Helper method to disable/enable buttons
    private fun buttonsEnabled(editPaymentEnabled: Boolean, payEnabled: Boolean, cancelEnabled: Boolean) {
        // Text buttons use the below colors, Pay Now button has custom background so no need to set colors
        val enabledTextColorPrimary = resources.getColor(R.color.colorPrimary, null)
        val disabledTextColorPrimary = resources.getColor(R.color.colorPrimaryDisabled, null)

        // Edit payment button
        if (editPaymentEnabled) {
            checkoutPaymentMethodEditButton.isEnabled = true
            checkoutPaymentMethodEditButton.setTextColor(enabledTextColorPrimary)
        } else {
            checkoutPaymentMethodEditButton.isEnabled = false
            checkoutPaymentMethodEditButton.setTextColor(disabledTextColorPrimary)
        }

        // Cancel button
        if (cancelEnabled) {
            checkoutCancelButton.isEnabled = true
            checkoutCancelButton.setTextColor(enabledTextColorPrimary)
        } else {
            checkoutCancelButton.isEnabled = false
            checkoutCancelButton.setTextColor(disabledTextColorPrimary)
        }

        // Pay button
        checkoutPayNowButton.isEnabled = payEnabled
    }

    companion object {
        const val TAG = "CheckoutActivity"
    }

}
