package com.adenilsonsilva.tapiocariaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adenilsonsilva.tapiocariaapp.model.Venda
import java.text.SimpleDateFormat
import java.util.*

class VendasAdapter(private val onItemClicked: (Venda) -> Unit) :
    ListAdapter<Venda, VendasAdapter.VendaViewHolder>(VendasComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VendaViewHolder {
        return VendaViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: VendaViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class VendaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val saborTextView: TextView = itemView.findViewById(R.id.saborTapiocaTextView)
        private val dataTextView: TextView = itemView.findViewById(R.id.dataVendaTextView)
        private val precoTextView: TextView = itemView.findViewById(R.id.precoVendaTextView)
        private val pagamentoTextView: TextView = itemView.findViewById(R.id.pagamentoVendaTextView)
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        fun bind(venda: Venda) {
            saborTextView.text = venda.sabor
            precoTextView.text = String.format(Locale.getDefault(), "R$ %.2f", venda.preco)
            dataTextView.text = dateFormat.format(Date(venda.data))
            pagamentoTextView.text = venda.formaDePagamento
        }

        companion object {
            fun create(parent: ViewGroup): VendaViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_venda, parent, false)
                return VendaViewHolder(view)
            }
        }
    }

    class VendasComparator : DiffUtil.ItemCallback<Venda>() {
        override fun areItemsTheSame(oldItem: Venda, newItem: Venda): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Venda, newItem: Venda): Boolean {
            return oldItem == newItem
        }
    }
}


