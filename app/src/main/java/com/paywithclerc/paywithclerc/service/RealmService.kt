package com.paywithclerc.paywithclerc.service

import android.content.Context
import com.paywithclerc.paywithclerc.model.Transaction
import io.realm.Realm
import java.util.*

object RealmService {

    /**
     * Initialize the default realm - should be done at application load
     */
    fun initializeRealm(context: Context) {
        Realm.init(context)
        // Do any setup here
    }

    /**
     * Adds a transaction to realm - assumes that the transaction date is the current instant
     */
    fun addTransaction(txnId: String, storeName: String, amount: Double, currency: String) {

        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()

        val transaction: Transaction = realm.createObject(Transaction::class.java)
        transaction.txnId = txnId
        transaction.amount = amount
        transaction.currency = currency
        transaction.storeName = storeName
        transaction.txnDate = Date()

        realm.commitTransaction()

    }

}