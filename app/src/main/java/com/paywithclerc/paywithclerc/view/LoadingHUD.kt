package com.paywithclerc.paywithclerc.view

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R
import kotlinx.android.synthetic.main.loading_hud.view.*

class LoadingHUD(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.loading_hud, this)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.LoadingHUD)
        loadingText.text = attributes.getString(R.styleable.LoadingHUD_text)
        attributes.recycle()
    }
    
}