package com.jerryallanakshay.stocks

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONTokener

class StockSummary : AppCompatActivity() {

    private var stockSymbol: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Stocks)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_summary)

        val stockTicker = findViewById<TextView>(R.id.stock_summary_ticker)
        val starBtn = findViewById<ImageView>(R.id.star_btn)
        val backBtn = findViewById<ImageView>(R.id.back_arrow_stock_summary)

        val sharedPref = this.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)

        stockSymbol = intent.getStringExtra(resources.getString(R.string.intent_stock_summary))
        stockTicker.text = stockSymbol

        setStar(starBtn, sharedPref)

        backBtn.setOnClickListener {
            finish()
        }

        starBtn.setOnClickListener {
            toggleStar(starBtn, sharedPref)
        }

    }

    fun toggleStar(starBtn: ImageView, sharedPref: SharedPreferences) {
        val prefData = sharedPref.getString(getString(R.string.watchlist), "[]")
        val jsonArray = JSONTokener(prefData).nextValue() as JSONArray
        val elementIndex = jsonArray.getIndexOfString(stockSymbol)
        var isStarred = false
        if(elementIndex!=-1) {
            jsonArray.remove(elementIndex)
            Toast.makeText(applicationContext, "$stockSymbol is removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            jsonArray.put(stockSymbol)
            isStarred = true
            Toast.makeText(applicationContext, "$stockSymbol is added to favorites", Toast.LENGTH_SHORT).show()
        }
        with (sharedPref.edit()) {
            putString(getString(R.string.watchlist), jsonArray.toString())
            apply()
        }
        starBtn.setImageResource(if(isStarred) R.drawable.star else R.drawable.star_outline)
    }

    private fun JSONArray.getIndexOfString(value: String?): Int {
        for(i in 0 until length()) {
            if(getString(i).equals(value)) {
                return i
            }
        }
        return -1
    }

    fun setStar(starBtn: ImageView, sharedPref: SharedPreferences) {
        val prefData = sharedPref.getString(getString(R.string.watchlist), "[]")
        val jsonArray = JSONTokener(prefData).nextValue() as JSONArray
        var isStarred = (jsonArray.getIndexOfString(stockSymbol)!=-1)
        starBtn.setImageResource(if(isStarred) R.drawable.star else R.drawable.star_outline)
    }

    override fun onBackPressed() {
        finish()
    }
}