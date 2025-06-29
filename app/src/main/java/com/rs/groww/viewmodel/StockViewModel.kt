package com.rs.groww.viewmodel

import androidx.lifecycle.ViewModel
import com.rs.groww.model.Stock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class StockViewModel : ViewModel() {
    private val _gainers = MutableStateFlow<List<Stock>>(emptyList())
    val gainers: StateFlow<List<Stock>> = _gainers

    private val _losers = MutableStateFlow<List<Stock>>(emptyList())
    val losers: StateFlow<List<Stock>> = _losers

    fun updateGainers(list: List<Stock>) {
        _gainers.value = list
    }

    fun updateLosers(list: List<Stock>) {
        _losers.value = list
    }
}