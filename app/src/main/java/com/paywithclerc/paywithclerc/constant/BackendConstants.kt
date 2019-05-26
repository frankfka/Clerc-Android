package com.paywithclerc.paywithclerc.constant

object BackendConstants {

    const val BACKEND_URL = "https://paywithclerc.appspot.com"
    const val CREATE_CUST_URL = "$BACKEND_URL/customers/create"
    const val CREATE_EPH_KEY_URL = "$BACKEND_URL/customers/create-ephemeral-key"
    const val JWT_URL = "$BACKEND_URL/jwt/refresh"
    const val CHARGE_URL = "$BACKEND_URL/charge"
    const val EMAIL_RECEIPT_URL = "$BACKEND_URL/customers/email-receipt"

}