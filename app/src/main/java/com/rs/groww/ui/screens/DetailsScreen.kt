package com.rs.groww.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rs.groww.model.Stock
import com.rs.groww.network.RetrofitInstance
import com.rs.groww.network.StockOverview
import com.rs.groww.viewmodel.ChartViewModel
import com.rs.groww.viewmodel.ChartViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    stock: Stock,
    onBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onAddToWatchlist: () -> Unit,
) {
    val api = remember { com.rs.groww.network.RetrofitInstance.api }
    val factory = remember { ChartViewModelFactory(api) }
    val viewModel: ChartViewModel = viewModel(factory = factory)
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    var overview by remember { mutableStateOf<StockOverview?>(null) }
    var selectedRange by remember { mutableStateOf(0) }
    val entries by remember { derivedStateOf { viewModel.entries } }
    LaunchedEffect(stock.symbol, selectedRange) {
        viewModel.load(stock.symbol, selectedRange)
    }

    LaunchedEffect(stock.symbol) {
        viewModel.load(stock.symbol, selectedRange)
        coroutineScope.launch {
            try {
                overview = RetrofitInstance.api.getStockOverview(
                    symbol = stock.symbol,
                    apiKey = RetrofitInstance.getApiKey()
                )
            } catch (e: Exception) {
                Log.e("DetailsScreen", "Error fetching overview", e)
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Details", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .background(Color.Black)
        ) {
            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.DarkGray, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stock.name.first().uppercaseChar().toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stock.name.uppercase(), fontWeight = FontWeight.Bold, color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("${stock.symbol}, Common Stock", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text("NSQ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("$${stock.price}", fontWeight = FontWeight.Bold, color = Color(0xFF00E676), style = MaterialTheme.typography.titleMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val rising = stock.change.startsWith("+")
                        Icon(
                            if (rising) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = if (rising) Color(0xFF00E676) else Color.Red
                        )
                        Text(stock.change, color = if (rising) Color(0xFF00E676) else Color.Red, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Chart
            Surface(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Color.Gray),
                color = Color.DarkGray
            ) {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    SimpleLineChart(
                        dataPoints = entries,
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        lineColor = Color(0xFF00E676)
                    )
                    Spacer(Modifier.height(8.dp))
                    RangeSelector(
                        ranges = listOf("1D", "1W", "1M", "3M", "6M", "1Y"),
                        selected = selectedRange,
                        onSelect = { selectedRange = it }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // About Section
            overview?.let {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.DarkGray,
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("About ${stock.name.uppercase()}", fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(4.dp))
                        Text(it.Description ?: "No description available", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    }
                }

                Spacer(Modifier.height(12.dp))

//                // Tags
//                Row {
//                    it.Industry?.let { Pill("Industry: $it") }
//                    Spacer(Modifier.width(8.dp))
//                    it.Sector?.let { Pill("Sector: $it") }
//                }
//
//                Spacer(Modifier.height(16.dp))

                // Stats
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    color = Color.DarkGray
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Stat("52-Week Low", it.`52WeekLow` ?: "-")
                            Stat("Current", "$${stock.price}")
                            Stat("52-Week High", it.`52WeekHigh` ?: "-")
                        }

                        Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Stat("Market Cap", it.MarketCapitalization ?: "-")
                            Stat("P/E Ratio", it.PERatio ?: "-")
                            Stat("Beta", it.Beta ?: "-")
                            Stat("Dividend", it.DividendYield ?: "-")
                            Stat("Margin", it.ProfitMargin ?: "-")
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onAddToWatchlist,
                modifier = Modifier.align(Alignment.CenterHorizontally).width(220.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black)
            ) {
                Text("Add to Watchlist")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
@Composable
private fun SimpleLineChart(
    dataPoints: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF00E676) // Bright Green for dark background
) {
    if (dataPoints.isEmpty()) return

    Canvas(modifier = modifier) {
        val widthStep = size.width / (dataPoints.size - 1)
        val maxY = dataPoints.maxOrNull() ?: 1f
        val minY = dataPoints.minOrNull() ?: 0f
        val heightRange = maxY - minY

        val points = dataPoints.mapIndexed { index, value ->
            val x = index * widthStep
            val y = size.height - ((value - minY) / heightRange) * size.height
            androidx.compose.ui.geometry.Offset(x, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }

        drawPath(path, color = lineColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
    }
}

@Composable
private fun RangeSelector(ranges: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        ranges.forEachIndexed { index, label ->
            val selectedColor = Color(0xFF00E676) // Green for selected chip
            val outline = Color.Gray

            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label, fontSize = 10.sp, color = if (selected == index) selectedColor else Color.White) },
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .height(32.dp)
                    .width(48.dp),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = outline,
                    disabledBorderColor = outline,
                    enabled = true,
                    selected = false
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.DarkGray,
                    selectedContainerColor = selectedColor.copy(alpha = 0.25f),
                    labelColor = Color.White,
                    selectedLabelColor = selectedColor
                )
            )
        }
    }
}

@Composable
private fun Pill(text: String) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF424242) // Darker gray for dark mode pill
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
private fun Stat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, color = Color.White)
    }
}
