package com.jerryallanakshay.stocks

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WatchlistAdapter(private val dataSet: ArrayList<FavoritesPortfolioDataModel>?) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ticker: TextView = view.findViewById(R.id.favorites_ticker)
            val name: TextView = view.findViewById(R.id.favorites_name)
            val stockPrice: TextView = view.findViewById(R.id.favorites_stock_price)
            val change: TextView = view.findViewById(R.id.stock_change)
            val changePercent: TextView = view.findViewById(R.id.stock_change_percent)
            val dollarSymbol: TextView = view.findViewById(R.id.dollar_symbol)
            val bracketSymbol: TextView = view.findViewById(R.id.bracket_symbol)
            val bracketSymbolClose: TextView = view.findViewById(R.id.bracket_symbol_close)
            val trendingSymbol: ImageView = view.findViewById(R.id.trending_symbol)
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.watchlist_item, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.ticker.text = dataSet?.get(position)?.ticker
            viewHolder.name.text = dataSet?.get(position)?.stockName
            viewHolder.stockPrice.text = dataSet?.get(position)?.stockPrice
            viewHolder.change.text = dataSet?.get(position)?.stockChange
            viewHolder.changePercent.text = dataSet?.get(position)?.stockChangePercent

            if(dataSet?.get(position)?.stockChange?.toDouble()!! > 0) {
                viewHolder.trendingSymbol.visibility = View.VISIBLE
                viewHolder.trendingSymbol.setImageResource(R.drawable.trending_up)
                setColors(viewHolder, R.color.green_tint)
            } else if(dataSet?.get(position)?.stockChange?.toDouble()!! < 0) {
                viewHolder.trendingSymbol.visibility = View.VISIBLE
                viewHolder.trendingSymbol.setImageResource(R.drawable.trending_down)
                setColors(viewHolder, R.color.red_tint)
            } else {
                viewHolder.trendingSymbol.visibility = View.GONE
                setColors(viewHolder, R.color.red_tint)
            }
        }

    fun setColors(viewHolder: ViewHolder, color: Int) {
        viewHolder.trendingSymbol.setColorFilter(viewHolder.trendingSymbol.resources.getColor(color))
        viewHolder.change.setTextColor(viewHolder.change.resources.getColor(color))
        viewHolder.changePercent.setTextColor(viewHolder.changePercent.resources.getColor(color))
        viewHolder.dollarSymbol.setTextColor(viewHolder.dollarSymbol.resources.getColor(color))
        viewHolder.bracketSymbol.setTextColor(viewHolder.bracketSymbol.resources.getColor(color))
        viewHolder.bracketSymbolClose.setTextColor(viewHolder.bracketSymbolClose.resources.getColor(color))
    }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = (dataSet?.size)?:0



    }
