package com.paywithclerc.paywithclerc.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Product(val id: String, val name: String, val cost: Double, val currency: String, val priceUnit: PriceUnit) : Parcelable

@Parcelize
enum class PriceUnit(val displayString: String, val perUnitString: String): Parcelable {
    UNIT("unit", "ea."),
    KG("kg", "/kg"),
    LB("lb", "/lb")
}