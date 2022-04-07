package com.jerryallanakshay.stocks

import android.content.Intent
import android.text.Html
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.Adapter

import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity

import androidx.recyclerview.widget.RecyclerView


class CompanyPeerAdapter(private val list: List<String>) : RecyclerView.Adapter<CompanyPeerAdapter.MyView>() {

    inner class MyView(view: View) : RecyclerView.ViewHolder(view) {
        // Text View
        var textView: TextView = view.findViewById(R.id.peer_name)
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): MyView {
        val itemView: View = LayoutInflater.from(parent.context).inflate(R.layout.company_peer_item, parent, false)
        return MyView(itemView)
    }

    override fun onBindViewHolder(
        holder: MyView,
        position: Int
    ) {
        holder.textView.text = Html.fromHtml("<a href='#'>${list[position]},</a>")

        holder.textView.setOnClickListener {
            val intent = Intent(holder.textView.context, StockSummary::class.java).apply {
                putExtra(holder.textView.resources.getString(R.string.intent_stock_summary), list[position])
            }
            holder.textView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}