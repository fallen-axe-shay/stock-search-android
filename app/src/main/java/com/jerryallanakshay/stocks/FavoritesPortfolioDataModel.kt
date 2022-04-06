package com.jerryallanakshay.stocks

import java.math.BigDecimal
import java.math.RoundingMode

class FavoritesPortfolioDataModel(ticker: String, stockName: String, stockPrice: Double, stockChange: Double, stockChangePercent: Double) {

    val ticker: String = ticker
    val stockName: String = stockName
    val stockPrice: String = roundToTwoDecimalPlaces(stockPrice).toString()
    val stockChange: String = roundToTwoDecimalPlaces(stockChange).toString()
    val stockChangePercent: String = roundToTwoDecimalPlaces(stockChangePercent).toString()

    private fun roundToTwoDecimalPlaces(value: Double): BigDecimal? {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)
    }

}