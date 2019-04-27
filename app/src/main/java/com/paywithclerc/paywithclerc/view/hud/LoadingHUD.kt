package com.paywithclerc.paywithclerc.view.hud

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R

class LoadingHUD(context: Context, text:String) : HUD(context, text) {

    /**
     * Shows the loading HUD in the parent layout
     */
    override fun placeInParent(parentLayout: ConstraintLayout) {
        val inflatedView = View.inflate(context, R.layout.loading_hud, this)
        // Update text
        updateText()
        // Layout the view in the parent
        layout(parentLayout, inflatedView)
    }

    override fun getHUDImage(): Drawable? {
        return null
    }
}