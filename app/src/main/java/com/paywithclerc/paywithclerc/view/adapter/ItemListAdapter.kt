package com.paywithclerc.paywithclerc.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.paywithclerc.paywithclerc.R
import com.paywithclerc.paywithclerc.model.Product
import com.paywithclerc.paywithclerc.service.ViewService

/**
 * Our Adapter is used by the RecyclerView and is a subclass of the RecyclerView Adapter
 */
class ItemListAdapter(private val items: List<Product>, private val quantities: List<Double>, private val onItemClick: (Product) -> (Unit))
    : RecyclerView.Adapter<ItemListAdapter.ItemListViewHolder>() {

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
            .inflate(R.layout.shopping_list_item, parent, false)
        // Wrap the view in our view holder
        return ItemListViewHolder(itemView)
    }

    /**
     * Fetches the appropriate item data and initializes the view
     */
    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        // Get the references to the views within each item
        val rootView = holder.itemView
        val itemNameLabel = rootView.findViewById<TextView>(R.id.shoppingListItemNameLabel)
        val quantityLabel = rootView.findViewById<TextView>(R.id.shoppingListItemQuantityLabel)
        val costLabel = rootView.findViewById<TextView>(R.id.shoppingListItemCostLabel)
        // Initialize view
        val product = items[position]
        val quantity = quantities[position]
        itemNameLabel.text = product.name
        quantityLabel.text = "${ViewService.getFormattedCost(product.cost)} x $quantity"
        costLabel.text = ViewService.getFormattedCost(product.cost * quantity)
        // Set listener for on-click
        rootView.setOnClickListener {
            onItemClick(product)
        }
    }

    // Returns size of the dataset for recyclerview
    override fun getItemCount() = items.size

}
