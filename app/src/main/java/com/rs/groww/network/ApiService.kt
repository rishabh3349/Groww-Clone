package com.rs.groww.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class TickerChangeItem(
    val ticker: String,
    val price: String,
    val change_amount: String,
    val change_percentage: String,
    val volume: String,
)

data class TopMoversResponse(
    val metadata: String? = null,
    val last_updated: String? = null,
    val top_gainers: List<TickerChangeItem> = emptyList(),
    val top_losers: List<TickerChangeItem> = emptyList(),
    val most_actively_traded: List<TickerChangeItem> = emptyList(),
)

data class StockOverview(
    val Name: String?,
    val Description: String?,
    val Sector: String?,
    val Industry: String?,
    val MarketCapitalization: String?,
    val PERatio: String?,
    val Beta: String?,
    val DividendYield: String?,
    val ProfitMargin: String?,
    val `52WeekHigh`: String?,
    val `52WeekLow`: String?,
)

data class IntradayResponse(
    @SerializedName("Time Series (5min)")
    val entries: Map<String, Map<String, String>> = emptyMap(),
)

data class DailyResponse(
    @SerializedName("Time Series (Daily)")
    val entries: Map<String, Map<String, String>> = emptyMap(),
)

data class MonthlyResponse(
    @SerializedName("Monthly Time Series")
    val entries: Map<String, Map<String, String>> = emptyMap(),
)

data class SymbolSearchRawResponse(
    @SerializedName("bestMatches")
    val bestMatches: List<Map<String, String>>?
) {
    fun toSymbolMatches(): List<SymbolMatch> {
        return bestMatches?.map {
            SymbolMatch(
                symbol = it["1. symbol"].orEmpty(),
                name = it["2. name"].orEmpty(),
                type = it["3. type"].orEmpty(),
                region = it["4. region"].orEmpty(),
                currency = it["8. currency"].orEmpty()
            )
        } ?: emptyList()
    }
}

data class SymbolSearchResponse(
    val bestMatches: List<SymbolMatch>
)

data class SymbolMatch(
    val symbol: String,
    val name: String,
    val type: String,
    val region: String,
    val currency: String
)

interface ApiService {
    @GET("query")
    suspend fun getTopMovers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey") apiKey: String,
    ): TopMoversResponse

    @GET("query")
    suspend fun getStockOverview(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): StockOverview

    @GET("query")
    suspend fun getIntraday(
        @Query("function") function: String = "TIME_SERIES_INTRADAY",
        @Query("symbol") symbol: String,
        @Query("interval") interval: String = "5min",
        @Query("apikey") apiKey: String,
    ): IntradayResponse

    @GET("query")
    suspend fun getDaily(
        @Query("function") function: String = "TIME_SERIES_DAILY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): DailyResponse

    @GET("query")
    suspend fun getMonthly(
        @Query("function") function: String = "TIME_SERIES_MONTHLY",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String,
    ): MonthlyResponse
    @GET("query")
    suspend fun searchSymbol(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): SymbolSearchRawResponse
}