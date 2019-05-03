package com.paywithclerc.paywithclerc.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(val id: String, val name: String, val cost: Double, val currency: String) : Parcelable