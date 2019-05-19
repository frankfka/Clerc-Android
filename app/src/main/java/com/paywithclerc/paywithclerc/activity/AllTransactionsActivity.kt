package com.paywithclerc.paywithclerc.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.model.Transaction
import com.paywithclerc.paywithclerc.service.ViewService
import com.paywithclerc.paywithclerc.view.adapter.TransactionsListAdapter
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_all_transactions.*

class AllTransactionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_transactions)

        // Initialize the view
        val pastTransactions = Realm.getDefaultInstance().where(Transaction::class.java)
            .sort("txnDate", Sort.DESCENDING)
            .findAll()

        val txnsListAdapter = TransactionsListAdapter(pastTransactions) { txn ->
            ViewService.showTxnDetailDialog(this, txn) { dialog, success ->
                dialog.dismiss()
                if (success) {
                    ViewService.showInfoDialog(this, "Email Receipt",
                        "An email has been sent. If you do not see it in your inbox, please check your spam folder.")
                } else {
                    ViewService.showErrorHUD(this, allTxnsMainConstraintLayout)
                }
            }
        }
        allTxnsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AllTransactionsActivity)
            adapter = txnsListAdapter
        }

    }
}
