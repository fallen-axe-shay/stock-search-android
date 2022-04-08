package com.jerryallanakshay.stocks

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.hichartsclasses.*
import com.highsoft.highcharts.core.HIChartView
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min


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
    private lateinit var peerData: JSONArray
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
    private lateinit var peerRecycler: RecyclerView
    private lateinit var peerRecyclerLayoutManager: LinearLayoutManager
    private lateinit var sharesOwned: TextView
    private lateinit var avgCost: TextView
    private lateinit var totalCost: TextView
    private lateinit var changeCost: TextView
    private lateinit var marketValue: TextView
    private lateinit var openPrice: TextView
    private lateinit var lowPrice: TextView
    private lateinit var highPrice: TextView
    private lateinit var closePrice: TextView
    private lateinit var ipoStart: TextView
    private lateinit var industry: TextView
    private lateinit var webpage: TextView
    private lateinit var tableCompany: TextView
    private lateinit var tableRedTot: TextView
    private lateinit var tableRedPos: TextView
    private lateinit var tableRedNeg: TextView
    private lateinit var tableTwiTot: TextView
    private lateinit var tableTwiNeg: TextView
    private lateinit var tableTwiPos: TextView
    private lateinit var tradeButton: Button
    private lateinit var recChart: WebView
    private lateinit var surpriseChart: WebView
    private lateinit var newsRecycler: RecyclerView
    private val newsList = ArrayList<NewsData>()
    private lateinit var newsAdapter: NewsAdapter
    private var peerList = ArrayList<String>()
    private val peerAdapter = CompanyPeerAdapter(peerList)
    private var timeToSend = 0L
    private var redditMentions = HashMap<String, Int>()
    private var twitterMentions = HashMap<String, Int>()


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
        peerRecycler = findViewById<RecyclerView>(R.id.peer_list)
        sharesOwned = findViewById(R.id.shares)
        avgCost = findViewById(R.id.avg_cost)
        totalCost = findViewById(R.id.total_cost)
        changeCost = findViewById(R.id.change)
        marketValue = findViewById(R.id.market_value)
        openPrice = findViewById(R.id.open_price)
        lowPrice = findViewById(R.id.low_price)
        highPrice = findViewById(R.id.high_price)
        closePrice = findViewById(R.id.prev_close)
        ipoStart = findViewById(R.id.ipo_start_date)
        industry = findViewById(R.id.industry)
        webpage = findViewById(R.id.webpage)
        tableCompany = findViewById(R.id.table_company_name)
        tableRedTot = findViewById(R.id.reddit_total_mentions)
        tableRedPos = findViewById(R.id.reddit_positive_mentions)
        tableRedNeg = findViewById(R.id.reddit_negative_mentions)
        tableTwiTot = findViewById(R.id.twitter_total_mentions)
        tableTwiNeg = findViewById(R.id.twitter_negative_mentions)
        tableTwiPos = findViewById(R.id.twitter_positive_mentions)
        recChart = findViewById(R.id.recommendation_trends)
        surpriseChart = findViewById(R.id.history_eps_surprises)
        newsRecycler = findViewById(R.id.news_list)
        tradeButton = findViewById(R.id.trade_button)

        newsAdapter = NewsAdapter(newsList, applicationContext)
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        newsRecycler.layoutManager = linearLayoutManager
        newsRecycler.adapter = newsAdapter

        peerRecyclerLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        peerRecycler.layoutManager = peerRecyclerLayoutManager
        peerRecycler.adapter = peerAdapter

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

        tradeButton.setOnClickListener{
            val dialog = Dialog(tradeButton.context)
            dialog.setContentView(R.layout.trade_dialog)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val title = dialog.findViewById<TextView>(R.id.title)
            val shares = dialog.findViewById<EditText>(R.id.number_of_shares)
            val calculation = dialog.findViewById<TextView>(R.id.current_details)
            val wallet = dialog.findViewById<TextView>(R.id.wallet_amount)
            val buyButton = dialog.findViewById<Button>(R.id.buy_button)
            val sellButton = dialog.findViewById<Button>(R.id.sell_button)
            title.text = "Trade ${profileAndPriceData.getString("name")} Shares"
            calculation.text = "0 * $${roundToTwoDecimalPlaces(profileAndPriceData.getDouble("c")).toString()}/Share = $0"
            wallet.text = "$${roundToTwoDecimalPlaces(sharedPref.getFloat(getString(R.string.cash_balance), 25000.00F).toDouble()).toString()} to buy ${profileAndPriceData.getString("ticker")}"

            shares.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) { }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var shareNo: Int?
                    try {
                        shareNo = s.toString().toInt()
                    } catch (ex: Exception) {
                        shareNo = 0
                    }
                    calculation.text = "${shareNo} * $${roundToTwoDecimalPlaces(profileAndPriceData.getDouble("c")).toString()}/Share = $${roundToTwoDecimalPlaces(shareNo!! * roundToTwoDecimalPlaces(profileAndPriceData.getDouble("c"))!!.toDouble()).toString()}"
                }

            })

            buyButton.setOnClickListener{
                val noText = shares.text.toString().trim()
                var no = 0
                if(!noText.equals("")) {
                    no = noText.toInt()
                }
                if(no == 0) {
                    dialog.dismiss()
                } else {
                    val totalCost = no * profileAndPriceData.getDouble("c")
                    val currentWallet =
                        sharedPref.getFloat(getString(R.string.cash_balance), 25000.00F)
                    if (totalCost > currentWallet) {
                        Toast.makeText(
                            applicationContext,
                            "Not enough money to buy",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        buyShares(
                            totalCost,
                            currentWallet.toDouble(),
                            profileAndPriceData.getString("ticker"),
                            no,
                            profileAndPriceData.getDouble("c")
                        )
                        dialog.dismiss()
                    }
                }
            }

            sellButton.setOnClickListener{
                val noText = shares.text.toString().trim()
                var no = 0
                if(!noText.equals("")) {
                    no = noText.toInt()
                }
                if(no == 0) {
                    dialog.dismiss()
                } else {

                    val shareData = sharedPref.getString(getString(R.string.shares_owned), "{}")
                    val jsonObject = JSONTokener(shareData).nextValue() as JSONObject
                    if (!jsonObject.has(profileAndPriceData.getString("ticker"))) {
                        Toast.makeText(
                            applicationContext,
                            "Not enough shares to sell",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val tickerShares =
                            jsonObject.getJSONArray(profileAndPriceData.getString("ticker"))
                        if (no > tickerShares.length()) {
                            Toast.makeText(
                                applicationContext,
                                "Not enough shares to sell",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            sellShares(
                                no,
                                profileAndPriceData.getDouble("c"),
                                profileAndPriceData.getString("ticker")
                            )
                            dialog.dismiss()
                        }
                    }
                }
            }

            dialog.show()
        }

        fetchSummaryData(pageLoader, pageContent)

    }

    fun sellShares(number: Int, cost: Double, symbol: String) {
        showTradeSuccessDialog("You have successfully sold $number shares of $symbol")
    }

    fun buyShares(totalCost: Double, currentWallet: Double, symbol: String, number: Int, price: Double) {
        showTradeSuccessDialog("You have successfully bought $number shares of $symbol")
    }

    fun showTradeSuccessDialog(message: String) {
        val dialog = Dialog(tradeButton.context)
        dialog.setContentView(R.layout.trade_complete_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = dialog.findViewById<TextView>(R.id.success_text)
        val btn = dialog.findViewById<Button>(R.id.done_button)

        title.text = message

        btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

        url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.company_peer_api)}$stockSymbol"
        requests++
        jsonArrayRequest = JsonArrayRequest (
            Request.Method.GET, url, null,
            { response ->
                peerData = response
                completedRequests++
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */ })
        queue?.add(jsonArrayRequest)

        url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.social_sentiment_api)}$stockSymbol"
        requests++
        jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                aggregateSocialSentiments(response)
                completedRequests++
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */ })
        queue?.add(jsonObjectRequest)

    }

    private fun aggregateSocialSentiments(data: JSONObject) {
        val redditArray = data.getJSONArray("reddit")
        val twitterArray = data.getJSONArray("twitter")
        var redPos = 0
        var redNeg = 0
        var redTot = 0
        var twiTot = 0
        var twiPos = 0
        var twiNeg = 0
        for(i in 0 until redditArray.length()) {
            redTot += (redditArray.getJSONObject(i).getInt("mention"))
            redPos += (redditArray.getJSONObject(i).getInt("positiveMention"))
            redNeg += (redditArray.getJSONObject(i).getInt("negativeMention"))
        }
        for(i in 0 until twitterArray.length()) {
            twiTot += (twitterArray.getJSONObject(i).getInt("mention"))
            twiPos += (twitterArray.getJSONObject(i).getInt("positiveMention"))
            twiNeg += (twitterArray.getJSONObject(i).getInt("negativeMention"))
        }
        twitterMentions["total"] = twiTot
        twitterMentions["positive"] = twiPos
        twitterMentions["negative"] = twiNeg
        redditMentions["total"] = redTot
        redditMentions["positive"] = redPos
        redditMentions["negative"] = redNeg
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
        tableCompany.text = profileAndPriceData.getString("name")
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
        populatePeerList()
        openPrice.text = "$" + roundToTwoDecimalPlaces(profileAndPriceData.getString("o").toDouble()).toString()
        lowPrice.text = "$" + roundToTwoDecimalPlaces(profileAndPriceData.getString("l").toDouble()).toString()
        highPrice.text = "$" + roundToTwoDecimalPlaces(profileAndPriceData.getString("h").toDouble()).toString()
        closePrice.text = "$" + roundToTwoDecimalPlaces(profileAndPriceData.getString("c").toDouble()).toString()
        var dateSplit = profileAndPriceData.getString("ipo").split("-")
        ipoStart.text = "${dateSplit[2]}-${dateSplit[1]}-${dateSplit[0]}"
        industry.text = profileAndPriceData.getString("finnhubIndustry")
        webpage.text = profileAndPriceData.getString("weburl")
        companyName.text = profileAndPriceData.getString("name")
        tableRedTot.text = redditMentions["total"].toString()
        tableRedPos.text = redditMentions["positive"].toString()
        tableRedNeg.text = redditMentions["negative"].toString()
        tableTwiTot.text = twitterMentions["total"].toString()
        tableTwiPos.text = twitterMentions["positive"].toString()
        tableTwiNeg.text = twitterMentions["negative"].toString()
        setRecChartOptions()
        setSurpriseChartOptions()
        showNewsData()
    }

    fun showNewsData() {
        newsList.clear()
        var leeWay = 0
        for(i in 0 until min(newsData.length(), 20 + leeWay)) {
            val item = newsData.getJSONObject(i)
            if(item.getString("image") == "" || item.getString("datetime") == "" || item.getString("source") == "" || item.getString("summary") == "" || item.getString("url") == "" || item.getString("headline") == "") {
                leeWay++
                continue
            }
            newsList.add(NewsData(item.getString("headline"), item.getLong("datetime"), item.getString("source"), item.getString("image"), item.getString("url"), item.getString("summary")))
        }
        newsList[0].type = 0
        newsAdapter.notifyDataSetChangedWithSort()
    }

    fun setRecChartOptions() {
        recChart.settings.javaScriptEnabled = true
        val url = "${applicationContext?.getString(R.string.server_url)}${applicationContext?.getString(R.string.rec_trends_api)}${stockSymbol}"
        recChart.loadUrl(url)
    }

    fun setSurpriseChartOptions() {
        surpriseChart.settings.javaScriptEnabled = true
        val url = "${applicationContext?.getString(R.string.server_url)}${applicationContext?.getString(R.string.comp_earnings_api)}${stockSymbol}"
        surpriseChart.loadUrl(url)
    }

    fun populatePeerList() {
        peerList.clear()
        for(i in 0 until peerData.length()) {
            if(peerData.getString(i).contains(".")) continue
            peerList.add(peerData.getString(i))
        }
        peerAdapter.notifyDataSetChanged()
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