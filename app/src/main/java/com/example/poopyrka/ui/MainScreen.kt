package com.example.poopyrka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.data.WorkShift
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToAddEntry: () -> Unit,
    onNavigateToEditEntry: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    MainScreenContent(
        uiState = uiState,
        onStartShift = { viewModel.startShift() },
        onCloseShift = { viewModel.closeShift() },
        onAddEntry = onNavigateToAddEntry,
        onEntryClick = onNavigateToEditEntry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContent(
    uiState: MainUiState,
    onStartShift: () -> Unit,
    onCloseShift: () -> Unit,
    onAddEntry: () -> Unit,
    onEntryClick: (Long) -> Unit
) {
    var showCloseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "POOPyrka",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    if (uiState.currentShift != null) {
                        IconButton(onClick = onAddEntry) {
                            Icon(Icons.Default.Add, contentDescription = "Add Entry", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (uiState.currentShift == null) {
            EmptyState(
                onStartShift = onStartShift,
                modifier = Modifier.padding(padding)
            )
        } else {
            MainContent(
                uiState = uiState,
                onCloseShiftClick = { showCloseDialog = true },
                onEntryClick = onEntryClick,
                modifier = Modifier.padding(padding)
            )
        }
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("Закрыть смену?") },
            text = { Text("Вы уверены, что хотите завершить текущую смену?") },
            confirmButton = {
                TextButton(onClick = {
                    onCloseShift()
                    showCloseDialog = false
                }) {
                    Text("Да, закрыть")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun MainContent(
    uiState: MainUiState,
    onCloseShiftClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val groupedEntries = uiState.entries.groupBy { it.deliveryGroup }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            ShiftSummaryCard(
                date = uiState.currentShift?.date ?: 0L,
                earnings = uiState.totalEarnings,
                totalLines = uiState.totalLines,
                onCloseClick = onCloseShiftClick
            )
        }

        groupedEntries.forEach { (group, entries) ->
            item {
                Text(
                    text = when (group) {
                        1 -> "Первая отгрузка"
                        2 -> "Вторая отгрузка"
                        3 -> "Третья отгрузка"
                        else -> "$group отгрузка"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    entries.forEachIndexed { index, entry ->
                        val shape = when {
                            entries.size == 1 -> RoundedCornerShape(16.dp)
                            index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                            index == entries.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                            else -> RoundedCornerShape(4.dp)
                        }
                        ShipmentItem(
                            entry = entry,
                            shape = shape,
                            onClick = { onEntryClick(entry.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentItem(
    entry: ShipmentEntry,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = entry.pointName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${entry.count} поз.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "${(entry.count * 1.0).toInt()} BYN",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EmptyState(onStartShift: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ничего не упаковано,\nничего не отправлено...\nГрошай няма\nСитуация патовая...",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            fontSize = 18.sp,
            lineHeight = 26.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartShift,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            contentPadding = PaddingValues(0.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Start", modifier = Modifier.size(40.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenActivePreview() {
    PoopyrkaTheme {
        MainScreenContent(
            uiState = MainUiState(
                currentShift = WorkShift(id = 1, date = System.currentTimeMillis()),
                entries = listOf(
                    ShipmentEntry(1, 1, "WB Палома", 25, 1),
                    ShipmentEntry(2, 1, "WB ЕСЦ", 25, 1),
                    ShipmentEntry(3, 1, "Озон Палома", 25, 2),
                    ShipmentEntry(4, 1, "Озон ЕСЦ", 25, 2),
                    ShipmentEntry(5, 1, "EMall", 25, 2),
                    ShipmentEntry(6, 1, "WB Палома", 25, 3),
                ),
                totalLines = 150,
                totalEarnings = 150.0,
                isLoading = false
            ),
            onStartShift = {},
            onCloseShift = {},
            onAddEntry = {},
            onEntryClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenEmptyPreview() {
    PoopyrkaTheme {
        MainScreenContent(
            uiState = MainUiState(currentShift = null, isLoading = false),
            onStartShift = {},
            onCloseShift = {},
            onAddEntry = {},
            onEntryClick = {}
        )
    }
}
