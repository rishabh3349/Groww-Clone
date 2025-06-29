package com.rs.groww.ui.screens

import WatchlistItem
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rs.groww.model.Stock
import com.rs.groww.model.Watchlist
import kotlinx.coroutines.launch

private const val PREF_NAME = "watchlist_prefs"
private const val WATCHLIST_KEY = "watchlists"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWatchlistBottomSheet(
    selectedStock: Stock,
    checkedIds: Set<Int>,
    onCheckedChange: (Int, Boolean) -> Unit,
    showSheet: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var watchlists by remember { mutableStateOf(loadWatchlists(context)) }
    var newWatchlistName by remember { mutableStateOf("") }

    fun saveWatchlists(updatedList: List<Watchlist>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(updatedList)
        prefs.edit().putString(WATCHLIST_KEY, json).apply()
    }

    fun addWatchlist(name: String) {
        val newId = (watchlists.maxOfOrNull { it.id } ?: 0) + 1
        val newWatchlist = Watchlist(id = newId, name = name, stocks = emptyList())
        watchlists = (watchlists + newWatchlist).toMutableList()
        saveWatchlists(watchlists)
    }

    fun addStockToSelectedWatchlists() {
        watchlists = watchlists.map { watchlist ->
            if (checkedIds.contains(watchlist.id) && !watchlist.stocks.contains(selectedStock)) {
                watchlist.copy(stocks = watchlist.stocks + selectedStock)
            } else watchlist
        }.toMutableList()

        saveWatchlists(watchlists)
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.55f),
            containerColor = Color(0xFF121212) // Deep dark background
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add to Watchlist",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newWatchlistName,
                    onValueChange = { newWatchlistName = it },
                    placeholder = { Text("Enter Watchlist Name", color = Color.Gray) },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF1E1E1E),
                        focusedContainerColor = Color(0xFF1E1E1E),
                        unfocusedBorderColor = Color.Gray,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (newWatchlistName.isNotBlank()) {
                            addWatchlist(newWatchlistName)
                            newWatchlistName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Add", color = Color.White)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "Select Watchlists:",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    watchlists.forEach { watchlist ->
                        FilterChip(
                            selected = checkedIds.contains(watchlist.id),
                            onClick = {
                                onCheckedChange(
                                    watchlist.id,
                                    !checkedIds.contains(watchlist.id)
                                )
                            },
                            label = {
                                Text(
                                    watchlist.name,
                                    color = if (checkedIds.contains(watchlist.id)) MaterialTheme.colorScheme.primary else Color.White
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                containerColor = Color(0xFF1E1E1E),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                labelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        addStockToSelectedWatchlists()
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Stock to Watchlists", color = Color.White)
                }
            }
        }
    }
}

fun loadWatchlists(context: Context): MutableList<Watchlist> {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val json = prefs.getString(WATCHLIST_KEY, null) ?: return mutableListOf()
    val type = object : TypeToken<MutableList<Watchlist>>() {}.type
    return Gson().fromJson(json, type)
}
