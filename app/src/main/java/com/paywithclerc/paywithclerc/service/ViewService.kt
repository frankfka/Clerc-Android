package com.paywithclerc.paywithclerc.service

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ViewConstants
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Transaction
import com.paywithclerc.paywithclerc.view.hud.ErrorHUD
import com.paywithclerc.paywithclerc.view.hud.LoadingHUD
import com.paywithclerc.paywithclerc.view.hud.SuccessHUD
import com.paywithclerc.paywithclerc.view.misc.NumberStepper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ViewService {

    private const val DEFAULT_DATE_FORMAT = "MMM dd, yyyy"

    /**
     * Show error HUD
     */
    fun showErrorHUD(context: Context, parentConstraintLayout: ConstraintLayout, message: String? = null) {
        val errorHint = message ?: context.resources.getString(R.string.default_error)
        val errorHUD = ErrorHUD(context, errorHint)
        errorHUD.placeInParent(parentConstraintLayout, ViewConstants.HUD_SHOW_TIME)
    }

    /**
     * Show success HUD
     */
    fun showSuccessHUD(context: Context, parentConstraintLayout: ConstraintLayout, message: String? = null) {
        val successHint = message ?: context.resources.getString(R.string.default_success)
        val successHUD = SuccessHUD(context, successHint)
        successHUD.placeInParent(parentConstraintLayout, ViewConstants.HUD_SHOW_TIME)
    }

    /**
     * Show loading HUD and returns the view so that we can dismiss it later
     */
    fun showLoadingHUD(context: Context, parentConstraintLayout: ConstraintLayout, message: String? = null): LoadingHUD {
        val loadingHint = message ?: context.resources.getString(R.string.default_loading)
        val loadingHUD = LoadingHUD(context, loadingHint)
        loadingHUD.placeInParent(parentConstraintLayout)
        return loadingHUD
    }

    /**
     * Dismiss the loading HUD
     * In the future, we can look at animations
     */
    fun dismissLoadingHUD(loadingHUD: LoadingHUD?) {
        loadingHUD?.removeFromParent()
    }

    /**
     * Shows an info dialog - same as confirmation dialog but with only one button
     */
    fun showInfoDialog(activity: Activity, title: String, description: String) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.confirmation_dialog, viewGroup, false)
        dialogView.findViewById<TextView>(R.id.confirmDialogTitle).text = title
        dialogView.findViewById<TextView>(R.id.confirmDialogDescription).text = description
        // Build the dialog & show
        val alertDialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setNeutralButton(activity.applicationContext.getString(R.string.dialog_dismiss), null)
            .create()
        // Show the dialog
        alertDialog.show()
        // Customize the button colors
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL)
            ?.setTextColor(activity.resources.getColor(R.color.colorPrimary, null))
    }

    /**
     * Shows a confirmation dialog
     */
    fun showConfirmDialog(activity: Activity, title: String, description: String,
                         confirmClickListener: DialogInterface.OnClickListener,
                         cancelClickListener: DialogInterface.OnClickListener? = null,
                         confirmBtnText: String? = null, cancelBtnText: String? = null) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.confirmation_dialog, viewGroup, false)
        dialogView.findViewById<TextView>(R.id.confirmDialogTitle).text = title
        dialogView.findViewById<TextView>(R.id.confirmDialogDescription).text = description
        // Build the dialog & show
        val alertDialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .setPositiveButton(confirmBtnText ?: "Confirm", confirmClickListener)
            .setNegativeButton(cancelBtnText ?: "Cancel", cancelClickListener)
            .create()
        // Show the dialog
        alertDialog.show()
        // Customize the button colors
        val positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE)
        val negativeButton = alertDialog.getButton(Dialog.BUTTON_NEGATIVE)
        if (positiveButton != null && negativeButton != null) {
            positiveButton.setTextColor(activity.resources.getColor(R.color.colorPrimary, null))
            negativeButton.setTextColor(activity.resources.getColor(R.color.colorPrimary, null))
        }
    }

    // TODO a lot of this dialog stuff is similar - perhaps refactor
    /**
     * Shows an edit weighed item dialog, used in ShoppingActivity
     */
    fun showEditWeighedItemDialog(activity: Activity, product: Product, quantity: Double,
                                  onUpdate: (Double) -> Unit) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.edit_weighed_item_dialog, viewGroup, false)
        // This is the actual dialog object
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()
        // Get the views
        val productNameLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductName)
        val totalCostLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductTotalCost)
        val individualCostLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductIndividualCost)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.editItemDialogDeleteButton)
        val updateButton = dialogView.findViewById<TextView>(R.id.editItemDialogUpdateButton)
        // Weighed item specific
        val quantityInput = dialogView.findViewById<TextInputEditText>(R.id.editWeightItemWeightEditText)
        val weightUnitLabel = dialogView.findViewById<TextView>(R.id.editWeighedItemWeightUnit)

        var currentQuantity = quantity // Keeps track of current quantity

        // Initialize the views
        productNameLabel.text = product.name
        val individualCost = product.cost
        totalCostLabel.text = getFormattedCost(individualCost * currentQuantity)
        // Weighed item specific
        individualCostLabel.text = "${getFormattedCost(individualCost)} ${product.priceUnit.perUnitString}"
        weightUnitLabel.text = product.priceUnit.displayString
        quantityInput.setText(currentQuantity.toString())

        // Initialize Listeners
        quantityInput.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                // Text empty = 0, otherwise try parsing
                val parsedDouble = if (s.toString().isEmpty()) {
                    0.0
                } else {
                    s.toString().toDoubleOrNull()
                }
                if (parsedDouble != null) {
                    currentQuantity = parsedDouble
                    totalCostLabel.text = getFormattedCost(currentQuantity * individualCost)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
        deleteButton.setOnClickListener {
            // Call completion with 0 as quantity
            onUpdate(0.0)
            dialog.dismiss()
        }
        updateButton.setOnClickListener {
            onUpdate(currentQuantity)
            dialog.dismiss()
        }

        // Show the dialog
        dialog.show()
    }

    /**
     * Shows an edit item dialog, used in ShoppingActivity
     */
    fun showEditUnitItemDialog(activity: Activity, product: Product, quantity: Int,
                               onUpdate: (Int) -> (Unit)) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.edit_unit_item_dialog, viewGroup, false)
        // This is the actual dialog object
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()
        // Get the views
        val productNameLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductName)
        val totalCostLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductTotalCost)
        val individualCostLabel = dialogView.findViewById<TextView>(R.id.editItemDialogProductIndividualCost)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.editItemDialogDeleteButton)
        val updateButton = dialogView.findViewById<TextView>(R.id.editItemDialogUpdateButton)
        val quantityStepper = dialogView.findViewById<NumberStepper>(R.id.editItemDialogNumberStepper)
        // Initialize the views
        productNameLabel.text = product.name
        quantityStepper.count = quantity
        val individualCost = product.cost
        totalCostLabel.text = getFormattedCost(individualCost * quantity)
        individualCostLabel.text = "${getFormattedCost(individualCost)} ${product.priceUnit.perUnitString}"
        // Configure listeners
        quantityStepper.onValueChange = { newQty ->
            totalCostLabel.text = getFormattedCost(individualCost * newQty)
        }
        deleteButton.setOnClickListener {
            // Call completion with 0 as quantity
            onUpdate(0)
            dialog.dismiss()
        }
        updateButton.setOnClickListener {
            onUpdate(quantityStepper.count)
            dialog.dismiss()
        }
        // Show the dialog
        dialog.show()
    }

    /**
     * Shows a transaction detail dialog
     */
    fun showTxnDetailDialog(activity: Activity, txn: Transaction,
                            onEmailDispatched: (Dialog, Boolean) -> Unit) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.txn_detail_dialog, viewGroup, false)
        // This is the actual dialog object
        val dialog = AlertDialog.Builder(activity)
            .setView(dialogView)
            .create()
        // Get the views
        val storeNameLabel = dialogView.findViewById<TextView>(R.id.txnDetailStoreName)
        val totalCostLabel = dialogView.findViewById<TextView>(R.id.txnDetailTotalCost)
        val dateLabel = dialogView.findViewById<TextView>(R.id.txnDetailDate)
        val emailReceiptButton = dialogView.findViewById<TextView>(R.id.txnDetailEmailReceipt)
        // Initialize the views
        storeNameLabel.text = txn.storeName
        totalCostLabel.text = getFormattedCost(txn.amount)
        dateLabel.text = getFormattedDate(txn.txnDate)
        emailReceiptButton.setOnClickListener {
            BackendService.emailReceipt(txn.txnId, activity.applicationContext, null) { success ->
                onEmailDispatched(dialog, success)
            }
        }
        // Show the dialog
        dialog.show()
    }

    /**
     * Gets a formatted string for the given cost
     */
    fun getFormattedCost(cost: Double): String {
        return NumberFormat.getCurrencyInstance().format(cost)
    }

    /**
     * Gets a formatted date string
     */
    fun getFormattedDate(date: Date): String {
        val dateFormat = SimpleDateFormat(DEFAULT_DATE_FORMAT, Locale.CANADA)
        return dateFormat.format(date)
    }

}