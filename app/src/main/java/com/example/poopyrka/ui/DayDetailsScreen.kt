package com.example.poopyrka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailsScreen(
    shiftId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onNavigateToEditEntry: (Long) -> Unit
) {
    val dayState by viewModel.getDayDetails(shiftId).collectAsState(initial = MainUiState())

    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru")) }
    val monthText = remember(dayState.currentShift) {
        dayState.currentShift?.let {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        } ?: ""
    }

    val locale = remember { Locale.forLanguageTag("ru") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(monthText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (dayState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainPurple)
            }
        } else {
            DayDetailsContent(
                modifier = Modifier.padding(padding),
                dayState = dayState,
                onNavigateToEditEntry = onNavigateToEditEntry
            )
        }
    }
}

@Composable
fun DayDetailsContent(
    modifier: Modifier = Modifier,
    dayState: MainUiState,
    onNavigateToEditEntry: (Long) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val date = dayState.currentShift?.let {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
        }

        val dayName = date?.format(DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru"))) ?: ""
        Text(
            text = "${date?.dayOfMonth ?: ""} ${date?.format(DateTimeFormatter.ofPattern("MMMM", Locale("ru"))) ?: ""}",
            fontSize = 18.sp,
            color = MainPurple,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = dayName.replaceFirstChar { it.uppercase() },
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "${dayState.totalEarnings.toInt()} BYN",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MainPurple
        )
        Text(
            text = "${dayState.totalLines} поз.",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        val groupedEntries = dayState.entries.groupBy { it.deliveryGroup }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            groupedEntries.forEach { (group, entries) ->
                item {
                    Column {
                        Text(
                            text = when (group) {
                                1 -> "Первая отгрузка"
                                2 -> "Вторая отгрузка"
                                3 -> "Третья отгрузка"
                                else -> "$group отгрузка"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                        )

                        // РЕАЛИЗАЦИЯ С GAP 2.dp (как в статистике)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            entries.forEachIndexed { index, entry ->
                                val shape = when {
                                    entries.size == 1 -> RoundedCornerShape(16.dp)
                                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                    index == entries.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                    else -> RoundedCornerShape(2.dp)
                                }

                                ShipmentItem(
                                    entry = entry,
                                    shape = shape,
                                    onClick = { onNavigateToEditEntry(entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShipmentItem(
    entry: ShipmentEntry,
    shape: Shape,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = onClick),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                text = "${entry.count} BYN",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayDetailsPreview() {
    PoopyrkaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            DayDetailsContent(
                dayState = MainUiState(
                    currentShift = com.example.poopyrka.data.WorkShift(1, 1778889600000, true),
                    entries = listOf(
                        ShipmentEntry(1, 1, "Точка А", 45, 1),
                        ShipmentEntry(2, 1, "Точка Б", 8, 1),
                        ShipmentEntry(3, 1, "Точка В", 15, 2)
                    ),
                    totalEarnings = 187.0,
                    totalLines = 187
                ),
                onNavigateToEditEntry = {}
            )
        }
    }
}