package com.jerryallanakshay.stocks

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

class StockHistoryChart : Fragment() {

    private var ticker: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stock_history_chart, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance(ticker: String) = StockHistoryChart().apply {
            arguments = Bundle().apply {
                putString("ticker", ticker)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("ticker")?.let {
            ticker = it
        }
    }

    fun fetchHistoricalData() {

    }
}