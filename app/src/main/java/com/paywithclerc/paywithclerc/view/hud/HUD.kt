package com.paywithclerc.paywithclerc.view.hud

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R
import androidx.constraintlayout.widget.ConstraintSet
import kotlinx.android.synthetic.main.loading_hud.view.*
import kotlinx.android.synthetic.main.standard_hud.view.*

abstract class HUD: ConstraintLayout {

    private var text = ""
    private var view: View? = null
    private var parentLayout: ConstraintLayout? = null

    // Constructors to conform to ConstraintLayout
    constructor(context: Context, text:String) : this(context, null) {
        this.text = text
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    /**
     * Sets text displayed on the HUD
     */
    fun setText(text: String) {
        this.text = text
        updateText()
    }

    /**
     * Shows the HUD in the parent layout
     * milliseconds - the # of milliseconds to show for
     */
    open fun placeInParent(parentLayout: ConstraintLayout, milliseconds: Long = 0L) {
        val inflatedView = View.inflate(context, R.layout.standard_hud, this)
        // Update text & image
        updateText()
        updateImage()
        // Layout the view in the parent
        layout(parentLayout, inflatedView)
        // If the # of milliseconds is specified, remove the view after the given time
        if (milliseconds != 0L) {
            // TODO - fade out animation
            inflatedView.postDelayed({ removeFromParent() }, milliseconds)
        }
    }

    /**
     * Removes the HUD in the parent layout
     */
    fun removeFromParent() {
        if (view != null && parentLayout != null) {
            parentLayout!!.removeView(view!!)
        }
    }

    /**
     * Private functions to set text & image
     */
    protected fun updateText() {
        if (hudText != null) {
            hudText.text = text
        } else if (loadingText != null) {
            loadingText.text = text
        }
    }
    private fun updateImage() {
        if (hudImage != null) {
            hudImage.setImageDrawable(getHUDImage())
        }
    }

    /**
     * Layout the view in the parent layout
     */
    protected fun layout(parentLayout: ConstraintLayout, inflatedView: View) {
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
    }

    /**
     * Must be overridden by superclass
     */
    protected abstract fun getHUDImage(): Drawable?

}