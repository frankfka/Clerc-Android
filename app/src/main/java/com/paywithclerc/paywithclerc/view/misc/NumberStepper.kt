package com.paywithclerc.paywithclerc.view.misc

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.paywithclerc.paywithclerc.R
import kotlinx.android.synthetic.main.number_stepper.view.*

class NumberStepper: LinearLayout {

    var count = 0
        set(count) {
            field = count
            updateUI()
        }
    lateinit var onValueChange: (Int) -> Unit

    // Constructors to conform to LinearLayout
    constructor(context: Context, count: Int, onValueChange: (Int) -> (Unit)) : this(context, null) {
        this.count = count
        this.onValueChange = onValueChange
    }
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.number_stepper, this)
        numberStepperValueText.text = count.toString()
        numberStepperMinusButton.setOnClickListener {
            // Only decrement if it won't violate min val
            if (count - STEP >= MIN_VAL) {
                count -= STEP
            }
            updateUI()
            onValueChange(count)
        }
        numberStepperPlusButton.setOnClickListener {
            // No max val for now
            count += STEP
            updateUI()
            onValueChange(count)
        }
    }

    // Updates UI based on count
    private fun updateUI() {
        numberStepperValueText.text = count.toString()
        numberStepperMinusButton.isEnabled = count - STEP >= MIN_VAL
    }

    /**
     * Some default settings - this could be made customizable but there's no point for now
     */
    companion object {
        const val MIN_VAL = 0
        const val STEP = 1
    }

}