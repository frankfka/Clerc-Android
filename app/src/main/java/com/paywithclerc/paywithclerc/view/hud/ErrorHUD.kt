package com.paywithclerc.paywithclerc.view.hud

import android.content.Context
import android.graphics.drawable.Drawable
import com.paywithclerc.paywithclerc.R

class ErrorHUD(context: Context, text: String) : HUD(context, text) {

    override fun getHUDImage(): Drawable {
        return resources.getDrawable(R.drawable.ic_error_warning, null)
    }

}