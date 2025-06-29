package com.rs.groww.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.rs.groww.AppNavigation
import com.rs.groww.R
import com.rs.groww.model.Stock
import com.rs.groww.network.RetrofitInstance
import com.rs.groww.network.SymbolMatch
import com.rs.groww.ui.components.LoadingStockCard
import com.rs.groww.ui.components.StockCard
import com.rs.groww.viewmodel.StockViewModel
import kotlinx.coroutines.launch

@Composable
fun MainScreen(
    viewModel: StockViewModel,
    onStockClick: (Stock) -> Unit,
    onViewAllGainers: () -> Unit,
    onViewAllLosers: () -> Unit,
    onWatchlistClick: (Int) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White) },
                    label = { Text("Home", color = Color.White) },
                    colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.DarkGray
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Star, contentDescription = "Watchlist",tint = Color.White) },
                    label = { Text("Watchlist", color = Color.White) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        unselectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.DarkGray
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).background(Color.Black)) {
            if (selectedTab == 0) {
                HomeScreenContent(
                    viewModel= viewModel,
                    onStockClick = onStockClick,
                    onViewAllGainers = onViewAllGainers,
                    onViewAllLosers = onViewAllLosers

                )
            } else {
                WatchlistScreen(
                    onWatchlistClick = { watchlist ->
                        onWatchlistClick(watchlist.id)
                    }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    viewModel: StockViewModel,
    onStockClick: (Stock) -> Unit,
    onViewAllGainers: () -> Unit,
    onViewAllLosers: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var gainers by remember { mutableStateOf<List<Stock>>(emptyList()) }
    var losers by remember { mutableStateOf<List<Stock>>(emptyList()) }
    val scrollState = rememberScrollState()
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SymbolMatch>>(emptyList()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val response =
                    RetrofitInstance.api.getTopMovers(apiKey = RetrofitInstance.getApiKey())
                Log.d("HomeScreen", "✅ Full API response: $response")

                response.top_gainers.forEach { Log.d("HomeScreen", "Gainer → $it") }
                response.top_losers.forEach { Log.d("HomeScreen", "Loser  → $it") }

                val gainersList = response.top_gainers.map {
                    Stock(
                        it.ticker,
                        it.ticker,
                        it.price.toDoubleOrNull() ?: 0.0,
                        it.change_percentage
                    )
                }
                val losersList = response.top_losers.map {
                    Stock(
                        it.ticker,
                        it.ticker,
                        it.price.toDoubleOrNull() ?: 0.0,
                        it.change_percentage
                    )
                }

                gainers = gainersList.take(4)
                losers = losersList.take(4)

                viewModel.updateGainers(gainersList)
                viewModel.updateLosers(losersList)
            } catch (e: Exception) {
                Log.e("HomeScreen", "Fetch error", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 8.dp)
            .verticalScroll(scrollState)
            .background(Color(0xFF000000))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.DarkGray, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "logo"
                )
            }
            OutlinedTextField(
                value = query,
                onValueChange = { text ->
                    query = text
                    if (query.length >= 2) {
                        coroutineScope.launch {
                            try {
                                val response = RetrofitInstance.api.searchSymbol(
                                    keywords = query,
                                    apiKey = RetrofitInstance.getApiKey()
                                )
                                results = response.toSymbolMatches()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                placeholder = { Text("Search stocks...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFF000000),
                    focusedContainerColor = Color(0xFF000000),
                    unfocusedBorderColor = Color(0xFFFFFFFF),
                    focusedBorderColor = Color(0xFFFFFFFF)
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            results.forEach { match ->
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(match.name, style = MaterialTheme.typography.titleSmall)
                        Text(match.symbol, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("${match.region} | ${match.currency}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        // Gainers
        SectionWithViewAll(title = "Top Gainers", onViewAll = onViewAllGainers) {
            TwoByTwoGrid(stocks = gainers, onStockClick = onStockClick)
        }

        Spacer(Modifier.height(12.dp))

        // Losers
        SectionWithViewAll(title = "Top Losers", onViewAll = onViewAllLosers) {
            TwoByTwoGrid(stocks = losers, onStockClick = onStockClick)
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun SectionWithViewAll(title: String, onViewAll: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontStyle = FontStyle.Italic,
                color = Color(0xFFFFFFFF)
            )
            TextButton(onClick = onViewAll) {
                Text("View All", color = Color(0xFFFFFFFF))
            }
        }
        Divider(color = Color(0xFFFFFFFF) , thickness = 1.dp)
        content()
    }
}

@Composable
fun TwoByTwoGrid(stocks: List<Stock>, onStockClick: (Stock) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
            .padding(horizontal = 12.dp)
    ) {
        if (stocks.isEmpty()) {
            repeat(2) { // 2 rows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) {
                        LoadingStockCard(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        } else {
            for (i in stocks.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StockCard(
                        stock = stocks[i],
                        onClick = { onStockClick(stocks[i]) },
                        modifier = Modifier.weight(1f)
                    )
                    if (i + 1 < stocks.size) {
                        StockCard(
                            stock = stocks[i + 1],
                            onClick = { onStockClick(stocks[i + 1]) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}



@Composable
fun WatchlistScreenContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your Watchlist",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(16.dp))
        Text("This is where your watchlisted stocks will appear.")
    }
}