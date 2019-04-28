package com.paywithclerc.paywithclerc.service

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.constant.ViewConstants
import com.paywithclerc.paywithclerc.view.hud.ErrorHUD
import com.paywithclerc.paywithclerc.view.hud.LoadingHUD

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

}