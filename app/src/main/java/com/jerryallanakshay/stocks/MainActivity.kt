package com.jerryallanakshay.stocks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private var queue: RequestQueue? = null
    private var autocompleteData: MutableList<String> = mutableListOf<String>()
    private var autoCompleteAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Stocks)
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
        val currentCashBalance = findViewById<TextView>(R.id.current_cash_balance)
        val todayText = findViewById<TextView>(R.id.date_today)

        val sharedPref = activity.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)

        setAdapterAndItemClickListener(searchTicker)

        initializeRequestQueue()
        setAutoCompleteAPICalls(searchTicker)

        setCurrentDate(todayText)
        getSetCashBalance(sharedPref, currentCashBalance)

        searchBtn.setOnClickListener {
            showSearchBar(searchToolbarLyt, toolbarLyt, searchTicker)
        }

        backSearchBtn.setOnClickListener {
            showNormalAppBar(searchToolbarLyt, toolbarLyt, searchTicker)
        }

        closeSearchBtn.setOnClickListener {
            showNormalAppBar(searchToolbarLyt, toolbarLyt, searchTicker)
        }

        finnhubLinkText.setOnClickListener {
            openLinkOnBrowser("https://finnhub.io/")
        }



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

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

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
            searchTicker.setText(modifyAutocompleteSelectedOption(autocompleteData.get(pos)))
            searchTicker.setSelection(searchTicker.length())
            hideKeyboard(applicationContext, searchTicker)
            searchTicker.clearFocus()
        }
    }

    fun modifyAutocompleteSelectedOption(value: String): String {
        return value.split("|")[0].trim()
    }

    fun setCurrentDate(todayText: TextView) {
        val sdf = SimpleDateFormat("dd MMMM yyyy")
        val currentDate = sdf.format(Date())
        todayText.text = currentDate.toString()
    }

    fun getSetCashBalance(sharedPref: SharedPreferences, currentCashBalanceText: TextView) {
        val currentCashBalance = sharedPref.getFloat(getString(R.string.cash_balance), 25000.00F)
        with (sharedPref.edit()) {
            putFloat(getString(R.string.cash_balance), currentCashBalance)
            apply()
        }
        currentCashBalanceText.text = roundToTwoDecimalPlaces(currentCashBalance.toDouble()).toString()
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


}