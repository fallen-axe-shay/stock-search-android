package com.jerryallanakshay.stocks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class StockSummary : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Stocks)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_summary)

        val stockTicker = findViewById<TextView>(R.id.stock_summary_ticker)
        val backBtn = findViewById<ImageView>(R.id.back_arrow_stock_summary)

        stockTicker.text = intent.getStringExtra(resources.getString(R.string.intent_stock_summary))

        backBtn.setOnClickListener {
            finish()
        }

    }

    override fun onBackPressed() {
        finish()
    }
}