package com.paywithclerc.paywithclerc.activity

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ActivityConstants
import com.paywithclerc.paywithclerc.model.Error
import com.paywithclerc.paywithclerc.model.PriceUnit
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Store
import com.paywithclerc.paywithclerc.service.FirestoreService
import com.paywithclerc.paywithclerc.service.UtilityService
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.view.adapter.ItemListAdapter
import kotlinx.android.synthetic.main.activity_shopping.*

class ShoppingActivity : AppCompatActivity() {

    private var store: Store? = null
    private var items: MutableList<Product> = ArrayList()
    private var quantities: MutableList<Double> = ArrayList()
    private var itemListAdapter: ItemListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        // Get the store item
        val scannedStore = intent.getParcelableExtra<Store>(ActivityConstants.STORE_OBJ_KEY)
        if (scannedStore != null) {
            store = scannedStore
            initializeUI()
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

    /**
     * Called on return from an activity
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // Returning from scanning a store barcode
        if (requestCode == ActivityConstants.SHOPPING_BARCODE_INTENT) {
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
            val loadingHUD = ViewService.showLoadingHUD(this, shoppingParentConstraintLayout, "Searching for Item…")
            // Try to get from Firestore
            FirestoreService.getProduct(barcode, store!!.id) { success: Boolean, product: Product?, error: Error? ->
                // When done, dismiss loading
                ViewService.dismissLoadingHUD(loadingHUD)
                // Check that we have a success
                if (success && product != null) {
                    // Item scanned successfully
                    Log.i(TAG, "Product with ID ${product.id} found, name: ${product.name}")
                    // First switch depending on the unit type
                    if (product.priceUnit == PriceUnit.UNIT) {
                        // See if the item has already been scanned - in which case just increment quantity
                        val scannedProductIndex = items.indexOf(product)
                        if (scannedProductIndex == -1) {
                            // Item does not yet exist in the cart, add it
                            items.add(product)
                            quantities.add(1.0)
                        } else {
                            // Item exists in cart, increment quantity
                            quantities[scannedProductIndex] = quantities[scannedProductIndex] + 1
                        }
                        // Show success dialog
                        ViewService.showSuccessHUD(this, shoppingParentConstraintLayout, "Item Added")
                        updateUI()
                    } else {
                        // Weighed item - always show dialog
                        // This is quantity - default to 1 - if we find item in the cart, update this
                        var weightQty = 1.0
                        val isInCart = items.indexOf(product) != -1
                        if (isInCart) {
                            weightQty = quantities[items.indexOf(product)]
                        }
                        ViewService.showEditWeighedItemDialog(this, product, weightQty) { specifiedQty ->
                            if (isInCart) {
                                quantities[items.indexOf(product)] = specifiedQty
                            } else {
                                items.add(product)
                                quantities.add(specifiedQty)
                            }
                            // Show success dialog and update UI
                            ViewService.showSuccessHUD(this, shoppingParentConstraintLayout, "Item Added")
                            updateUI()
                        }
                    }
                } else {
                    Log.e(TAG, "Error while getting item with barcode $barcode from store ID ${store?.id}, Error: ${error?.message}")
                    // Show error HUD
                    ViewService.showErrorHUD(this, shoppingParentConstraintLayout, "No item found. Please try again.")
                }
            }
        } else {
            // An error occured while scanning
            Log.e(TAG, "Returned from scanning barcode but the barcode is somehow null")
            ViewService.showErrorHUD(this, shoppingParentConstraintLayout)
        }
    }

    // Shows confirmation dialog
    private fun exitShopping() {
        ViewService.showConfirmDialog(this, "Exit Shopping", "Are you sure you want to stop shopping?",
            confirmClickListener = DialogInterface.OnClickListener { _, _ ->
                // Navigate back
                super.onBackPressed()
            })
    }

    // Initializes all the UI elements
    private fun initializeUI() {
        // Initialize UI with store information
        shoppingStoreTitle.text = store!!.name
        // Initialize the RecyclerView
        itemListAdapter = ItemListAdapter(items, quantities) { product ->
            val productIndex = items.indexOf(product)
            // Sanity check to make sure that returned product is actually in the cart
            if (productIndex >= 0) {
                // Switch depending on whether the product is by unit or by weight
                // TODO these are very similar, see if we can simplify somehow
                if (product.priceUnit == PriceUnit.UNIT) {
                    // Display dialog to edit unit quantity
                    // IMPORTANT - we cast to int here to ensure that the quantity here is always a whole number
                    ViewService.showEditUnitItemDialog(this, product, quantities[items.indexOf(product)].toInt()) { newQty ->
                        if (newQty > 0) {
                            // Changing the quantity
                            quantities[productIndex] = newQty.toDouble()
                        } else {
                            // Remove the item
                            quantities.removeAt(productIndex)
                            items.removeAt(productIndex)
                        }
                        updateUI()
                    }
                } else {
                    // Display dialog to edit double quantity
                    ViewService.showEditWeighedItemDialog(this, product, quantities[items.indexOf(product)]) { newQty ->
                        if (newQty > 0) {
                            // Changing the quantity
                            quantities[productIndex] = newQty
                        } else {
                            // Remove the item
                            quantities.removeAt(productIndex)
                            items.removeAt(productIndex)
                        }
                        updateUI()
                    }
                }
            } else {
                Log.e(TAG, "Clicked product is somehow not in the shopping cart")
            }
        }
        shoppingItemsRecycler.apply {
            layoutManager = LinearLayoutManager(this@ShoppingActivity)
            adapter = itemListAdapter
        }
        // Set up on-click listeners
        shoppingCancelButton.setOnClickListener {
            exitShopping()
        }
        shoppingCheckoutButton.setOnClickListener {
            // Go to checkout screen
            val intent = Intent(this, CheckoutActivity::class.java)
            // Add the store, items & quantities to the intent
            intent.putExtra(ActivityConstants.STORE_OBJ_KEY, store!!)
            intent.putParcelableArrayListExtra(ActivityConstants.ITEMS_KEY, ArrayList(items))
            intent.putExtra(ActivityConstants.QTYS_KEY, ArrayList(quantities))
            startActivity(intent)
        }
        shoppingClearCartButton.setOnClickListener {
            ViewService.showConfirmDialog(this, "Clear Cart", "Are you sure you want to clear the cart?",
                confirmClickListener = DialogInterface.OnClickListener { _, _ ->
                    items.clear()
                    quantities.clear()
                    updateUI()
                })
        }
        shoppingAddItemFAB.setOnClickListener {
            // When add item is clicked, transition to the scanner view
            val intent = Intent(this, BarcodeScannerActivity::class.java)
            startActivityForResult(intent, ActivityConstants.SHOPPING_BARCODE_INTENT)
        }
        // Run update UI once
        updateUI()
    }

    // Update UI based on state
    private fun updateUI() {
        // Update cost label
        val subtotal = UtilityService.getTotalCost(items, quantities)
        val taxes = UtilityService.getTaxes(subtotal, store!!)
        shoppingTotalAmount.text = ViewService.getFormattedCost(subtotal + taxes)
        shoppingSubtotal.text = ViewService.getFormattedCost(subtotal)
        shoppingTaxes.text = ViewService.getFormattedCost(taxes)
        // Disable checkout button if we don't have anything in cart
        if (items.size == 0) {
            shoppingCheckoutButton.isEnabled = false
            shoppingClearCartButton.isEnabled = false
            shoppingClearCartButton.setTextColor(resources.getColor(R.color.colorPrimaryDisabled, null))
        } else {
            shoppingCheckoutButton.isEnabled = true
            shoppingClearCartButton.isEnabled = true
            shoppingClearCartButton.setTextColor(resources.getColor(R.color.colorPrimary, null))
        }
        // Update Recycler
        itemListAdapter?.notifyDataSetChanged()
    }

    companion object {

        const val TAG = "Shopping Activity"

    }

}
