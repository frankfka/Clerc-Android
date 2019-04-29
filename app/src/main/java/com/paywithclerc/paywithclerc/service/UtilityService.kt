package com.paywithclerc.paywithclerc.service

import com.paywithclerc.paywithclerc.model.Product

object UtilityService {

    /**
     * Calculate total cost for items in cart
     */
    fun getTotalCost(items: List<Product>, quantities: List<Int>) : Double {
        var totalPrice = 0.0
        for (index in 0 until items.size) {
            totalPrice += items[index].cost * quantities[index]
        }
        return totalPrice
    }

}