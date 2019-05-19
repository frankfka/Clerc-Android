package com.paywithclerc.paywithclerc.service

import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Store

object UtilityService {

    /**
     * Calculate total cost for items in cart
     */
    fun getTotalCost(items: List<Product>, quantities: List<Double>): Double {
        var totalPrice = 0.0
        for (index in 0 until items.size) {
            totalPrice += items[index].cost * quantities[index]
        }
        return totalPrice
    }

    /**
     * Calculates taxes
     */
    fun getTaxes(subtotal: Double, store: Store): Double {
        return subtotal * store.taxRate
    }

}