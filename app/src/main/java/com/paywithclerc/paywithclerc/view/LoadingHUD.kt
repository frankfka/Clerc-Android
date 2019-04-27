package com.paywithclerc.paywithclerc.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.paywithclerc.paywithclerc.R
import kotlinx.android.synthetic.main.loading_hud.view.*
import androidx.constraintlayout.widget.ConstraintSet

class LoadingHUD: ConstraintLayout {

    private var hintText = ""
    private var view: View? = null
    private var parentLayout: ConstraintLayout? = null

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // Inflating via XML - not currently supported
        if (attrs != null) {
            val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingHUD)
            hintText = attributes.getString(R.styleable.LoadingHUD_text) ?: ""
            attributes.recycle()
        }
    }

    /**
     * Sets hintText displayed on the loading HUD
     */
    fun setHint(hint: String) {
        this.hintText = hint
    }

    /**
     * Shows the loading HUD in the parent layout
     */
    fun placeInParent(parentLayout: ConstraintLayout) {
        val inflatedView = View.inflate(context, R.layout.loading_hud, this)
        updateMessage()
        // Layout params for content width and height
        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        // Add to the parent layout
        inflatedView.id = View.generateViewId()
        inflatedView.layoutParams = layoutParams
        parentLayout.addView(inflatedView)
        // Constraints to connect to parent view
        val constraints = ConstraintSet()
        constraints.clone(parentLayout)
        constraints.connect(inflatedView.id, ConstraintSet.TOP, parentLayout.id, ConstraintSet.TOP, 0)
        constraints.connect(inflatedView.id, ConstraintSet.BOTTOM, parentLayout.id, ConstraintSet.BOTTOM, 0)
        constraints.connect(inflatedView.id, ConstraintSet.RIGHT, parentLayout.id, ConstraintSet.RIGHT, 0)
        constraints.connect(inflatedView.id, ConstraintSet.LEFT, parentLayout.id, ConstraintSet.LEFT, 0)
        constraints.applyTo(parentLayout)
        // Initialize properties within the class
        this.view = inflatedView
        this.parentLayout = parentLayout
        // Default is not visible
        hide()
    }

    /**
     * Removes the loading HUD in the parent layout
     */
    fun removeFromParent() {
        if (view != null && parentLayout != null) {
            val layout = parentLayout
            layout!!.removeView(view!!)
        }
    }

    /**
     * Shows the loading HUD
     */
    fun show() {
        if (loadingHUD != null) {
            loadingHUD.isVisible = true
        }
    }

    /**
     * Hides the loading HUD
     */
    fun hide() {
        if (loadingHUD != null) {
            loadingHUD.isVisible = false
        }
    }

    private fun updateMessage() {
        if (loadingText != null) {
            loadingText.text = hintText
        }
    }


}