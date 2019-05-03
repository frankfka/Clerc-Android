package com.paywithclerc.paywithclerc.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.model.Transaction
import com.paywithclerc.paywithclerc.service.ViewService

/**
 * Our Adapter is used by the RecyclerView and is a subclass of the RecyclerView Adapter
 */
class TransactionsListAdapter(private val transactions: List<Transaction>, private val onItemClick: (Transaction) -> (Unit))
    : RecyclerView.Adapter<TransactionsListAdapter.ItemListViewHolder>() {

    /**
     * Subclass ViewHolder - our base view for each list item is just a simple view
     */
    class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /**
     * Inflates the view that we want and wraps it in our ViewHolder class
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        // Inflate the item view
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.transactions_list_item, parent, false)
        // Wrap the view in our view holder
        return ItemListViewHolder(itemView)
    }

    /**
     * Fetches the appropriate item data and initializes the view
     */
    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        // Get the references to the views within each item
        val rootView = holder.itemView
        val storeNameLabel = rootView.findViewById<TextView>(R.id.txnListStoreNameLabel)
        val dateLabel = rootView.findViewById<TextView>(R.id.txnListDateLabel)
        val costLabel = rootView.findViewById<TextView>(R.id.txnListCostLabel)
        // Initialize view
        val txn = transactions[position]
        storeNameLabel.text = txn.storeName
        dateLabel.text = ViewService.getFormattedDate(txn.txnDate)
        costLabel.text = ViewService.getFormattedCost(txn.amount)
        // Set listener for on-click
        rootView.setOnClickListener {
            onItemClick(txn)
        }
    }

    // Returns size of the dataset for recyclerview
    override fun getItemCount() = transactions.size

}
