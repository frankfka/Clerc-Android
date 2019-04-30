package com.paywithclerc.paywithclerc.service

import android.app.Activity
import android.content.ClipDescription
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.activity.MainActivity
import com.paywithclerc.paywithclerc.constant.ViewConstants
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.view.hud.ErrorHUD
import com.paywithclerc.paywithclerc.view.hud.LoadingHUD
import com.paywithclerc.paywithclerc.view.hud.SuccessHUD
import com.paywithclerc.paywithclerc.view.misc.NumberStepper
import java.text.NumberFormat

object ViewService {

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
    fun dismissLoadingHUD(loadingHUD: LoadingHUD) {
        loadingHUD.removeFromParent()
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
        AlertDialog.Builder(activity)
            .setView(dialogView)
            .setPositiveButton(confirmBtnText ?: "Confirm", confirmClickListener)
            .setNegativeButton(cancelBtnText ?: "Cancel", cancelClickListener)
            .create()
            .show()
    }

    /**
     * Shows an edit item dialog, used in ShoppingActivity
     */
    fun showEditItemDialog(activity: Activity, product: Product, quantity: Int,
                           onUpdate: (Int) -> (Unit)) {
        // Get the main content view
        val viewGroup = activity.findViewById<ViewGroup>(R.id.content)
        // Inflate our custom dialog within the activity
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.edit_item_dialog, viewGroup, false)
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
        // TODO This stuff should probably be done in a constructor - but how?
        quantityStepper.count = quantity
        val individualCost = product.cost
        totalCostLabel.text = getFormattedCost(individualCost * quantity)
        individualCostLabel.text = "${getFormattedCost(individualCost)} ea."
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
     * Gets a formatted string for the given cost
     */
    fun getFormattedCost(cost: Double): String {
        return NumberFormat.getCurrencyInstance().format(cost)
    }

}