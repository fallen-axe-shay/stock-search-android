package com.jerryallanakshay.stocks

import java.math.BigDecimal
import java.math.RoundingMode

class FavoritesPortfolioDataModel(ticker: String = "", stockName: String = "", stockPrice: Double = 0.0, stockChange: Double = 0.0, stockChangePercent: Double = 0.0, type: Int, banner: String = "", netWorth: Double = 0.0, cashBalance: Double = 0.0) {

    val ticker: String = ticker
    val stockName: String = stockName
    var stockPrice: String = roundToTwoDecimalPlaces(stockPrice).toString()
    var stockChange: String = roundToTwoDecimalPlaces(stockChange).toString()
    var stockChangePercent: String = roundToTwoDecimalPlaces(stockChangePercent).toString()
    val type: Int = type
    val banner: String = banner
    var netWorth: String = roundToTwoDecimalPlaces(netWorth).toString()
    val cashBalance: String = roundToTwoDecimalPlaces(cashBalance).toString()

    private fun roundToTwoDecimalPlaces(value: Double): BigDecimal? {
        return BigDecimal(value).setScale(2, RoundingMode.HALF_EVEN)
    }

}