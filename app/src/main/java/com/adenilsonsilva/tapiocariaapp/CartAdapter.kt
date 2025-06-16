package com.adenilsonsilva.tapiocariaapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adenilsonsilva.tapiocariaapp.databinding.ListItemOrderCartBinding

data class CartItem(val id: String, val sabor: String, val preco: Double, var quantity: Int)

class CartAdapter(
    private val onIncrease: (CartItem) -> Unit,
    private val onDecrease: (CartItem) -> Unit
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ListItemOrderCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding, onIncrease, onDecrease)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CartViewHolder(
        private val binding: ListItemOrderCartBinding,
        private val onIncrease: (CartItem) -> Unit,
        private val onDecrease: (CartItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: CartItem) {
            binding.textViewCartItemName.text = cartItem.sabor
            binding.textViewQuantity.text = cartItem.quantity.toString()
            binding.buttonAddItem.setOnClickListener { onIncrease(cartItem) }
            binding.buttonRemoveItem.setOnClickListener { onDecrease(cartItem) }
        }
    }
}

class CartDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}