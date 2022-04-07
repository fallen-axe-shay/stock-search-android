package com.jerryallanakshay.stocks

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.hichartsclasses.*
import com.highsoft.highcharts.core.HIChartView
import org.json.JSONObject
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

class StockSummaryChart : Fragment() {

    private var ticker = ""
    private lateinit var stockChart: HIChartView
    private var options = HIOptions()
    private lateinit var closePrices: DoubleArray
    private lateinit var closeTimes: DoubleArray
    private var change: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_stock_summary_chart, container, false)
        stockChart = root.findViewById<HIChartView>(R.id.stock_summary_highcharts)
        setHighChartOptions()
        return root
    }

    fun setHighChartOptions() {
        val chart = HIChart()
        val title = HITitle()
        title.text = "$ticker Hourly Price Variation"
        val style = HICSSObject()
        style.color = HIColor.initWithHexValue("686868")
        title.style = style
        val exporting = HIExporting()
        exporting.enabled = false
        val tooltip = HITooltip()
        tooltip.split = true
        val xAxis = HIXAxis()
        val yAxis = HIYAxis()
        val xAxisTitle = HITitle()
        val yAxisTitle = HITitle()
        xAxisTitle.text = ""
        yAxisTitle.text = ""
        xAxis.title = xAxisTitle
        yAxis.title = yAxisTitle
        val crosshair = HICrosshair()
        xAxis.crosshair = crosshair
        xAxis.type = "datetime"
        val xAxisList = ArrayList<HIXAxis>()
        xAxisList.add(xAxis)
        val yAxisList = ArrayList<HIYAxis>()
        yAxisList.add(yAxis)
        val series = HISeries()
        series.type = "line"
        series.showInLegend = false
        series.name = ticker
        if(change>0) {
            series.color = HIColor.initWithHexValue("229e38")
        } else if(change<0) {
            series.color = HIColor.initWithHexValue("c50000")
        } else {
            series.color = HIColor.initWithName("black")
        }
        val seriesData = ArrayList<Array<Double>>()
        for(i in 0 until closePrices.size) {
            seriesData.add(arrayOf((closeTimes[i] * 1000) - 7*60*60*1000, closePrices.get(i)))
        }
        series.data = seriesData
        options.series = ArrayList(Collections.singletonList(series))
        options.chart = chart
        options.title = title
        options.exporting = exporting
        options.tooltip = tooltip
        options.xAxis = xAxisList
        options.yAxis = yAxisList
        stockChart.options = options
    }

    companion object {

        @JvmStatic
        fun newInstance(ticker: String, recentHistory: JSONObject, change: Double) = StockSummaryChart().apply {
            arguments = Bundle().apply {
                putString("ticker", ticker)
                val closePrices = ArrayList<Double>()
                val times = ArrayList<Double>()
                for(i  in 0 until recentHistory.getJSONArray("c").length()) {
                    closePrices.add(recentHistory.getJSONArray("c").getDouble(i))
                    times.add(recentHistory.getJSONArray("t").getDouble(i))
                }
                putDoubleArray("closePrices", closePrices.toTypedArray().toDoubleArray())
                putDoubleArray("times", times.toTypedArray().toDoubleArray())
                putDouble("change", change)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("ticker")?.let {
            ticker = it
        }
        arguments?.getDoubleArray("closePrices")?.let {
            closePrices = it
        }
        arguments?.getDoubleArray("times")?.let {
            closeTimes = it
        }
        arguments?.getDouble("change")?.let {
            change = it
        }
    }

}