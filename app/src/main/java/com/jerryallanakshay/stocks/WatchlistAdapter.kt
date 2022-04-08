package com.jerryallanakshay.stocks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONTokener

class WatchlistAdapter(private val dataSet: ArrayList<FavoritesPortfolioDataModel>?, private val context: Context, private val sharedPref: SharedPreferences) : RecyclerView.Adapter<WatchlistAdapter.ViewHolder>() {

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
            val chevronSymbol: ImageView = view.findViewById(R.id.chevron_symbol)
            val banner: TextView = view.findViewById(R.id.banner)
            val portfolioSummary: LinearLayout = view.findViewById(R.id.portfolio_summary)
            val tickerData: LinearLayout = view.findViewById(R.id.ticker_data)
            val netWorth: TextView = view.findViewById(R.id.current_net_worth)
            val cashBalance: TextView = view.findViewById(R.id.current_cash_balance)
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
            viewHolder.portfolioSummary.visibility = View.GONE
            viewHolder.banner.visibility = View.GONE
            viewHolder.tickerData.visibility = View.GONE

            when (dataSet?.get(position)?.type) {
                1 -> {
                    viewHolder.banner.visibility = View.VISIBLE
                    viewHolder.banner.text = dataSet?.get(position)?.banner
                }
                2 -> {
                    viewHolder.portfolioSummary.visibility = View.VISIBLE
                    viewHolder.netWorth.text = dataSet?.get(position)?.netWorth
                    viewHolder.cashBalance.text = dataSet?.get(position)?.cashBalance
                }
                4 -> {
                    viewHolder.banner.visibility = View.VISIBLE
                    viewHolder.banner.text = dataSet?.get(position)?.banner
                }
                3, 5 -> {
                    viewHolder.tickerData.visibility = View.VISIBLE
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
                        setColors(viewHolder, R.color.black)
                    }

                    viewHolder.chevronSymbol.setOnClickListener {
                        val intent = Intent(viewHolder.chevronSymbol.context, StockSummary::class.java).apply {
                            putExtra(viewHolder.chevronSymbol.resources.getString(R.string.intent_stock_summary), dataSet?.get(position)?.ticker)
                        }
                        MainActivity.timer.cancel()
                        MainActivity.timer.purge()
                        viewHolder.chevronSymbol.context.startActivity(intent)
                    }
                }
                else -> {
                    //Do nothing
                }
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

        fun deleteItem(position: Int) {
            val prefData = sharedPref.getString(context.getString(R.string.watchlist), "[]")
            val jsonArray = JSONTokener(prefData).nextValue() as JSONArray
            val elementIndex = jsonArray.getIndexOfString(dataSet?.get(position)?.ticker)
            if(elementIndex!=-1) {
                jsonArray.remove(elementIndex)
            }
            with (sharedPref.edit()) {
                putString(context.getString(com.jerryallanakshay.stocks.R.string.watchlist), jsonArray.toString())
                apply()
            }
            dataSet?.removeAt(position)
            this.notifyDataSetChangedWithSort()
        }

        fun getContext(): Context {
            return context
        }

        private fun JSONArray.getIndexOfString(value: String?): Int {
            for(i in 0 until length()) {
                if(getString(i).equals(value)) {
                    return i
                }
            }
            return -1
        }

        fun notifyDataSetChangedWithSort() {
            dataSet?.sortBy { item -> item.ticker }
            dataSet?.sortBy { item -> item.type }
            notifyDataSetChanged()
        }

        fun getData(): ArrayList<FavoritesPortfolioDataModel>? {
            return dataSet
        }

        fun getItemBounds(type: Int?): Array<Int?> {
            when(type) {
                3 -> {
                    val firstIndex = dataSet?.indexOfFirst { it.type == 3 }
                    val secondIndex = dataSet?.indexOfLast { it.type == 3 }
                    return arrayOf(firstIndex, secondIndex)
                }
                5 -> {
                    val firstIndex = dataSet?.indexOfFirst { it.type == 5 }
                    val secondIndex = dataSet?.indexOfLast { it.type == 5 }
                    return arrayOf(firstIndex, secondIndex)
                }
                else -> {
                    /*Do nothing*/
                }
            }
            return arrayOf(-1,-1)
        }

    }
