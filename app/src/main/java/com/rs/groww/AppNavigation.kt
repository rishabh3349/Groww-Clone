package com.rs.groww

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rs.groww.model.Stock
import com.rs.groww.ui.screens.AddWatchlistBottomSheet
import com.rs.groww.ui.screens.DetailsScreen
import com.rs.groww.ui.screens.MainScreen
import com.rs.groww.ui.screens.ViewAllScreen
import com.rs.groww.ui.screens.WatchlistDetailScreen
import com.rs.groww.ui.screens.WatchlistScreen
import com.rs.groww.ui.screens.loadWatchlists
import com.rs.groww.viewmodel.StockViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var selectedStock by remember { mutableStateOf<Stock?>(null) }
    var checkedIds by remember { mutableStateOf(setOf<Int>()) }
    var showBottomSheet by remember { mutableStateOf(false) }
    val viewModel: StockViewModel = viewModel()
    val gainers by viewModel.gainers.collectAsState()
    val losers by viewModel.losers.collectAsState()

    NavHost(navController, startDestination = "home", modifier = modifier) {
        composable("home") {
            MainScreen(
                viewModel = viewModel,
                onStockClick = {
                    selectedStock = it
                    navController.navigate("details")
                },
                onViewAllGainers = { navController.navigate("gainers") },
                onViewAllLosers = { navController.navigate("losers") },
                onWatchlistClick = { watchlistId ->
                    navController.navigate("watchlistDetail/$watchlistId")
                }

            )
        }
        composable("gainers") {
            ViewAllScreen(
                title = "Top Gainers",
                stocks = gainers,
                onStockClick = {
                    selectedStock = it
                    navController.navigate("details")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("losers") {
            ViewAllScreen(
                title = "Top Losers",
                stocks = losers,
                onStockClick = {
                    selectedStock = it
                    navController.navigate("details")
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("details") {
            selectedStock?.let { stock ->
                DetailsScreen(
                    stock = stock,
                    onBack = { navController.popBackStack() },
                    onToggleBookmark = { showBottomSheet = true },
                    onAddToWatchlist = { showBottomSheet = true }
                )
                if (showBottomSheet) {
                    AddWatchlistBottomSheet(
                        selectedStock = stock,
                        checkedIds = checkedIds,
                        onCheckedChange = { id, checked ->
                            checkedIds = if (checked) checkedIds + id else checkedIds - id
                        },
                        onDismiss = {
                            showBottomSheet = false
                        },
                        showSheet = true
                    )
                }
            }
        }
        composable("watchlists") {
            WatchlistScreen(
                onWatchlistClick = { watchlist ->
                    navController.navigate("watchlistDetail/${watchlist.id}")
                }
            )
        }
        composable(
            "watchlistDetail/{watchlistId}",
            arguments = listOf(navArgument("watchlistId") { type = NavType.IntType })
        ) { backStackEntry ->
            val watchlistId = backStackEntry.arguments?.getInt("watchlistId") ?: -1
            val context = LocalContext.current
            val allWatchlists = loadWatchlists(context)
            val selectedWatchlist = allWatchlists.find { it.id == watchlistId }

            selectedWatchlist?.let { watchlist ->
                WatchlistDetailScreen(
                    watchlist = watchlist,
                    onStockClick={
                        selectedStock = it
                        navController.navigate("details")
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

