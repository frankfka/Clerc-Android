package com.paywithclerc.paywithclerc.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Store(val id: String, val name: String, val taxRate: Double = 0.0, val successMessage: String? = null) : Parcelable