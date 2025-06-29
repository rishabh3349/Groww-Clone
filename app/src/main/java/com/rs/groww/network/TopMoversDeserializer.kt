package com.rs.groww.network

import com.google.gson.*
import java.lang.reflect.Type

class TopMoversDeserializer : JsonDeserializer<TopMoversResponse> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): TopMoversResponse {
        val obj = json?.asJsonObject ?: return TopMoversResponse()
        fun parseArray(name: String) = obj[name]?.asJsonArray?.map {
            it.asJsonObject.let { i ->
                TickerChangeItem(
                    ticker = i["ticker"].asString,
                    price = i["price"].asString,
                    change_amount = i["change_amount"].asString,
                    change_percentage = i["change_percentage"].asString,
                    volume = i["volume"].asString
                )
            }
        } ?: emptyList()

        return TopMoversResponse(
            metadata = obj["metadata"]?.asString,
            last_updated = obj["last_updated"]?.asString,
            top_gainers = parseArray("top_gainers"),
            top_losers = parseArray("top_losers"),
            most_actively_traded = parseArray("most_actively_traded")
        )
    }
}