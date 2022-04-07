package com.jerryallanakshay.stocks

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast

class StockHistoryChart : Fragment() {

    private var ticker = ""
    private var time = 0L
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_stock_history_chart, container, false)
        webView = root.findViewById<WebView>(R.id.chart_web_view)
        webView.settings.javaScriptEnabled = true
        val url = "${context?.getString(R.string.server_url)}${context?.getString(R.string.two_years_historical_data)}${ticker}/${time}"
        webView.loadUrl(url)
        return root
    }

    companion object {

        @JvmStatic
        fun newInstance(ticker: String, time: Long) = StockHistoryChart().apply {
            arguments = Bundle().apply {
                putString("ticker", ticker)
                putLong("time", time)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("ticker")?.let {
            ticker = it
        }
        arguments?.getLong("time")?.let {
            time = it
        }
    }
}