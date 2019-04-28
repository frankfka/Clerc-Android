package com.paywithclerc.paywithclerc.service

import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ViewConstants
import com.paywithclerc.paywithclerc.view.hud.ErrorHUD

object ViewService {

    fun showErrorHUD(context: Context, parentConstraintLayout: ConstraintLayout, message: String? = null) {
        val errorHint = message ?: context.resources.getString(R.string.default_error)
        val errorHUD = ErrorHUD(context, errorHint)
        errorHUD.placeInParent(parentConstraintLayout, ViewConstants.HUD_SHOW_TIME)
    }

}