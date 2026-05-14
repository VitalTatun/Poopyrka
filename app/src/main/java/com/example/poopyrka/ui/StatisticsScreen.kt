package com.example.poopyrka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import com.example.poopyrka.data.WorkShift
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (Long) -> Unit
) {
    val statsState by viewModel.statsState.collectAsState()

    StatisticsScreenContent(
        statsState = statsState,
        onMonthChange = { viewModel.changeMonth(it) },
        onNavigateToDetails = onNavigateToDetails
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreenContent(
    statsState: StatisticsUiState,
    onMonthChange: (Int) -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Medium) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                MonthSelector(
                    selectedMonth = statsState.selectedMonth,
                    canGoBack = statsState.canGoBack,
                    canGoForward = statsState.canGoForward,
                    onMonthChange = onMonthChange
                )
                Spacer(modifier = Modifier.height(24.dp))
                MonthlySummary(
                    totalEarnings = statsState.monthlyTotalEarnings,
                    totalLines = statsState.monthlyTotalLines
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            // РЕАЛИЗАЦИЯ CONTAINED LIST (M3 Gap Strategy)
            if (statsState.daySummaries.isNotEmpty()) {
                item {
                    // Используем Column как контейнер для группы с Gaps
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp) // Тот самый GAP в 2dp
                    ) {
                        statsState.daySummaries.forEachIndexed { index, summary ->
                            // Логика shape остается для эффекта "единого блока"
                            val shape = when {
                                statsState.daySummaries.size == 1 -> RoundedCornerShape(16.dp)
                                    index == 0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                                index == statsState.daySummaries.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                                else -> RoundedCornerShape(2.dp) // Средние элементы с едва заметным скруглением
                            }

                            DaySummaryItem(
                                summary = summary,
                                shape = shape,
                                onClick = { onNavigateToDetails(summary.shift.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummary(
    totalEarnings: Double,
    totalLines: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Всего за месяц",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
        Text(
            "${totalEarnings.toInt()} BYN",
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "$totalLines поз.",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MonthSelector(
    selectedMonth: YearMonth,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onMonthChange: (Int) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale.forLanguageTag("ru")) }
    val monthText = remember(selectedMonth) {
        selectedMonth.format(formatter).replaceFirstChar { it.uppercase() }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = { onMonthChange(-1) }, enabled = canGoBack) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = null,
                tint = if (canGoBack) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.3f)
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(100.dp)
        ) {
            Text(
                text = monthText,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        IconButton(onClick = { onMonthChange(1) }, enabled = canGoForward) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = if (canGoForward) MaterialTheme.colorScheme.primary else Color.Gray.copy(0.3f)
            )
        }
    }
}

@Composable
fun DaySummaryItem(
    summary: DaySummary,
    shape: Shape, // Добавляем этот параметр
    onClick: () -> Unit
) {
    val date = Instant.ofEpochMilli(summary.shift.date).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM", Locale.forLanguageTag("ru")) }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru")) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            // Важно: clip идет ПЕРЕД clickable, чтобы Ripple эффект не вылезал за углы
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
            Column {
                Text(
                    text = date.format(dateFormatter),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = date.format(dayFormatter).replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${summary.earnings.toInt()} BYN",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
                Text(
                    text = "${summary.totalLines} поз. • ${summary.coeffLabel}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    PoopyrkaTheme() {
        // Обертываем в Surface для корректного фона в превью
        Surface(color = MaterialTheme.colorScheme.background) {
            StatisticsScreenContent(
                statsState = StatisticsUiState(
                    selectedMonth = YearMonth.of(2026, 5),
                    daySummaries = listOf(
                        DaySummary(
                            shift = WorkShift(id = 1, date = 1778889600000, isClosed = true), // 12 мая 2026
                            totalLines = 1200,
                            earnings = 960.0,
                            coeffLabel = "Коэф 0.8"
                        ),
                        DaySummary(
                            shift = WorkShift(id = 2, date = 1778803200000, isClosed = true), // 11 мая 2026
                            totalLines = 850,
                            earnings = 850.0,
                            coeffLabel = "Коэф 1.0"
                        ),
                        DaySummary(
                            shift = WorkShift(id = 3, date = 1778716800000, isClosed = true), // 10 мая 2026
                            totalLines = 1000,
                            earnings = 1000.0,
                            coeffLabel = "Коэф 1.0"
                        )
                    ),
                    monthlyTotalEarnings = 2810.0,
                    monthlyTotalLines = 3050,
                    isLoading = false,
                    canGoBack = true,
                    canGoForward = false
                ),
                onMonthChange = {},
                onNavigateToDetails = {}
            )
        }
    }
}