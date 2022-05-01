package com.jerryallanakshay.stocks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    companion object {
        var timer = Timer()
    }

    private var queue: RequestQueue? = null
    private var autocompleteData: MutableList<String> = mutableListOf<String>()
    private var autoCompleteAdapter: ArrayAdapter<String>? = null
    private var watchlistArrayList: ArrayList<FavoritesPortfolioDataModel>? = ArrayList()
    private var watchlistAdapter: WatchlistAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null
    private var requestCounter: Int = 0
    private var completedRequests: Int = 0
    private var shouldExecuteOnResume: Boolean = true
    private var netWorth = 0.0
    private var updateRequestCounter: Int = 0
    private var updateRequestCompleted: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        shouldExecuteOnResume = false
        setTheme(R.style.Theme_Stocks_NoActionBar)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val activity = this

        val searchBtn = findViewById<ImageView>(R.id.search_btn)
        val backSearchBtn = findViewById<ImageView>(R.id.back_arrow_search_bar)
        val closeSearchBtn = findViewById<ImageView>(R.id.close_search_bar)
        val toolbarLyt = findViewById<RelativeLayout>(R.id.default_toolbar_layout)
        val searchToolbarLyt = findViewById<RelativeLayout>(R.id.search_toolbar_layout)
        val searchTicker = findViewById<AutoCompleteTextView>(R.id.search_ticker)
        val finnhubLinkText = findViewById<TextView>(R.id.finnhub_link)
        val todayText = findViewById<TextView>(R.id.date_today)
        val watchlistList = findViewById<RecyclerView>(R.id.favorites_list)
        val pageLoader = findViewById<ProgressBar>(R.id.page_loader)
        val pageContent = findViewById<RelativeLayout>(R.id.page_content)

        val sharedPref = activity.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)

        addBannersToArrayList()

        searchTicker.autocapitalize()
        setAdapterAndItemClickListener(searchTicker)

        initializeRequestQueue()
        setAutoCompleteAPICalls(searchTicker)

        setWatchlistRecyclerView(watchlistList, sharedPref, pageLoader, pageContent)

        setCurrentDate(todayText)
        getSetCashBalance(sharedPref)

        searchBtn.setOnClickListener {
            showSearchBar(searchToolbarLyt, toolbarLyt, searchTicker)
        }

        backSearchBtn.setOnClickListener {
            showNormalAppBar(searchToolbarLyt, toolbarLyt, searchTicker)
        }

        closeSearchBtn.setOnClickListener {
            searchTicker.setText("")
            searchTicker.dismissDropDown()
        }

        finnhubLinkText.setOnClickListener {
            openLinkOnBrowser("https://finnhub.io/")
        }

        checkAndTogglePageVisibility(pageLoader, pageContent)

    }

    fun setUpdateInterval() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateCashBalance()
                updatePortfolioData()
                updateWatchlistData()
            }
        }, 0, 15000)
    }

    fun updateCashBalance() {
        val index = watchlistArrayList?.indexOfFirst { it.type == 2 }
        watchlistArrayList!!.get(index!!).netWorth = watchlistArrayList!!.get(index!!).cashBalance
    }

    fun makePortfolioUpdateRequest(sharedPref: SharedPreferences, portfolioQueue: RequestQueue, ticker: String) {
        val url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.profile_and_quote_api)}$ticker"
        val jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                val prefData = sharedPref.getString(getString(R.string.shares_owned), "{}")
                val shareObject = JSONTokener(prefData).nextValue() as JSONObject
                var currentArray = shareObject.getJSONArray(response.getString("ticker"))
                var totalCost = 0.0
                for(i in 0 until currentArray.length()) {
                    totalCost += currentArray.getJSONObject(i).getDouble("price")
                }
                var average = 0.0
                if(currentArray.length()!=0) {
                    average = roundToTwoDecimalPlaces(totalCost/currentArray.length()).toString().toDouble()
                }
                var changeData = (response.getDouble("c") - average) * currentArray.length()
                var changePercent = (changeData/totalCost) * 100
                val index = watchlistArrayList?.indexOfFirst { it.ticker == response.getString("ticker") }
                watchlistArrayList?.get(index!!)?.stockPrice = roundToTwoDecimalPlaces(currentArray.length() * response.getDouble("c")).toString()
                watchlistArrayList?.get(index!!)?.stockChange = roundToTwoDecimalPlaces(changeData).toString()
                watchlistArrayList?.get(index!!)?.stockChangePercent = roundToTwoDecimalPlaces(changePercent).toString()
                //watchlistAdapter?.notifyItemChanged(index!!)
                val indexWallet = watchlistArrayList?.indexOfFirst { it.type == 2 }
                var netWorth = watchlistArrayList!!.get(indexWallet!!).netWorth.toDouble()
                netWorth += (currentArray.length()*response.getDouble("c"))
                watchlistArrayList!!.get(indexWallet!!).netWorth = roundToTwoDecimalPlaces(netWorth).toString()
                //watchlistAdapter?.notifyItemChanged(indexWallet!!)
                updateRequestCompleted++
                checkIfAllDataUpdated()
            },
            { /* Do nothing */ })
        portfolioQueue?.add(jsonObjectRequest)
    }

    fun checkIfAllDataUpdated() {
        if(updateRequestCounter==updateRequestCompleted) {
            //watchlistAdapter?.notifyDataSetChanged()
        }
    }

    fun updatePortfolioData() {
        val sharedPref = this.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)
        val portfolioQueue = Volley.newRequestQueue(applicationContext)
        val shareObject = JSONTokener(sharedPref.getString(getString(R.string.shares_owned), "{}")).nextValue() as JSONObject
        val keys: Iterator<*> = shareObject.keys()
        while (keys.hasNext()) {
            updateRequestCounter++
            val currentKey = keys.next() as String
            makePortfolioUpdateRequest(sharedPref, portfolioQueue, currentKey)
        }
    }

    fun updateWatchlistData() {
        val sharedPref = this.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)
        val watchlistQueue = Volley.newRequestQueue(applicationContext)
        val prefData = sharedPref.getString(getString(R.string.watchlist), "[]")
        val jsonArray = JSONTokener(prefData).nextValue() as JSONArray
        for(i in 0 until jsonArray.length()) {
            updateRequestCounter++
            makeStockUpdateRequest(jsonArray.getString(i), watchlistQueue)
        }
    }

    fun makeStockUpdateRequest(ticker: String, watchlistQueue: RequestQueue) {
        val url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.profile_and_quote_api)}$ticker"
        val jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                val index = watchlistArrayList?.indexOfLast { it.ticker == response.getString("ticker") }
                watchlistArrayList?.get(index!!)?.stockPrice = roundToTwoDecimalPlaces(response.getDouble("c")).toString()
                watchlistArrayList?.get(index!!)?.stockChange = roundToTwoDecimalPlaces(response.getDouble("d")).toString()
                watchlistArrayList?.get(index!!)?.stockChangePercent = roundToTwoDecimalPlaces(response.getDouble("dp")).toString()
                //watchlistAdapter?.notifyItemChanged(index!!)
                updateRequestCompleted++
                checkIfAllDataUpdated()
            },
            { /* Do nothing */ })
        watchlistQueue?.add(jsonObjectRequest)
    }


    private fun setWatchlistRecyclerView(watchlistList: RecyclerView, sharedPref: SharedPreferences, pageLoader: ProgressBar, pageContent: RelativeLayout) {
        val linearLayoutManager = LinearLayoutManager(applicationContext)
        watchlistList.layoutManager = linearLayoutManager
        watchlistAdapter = WatchlistAdapter(watchlistArrayList, applicationContext, sharedPref)
        watchlistList.adapter = watchlistAdapter
        watchlistList.addItemDecoration(DividerItemDecoration(watchlistList.context, DividerItemDecoration.VERTICAL))
        itemTouchHelper = ItemTouchHelper(ItemTouchHelperCallback(watchlistAdapter!!))
        itemTouchHelper!!.attachToRecyclerView(watchlistList)
        fetchFavoritesData(sharedPref, pageLoader, pageContent)
        fetchPortfolioData(sharedPref, pageLoader, pageContent)
    }

    fun fetchPortfolioData(sharedPref: SharedPreferences, pageLoader: ProgressBar, pageContent: RelativeLayout) {
        watchlistArrayList?.removeIf { data -> data.type == 3 }
        val prefData = sharedPref.getString(getString(R.string.shares_owned), "{}")
        val shareObject = JSONTokener(prefData).nextValue() as JSONObject
        val keys: Iterator<*> = shareObject.keys()
        while (keys.hasNext()) {
            val currentKey = keys.next() as String
            requestCounter++
            makePortfolioRequest(currentKey, pageLoader, pageContent, sharedPref)
        }
    }

    fun makePortfolioRequest(ticker: String, pageLoader: ProgressBar, pageContent: RelativeLayout, sharedPref: SharedPreferences) {
        val url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.profile_and_quote_api)}$ticker"
        val jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
                val prefData = sharedPref.getString(getString(R.string.shares_owned), "{}")
                val shareObject = JSONTokener(prefData).nextValue() as JSONObject
                var currentArray = shareObject.getJSONArray(response.getString("ticker"))
                var totalCost = 0.0
                for(i in 0 until currentArray.length()) {
                    totalCost += currentArray.getJSONObject(i).getDouble("price")
                }
                var average = 0.0
                if(currentArray.length()!=0) {
                    average = roundToTwoDecimalPlaces(totalCost/currentArray.length()).toString().toDouble()
                }
                var changeData = (response.getDouble("c") - average) * currentArray.length()
                var changePercent = (changeData/totalCost) * 100
                watchlistArrayList?.add(FavoritesPortfolioDataModel(response.getString("ticker"), "${currentArray.length()} Shares", (currentArray.length() * response.getDouble("c")), changeData, changePercent, 3, ""))
                watchlistAdapter?.notifyDataSetChangedWithSort()
                completedRequests++
                getSetCashBalance(sharedPref, true, currentArray.length()*response.getDouble("c"))
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */ })
        queue?.add(jsonObjectRequest)
    }

    fun fetchFavoritesData(sharedPref: SharedPreferences, pageLoader: ProgressBar, pageContent: RelativeLayout) {
        watchlistArrayList?.removeIf { data -> data.type == 5 }
        val prefData = sharedPref.getString(getString(R.string.watchlist), "[]")
        val jsonArray = JSONTokener(prefData).nextValue() as JSONArray
        for(i in 0 until jsonArray.length()) {
            requestCounter++
            makeStockRequest(jsonArray.getString(i), pageLoader, pageContent)
        }
    }

    fun addBannersToArrayList() {
        watchlistArrayList?.add(FavoritesPortfolioDataModel(type = 1, banner = "portfolio"))
        watchlistArrayList?.add(FavoritesPortfolioDataModel(type = 4, banner = "favorites"))
    }

    fun makeStockRequest(ticker: String, pageLoader: ProgressBar, pageContent: RelativeLayout) {
        val url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.profile_and_quote_api)}$ticker"
        val jsonObjectRequest = JsonObjectRequest (
            Request.Method.GET, url, null,
            { response ->
               watchlistArrayList?.add(FavoritesPortfolioDataModel(response.getString("ticker"), response.getString("name"), response.getString("c").toDouble(), response.getString("d").toDouble(), response.getString("dp").toDouble(), 5, ""))
                watchlistAdapter?.notifyDataSetChangedWithSort()
                completedRequests++
                checkAndTogglePageVisibility(pageLoader, pageContent)
            },
            { /* Do nothing */ })
        queue?.add(jsonObjectRequest)
    }

    fun checkAndTogglePageVisibility(pageLoader: ProgressBar, pageContent: RelativeLayout) {
        if(requestCounter==completedRequests) {
            pageLoader.visibility = View.GONE
            pageContent.visibility = View.VISIBLE
            setUpdateInterval()
        }
    }

    private fun EditText.autocapitalize() {
        val allCapsFilter = InputFilter.AllCaps()
        filters += allCapsFilter
    }


    fun initializeRequestQueue() {
        queue = Volley.newRequestQueue(this)
    }

    fun setAutoCompleteAPICalls(searchTicker: AutoCompleteTextView) {

        searchTicker.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {
                val url = "${resources.getString(R.string.server_url)}${resources.getString(R.string.autocomplete_api)}$s"
                val jsonObjectRequest = JsonObjectRequest (
                    Request.Method.GET, url, null,
                    { response ->
                        val compList = mutableListOf<String>()
                        val result = response.getJSONArray("result")
                        for (i in 0 until result.length()) {
                            val company = result.getJSONObject(i)
                            if(!company.getString("type").equals("Common Stock") || company.getString("displaySymbol").contains(".")) {
                                continue
                            }
                            compList.add("${company.getString("symbol").uppercase()} | ${company.getString("description").uppercase()}")
                        }
                        updateAutoCompleteAdapter(compList.toTypedArray(), searchTicker)
                    },
                    { /*Do nothing*/ })
                jsonObjectRequest.tag = s.toString()
                queue?.add(jsonObjectRequest)
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                queue?.cancelAll(s)
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchTicker.dismissDropDown()
            }

        })
    }

    fun updateAutoCompleteAdapter(data: Array<String>, searchTicker: AutoCompleteTextView) {
        autocompleteData.clear()
        autocompleteData.addAll(data)
        setAutoCompleteAdapter(searchTicker)
    }

    fun setAutoCompleteAdapter(searchTicker: AutoCompleteTextView) {
        autoCompleteAdapter = ArrayAdapter(applicationContext, android.R.layout.select_dialog_item, autocompleteData)
        searchTicker.threshold = 1
        searchTicker.setAdapter(autoCompleteAdapter)
        searchTicker.refreshAutoCompleteResults()
    }

    fun setAdapterAndItemClickListener(searchTicker: AutoCompleteTextView) {
        setAutoCompleteAdapter(searchTicker)
        searchTicker.onItemClickListener = OnItemClickListener { _, _, pos, _ ->
            val ticker = modifyAutocompleteSelectedOption(autocompleteData[pos])
            searchTicker.setText(ticker)
            searchTicker.setSelection(searchTicker.length())
            hideKeyboard(applicationContext, searchTicker)
            searchTicker.clearFocus()
            val intent = Intent(applicationContext, StockSummary::class.java).apply {
                putExtra(resources.getString(R.string.intent_stock_summary), ticker)
            }
            timer.cancel()
            timer.purge()
            startActivity(intent)
        }
        searchTicker.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                //Perform Code
                hideKeyboard(applicationContext, searchTicker)
                searchTicker.clearFocus()
                return@OnKeyListener true
            }
            false
        })
    }

    fun modifyAutocompleteSelectedOption(value: String): String {
        return value.split("|")[0].trim()
    }

    fun setCurrentDate(todayText: TextView) {
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        val currentDate = sdf.format(Date())
        todayText.text = currentDate.toString()
    }

    fun getSetCashBalance(sharedPref: SharedPreferences, fromPortfolio: Boolean = false, amount: Double = 0.0) {
        watchlistArrayList?.removeIf { data -> data.type == 2 }
        val currentCashBalance = sharedPref.getFloat(getString(R.string.cash_balance), 25000.00F)
        with (sharedPref.edit()) {
            putFloat(getString(R.string.cash_balance), currentCashBalance)
            apply()
        }
        if(fromPortfolio) {
            netWorth += amount
        } else {
            netWorth = currentCashBalance.toDouble()
        }
        watchlistArrayList?.add(FavoritesPortfolioDataModel(type = 2, netWorth = netWorth, cashBalance = currentCashBalance.toDouble()))
        watchlistAdapter!!.notifyDataSetChangedWithSort()
    }

    fun roundToTwoDecimalPlaces(value: Double): BigDecimal? {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)
    }

    fun openLinkOnBrowser(url: String) {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

    fun showSearchBar(searchToolbarLyt: View, toolbarLyt: View, searchTicker: View) {
        searchToolbarLyt.visibility = View.VISIBLE
        toolbarLyt.visibility = View.GONE
        openSoftKeyboard(applicationContext, searchTicker)
    }

    fun showNormalAppBar(searchToolbarLyt: View, toolbarLyt: View, searchTicker: View) {
        searchToolbarLyt.visibility = View.GONE
        toolbarLyt.visibility = View.VISIBLE
        hideKeyboard(applicationContext, searchTicker)
        searchTicker.clearFocus()
    }

    fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun openSoftKeyboard(context: Context, view: View) {
        view.requestFocus()
        // open the soft keyboard
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onResume() {
        super.onResume()
        if(shouldExecuteOnResume) {
            val sharedPref = this.getSharedPreferences(
                getString(R.string.stock_app_shared_pref),
                Context.MODE_PRIVATE
            )
            val pageLoader = findViewById<ProgressBar>(R.id.page_loader)
            val pageContent = findViewById<RelativeLayout>(R.id.page_content)
            fetchFavoritesData(sharedPref, pageLoader, pageContent)
            fetchPortfolioData(sharedPref, pageLoader, pageContent)
            getSetCashBalance(sharedPref)
        } else {
            shouldExecuteOnResume = true
        }
    }

    override fun onDestroy() {
        timer.cancel()
        timer.purge()
        super.onDestroy()
    }

    override fun onBackPressed() {
        finish()
    }


}