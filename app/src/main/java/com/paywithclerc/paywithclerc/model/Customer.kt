package com.paywithclerc.paywithclerc.model

data class Customer(val firebaseID: String, val stripeID: String, val name: String?, val email: String?) {

    companion object {
        var current: Customer? = null
    }

}