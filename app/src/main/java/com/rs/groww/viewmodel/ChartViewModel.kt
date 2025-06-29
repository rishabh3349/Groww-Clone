package com.rs.groww.viewmodel

import android.provider.Contacts.SettingsColumns.KEY
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rs.groww.network.ApiService
import kotlinx.coroutines.launch

class ChartViewModel(private val api: ApiService) : ViewModel() {
    var entries by mutableStateOf<List<Float>>(emptyList()); private set

    fun load(symbol: String, rangeIndex: Int) = viewModelScope.launch {
        val result = try {
            when (rangeIndex) {
                0 -> parseCloses(api.getIntraday(symbol=symbol, apiKey=KEY).entries)
                in 1..3 -> {
                    val all = parseCloses(api.getDaily(symbol=symbol, apiKey=KEY).entries)
                    all.take(when(rangeIndex){
                        1 -> 7
                        2 -> 22
                        else -> 66
                    })
                }
                else -> parseCloses(api.getMonthly(symbol=symbol, apiKey=KEY).entries)
            }
        } catch (e: Exception) {
            emptyList()
        }
        entries = result
    }

    private fun parseCloses(ts: Map<String, Map<String, String>>) =
        ts.entries
            .sortedBy { it.key }
            .mapNotNull { it.value["4. close"]?.toFloatOrNull() }
}