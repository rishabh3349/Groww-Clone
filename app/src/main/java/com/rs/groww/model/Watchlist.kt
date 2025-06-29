package com.rs.groww.model

data class Watchlist(
    val id: Int,
    val name: String,
    val stocks: List<Stock> = emptyList<Stock>()
)
