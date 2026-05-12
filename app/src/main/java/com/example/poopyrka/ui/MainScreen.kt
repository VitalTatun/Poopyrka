package com.example.poopyrka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.data.WorkShift
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.BackgroundLight
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A4A4A)
                )
            }
            items(entries) { entry ->
                ShipmentItem(
                    entry = entry,
                    onClick = { onEntryClick(entry.id) }
                )
            }
        }
    }
}

@Composable
fun ShipmentItem(entry: ShipmentEntry, onClick: (() -> Unit)? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FiberManualRecord,
                contentDescription = null,
                tint = MainPurple,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = entry.pointName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${entry.count} поз.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Text(
                text = "${(entry.count * 1.0).toInt()} BYN",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
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
            color = MainPurple.copy(alpha = 0.6f),
            fontSize = 18.sp,
            lineHeight = 26.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartShift,
            modifier = Modifier.size(80.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = MainPurple),
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
