package com.jerryallanakshay.stocks

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {

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
        val searchTicker = findViewById<EditText>(R.id.search_ticker)
        val finnhubLinkText = findViewById<TextView>(R.id.finnhub_link)
        val currentCashBalance = findViewById<TextView>(R.id.current_cash_balance)

        val sharedPref = activity.getSharedPreferences(getString(R.string.stock_app_shared_pref), Context.MODE_PRIVATE)

        getSetCashBalance(sharedPref, currentCashBalance)

        searchBtn.setOnClickListener {
            //Toast.makeText(this, "You clicked me.", Toast.LENGTH_SHORT).show()
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
        searchTicker.clearFocus()
        hideKeyboard(applicationContext, searchTicker)
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