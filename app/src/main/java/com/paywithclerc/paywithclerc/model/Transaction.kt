package com.paywithclerc.paywithclerc.model

import io.realm.RealmObject
import java.util.*

/**
 * Locally stored transaction class
 *
 * Can't be a data class because Kotlin makes data classes final
 */
open class Transaction(var txnId: String, var storeName: String, var txnDate: Date, var amount: Double, var currency: String): RealmObject() {

    // Empty constructor required for realm
    constructor(): this("", "", Date(), 0.0, "")

}