package com.jerryallanakshay.stocks

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONTokener


class StockSummary : AppCompatActivity() {

    private var stockSymbol: String? = null
    val ICONS = intArrayOf(
        R.drawable.chart_line,
        R.drawable.clock
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Stocks)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_summary)

        val stockTicker = findViewById<TextView>(R.id.stock_summary_ticker)
        val starBtn = findViewById<ImageView>(R.id.star_btn)
        val backBtn = findViewById<ImageView>(R.id.back_arrow_stock_summary)
        val pager = findViewById<ViewPager>(R.id.viewPager)
        val tab = findViewById<TabLayout>(R.id.tabs)

        val sharedPref = this.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)

        val adapter = ViewPagerAdapter(supportFragmentManager)

        setupViewPager(adapter, pager, tab)


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

    fun setupViewPager(adapter: ViewPagerAdapter, pager: ViewPager, tab: TabLayout) {
        // add fragment to the list
        adapter.addFragment(StockSummaryChart(), "")
        adapter.addFragment(StockHistoryChart(), "")
        // Adding the Adapter to the ViewPager
        pager.adapter = adapter
        // bind the viewPager with the TabLayout.
        tab.setupWithViewPager(pager)
        tab.getTabAt(0)!!.setIcon(ICONS[0]);
        tab.getTabAt(1)!!.setIcon(ICONS[1]);
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