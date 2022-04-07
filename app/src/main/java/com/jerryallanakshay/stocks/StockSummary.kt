package com.jerryallanakshay.stocks

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import org.w3c.dom.Text
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*


class StockSummary : AppCompatActivity() {

    private var stockSymbol: String? = null
    val ICONS = intArrayOf(
        R.drawable.chart_line,
        R.drawable.clock
    )
    private lateinit var queue: RequestQueue
    private lateinit var profileAndPriceData: JSONObject
    private lateinit var newsData: JSONArray
    private lateinit var recentHistory: JSONObject
    private var requests = 0
    private var completedRequests = 0
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var pager: ViewPager
    private lateinit var tab: TabLayout
    private lateinit var logo: ImageView
    private lateinit var companySymbol: TextView
    private lateinit var companyName: TextView
    private lateinit var price: TextView
    private lateinit var change: TextView
    private lateinit var changePercent: TextView
    private lateinit var trendingSymbol: ImageView
    private lateinit var dollarSymbol: TextView
    private lateinit var bracketSymbol: TextView
    private lateinit var bracketSymbolClose: TextView
    private var timeToSend = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Stocks)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stock_summary)

        val stockTicker = findViewById<TextView>(R.id.stock_summary_ticker)
        val starBtn = findViewById<ImageView>(R.id.star_btn)
        val backBtn = findViewById<ImageView>(R.id.back_arrow_stock_summary)
        pager = findViewById<ViewPager>(R.id.viewPager)
        tab = findViewById<TabLayout>(R.id.tabs)
        val pageLoader = findViewById<ProgressBar>(R.id.stock_summary_page_loader)
        val pageContent = findViewById<LinearLayout>(R.id.stock_summary_content)
        logo = findViewById(R.id.stock_summary_ticker_image)
        companyName = findViewById(R.id.stock_summary_ticker_name)
        companySymbol = findViewById(R.id.stock_summary_ticker_symbol)
        price = findViewById(R.id.stock_summary_price)
        change = findViewById(R.id.stock_change)
        changePercent = findViewById(R.id.stock_change_percent)
        trendingSymbol = findViewById(R.id.trending_symbol)
        dollarSymbol = findViewById(R.id.dollar_symbol)
        bracketSymbol = findViewById(R.id.bracket_symbol)
        bracketSymbolClose = findViewById(R.id.bracket_symbol_close)

        val sharedPref = this.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)
        queue = Volley.newRequestQueue(this)

        adapter = ViewPagerAdapter(supportFragmentManager)

        stockSymbol = intent.getStringExtra(resources.getString(R.string.intent_stock_summary))
        stockTicker.text = stockSymbol

        setStar(starBtn, sharedPref)

        backBtn.setOnClickListener {
            finish()
        }

        starBtn.setOnClickListener {
            toggleStar(starBtn, sharedPref)
        }

        fetchSummaryData(pageLoader, pageContent)

    }

    fun fetchRecentHistoryData(closeTime: Long, pageLoader: ProgressBar, pageContent: LinearLayout) {
        val calendar = Calendar.getInstance()
        val now = (calendar.timeInMillis)/1000
        timeToSend = closeTime
        if((now-closeTime) < (5*60)) {
            timeToSend = now
        }
        requests++
        var url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.six_hours_historical_data)}$stockSymbol/$timeToSend"
        var jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                recentHistory = response
                completedRequests++
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */})
        queue?.add(jsonObjectRequest)
    }

    fun fetchSummaryData(pageLoader: ProgressBar, pageContent: LinearLayout) {

        var url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.profile_and_quote_api)}$stockSymbol"
        requests++
        var jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                profileAndPriceData = response
                completedRequests++
                fetchRecentHistoryData(profileAndPriceData.getLong("t"), pageLoader, pageContent)
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /*Do nothing*/ })
        queue?.add(jsonObjectRequest)

        url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.news_api)}$stockSymbol"
        requests++
        var jsonArrayRequest = JsonArrayRequest (
            Request.Method.GET, url, null,
            { response ->
                newsData = response
                completedRequests++
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */ })
        queue?.add(jsonArrayRequest)

    }

    private fun roundToTwoDecimalPlaces(value: Double): BigDecimal? {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)
    }

    fun checkAndTogglePageVisibility(pageLoader: ProgressBar, pageContent: LinearLayout) {
        if(requests==completedRequests) {
            setupViewPager()
            displayData()
            pageLoader.visibility = View.GONE
            pageContent.visibility = View.VISIBLE
        }
    }

    fun displayData() {
        Glide.with(applicationContext)
            .load(profileAndPriceData.getString("logo"))
            .into(logo)
        price.text = roundToTwoDecimalPlaces(profileAndPriceData.getDouble("c")).toString()
        change.text = roundToTwoDecimalPlaces(profileAndPriceData.getDouble("d")).toString()
        changePercent.text = roundToTwoDecimalPlaces(profileAndPriceData.getDouble("dp")).toString()
        companyName.text = profileAndPriceData.getString("name")
        companySymbol.text = profileAndPriceData.getString("ticker")
        if(profileAndPriceData.getDouble("d") > 0) {
            trendingSymbol.visibility = View.VISIBLE
            trendingSymbol.setImageResource(R.drawable.trending_up)
            setColors(R.color.green_tint)
        } else if(profileAndPriceData.getDouble("d") < 0) {
            trendingSymbol.visibility = View.VISIBLE
            trendingSymbol.setImageResource(R.drawable.trending_down)
            setColors(R.color.red_tint)
        } else {
            trendingSymbol.visibility = View.GONE
            setColors(R.color.black)
        }
    }

    fun setColors(color: Int) {
        trendingSymbol.setColorFilter(resources.getColor(color))
        change.setTextColor(resources.getColor(color))
        changePercent.setTextColor(resources.getColor(color))
        dollarSymbol.setTextColor(resources.getColor(color))
        bracketSymbol.setTextColor(resources.getColor(color))
        bracketSymbolClose.setTextColor(resources.getColor(color))
    }

    fun setupViewPager() {
        // add fragment to the list
        adapter.addFragment(StockSummaryChart.newInstance(stockSymbol!!, recentHistory, profileAndPriceData.getDouble("d")), "")
        adapter.addFragment(StockHistoryChart.newInstance(stockSymbol!!, timeToSend), "")
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