package com.example.poopyrka.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.data.WorkShift
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.BackgroundLight
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
            CenterAlignedTopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = BackgroundLight,
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MonthSelector(
                selectedMonth = statsState.selectedMonth,
                canGoBack = statsState.canGoBack,
                canGoForward = statsState.canGoForward,
                onMonthChange = onMonthChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Всего за месяц", color = Color.Gray, fontSize = 16.sp)
            Text(
                "${statsState.monthlyTotalEarnings.toInt()} BYN",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MainPurple
            )
            Text(
                "${statsState.monthlyTotalLines} поз.",
                fontSize = 18.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(statsState.daySummaries) { summary ->
                    DaySummaryItem(
                        summary = summary,
                        onClick = { onNavigateToDetails(summary.shift.id) }
                    )
                }
            }
        }
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(
            onClick = { onMonthChange(-1) },
            enabled = canGoBack
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Prev",
                tint = if (canGoBack) MainPurple else Color.Gray.copy(alpha = 0.3f)
            )
        }

        Surface(
            color = MainPurple,
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = monthText,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(
            onClick = { onMonthChange(1) },
            enabled = canGoForward
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next",
                tint = if (canGoForward) MainPurple else Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun DaySummaryItem(
    summary: DaySummary,
    onClick: () -> Unit
) {
    val date = Instant.ofEpochMilli(summary.shift.date).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru")) }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ru")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${summary.earnings.toInt()} BYN",
                    fontWeight = FontWeight.Bold,
                    color = MainPurple,
                    fontSize = 16.sp
                )
                Text(
                    text = "${summary.totalLines} поз.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = summary.coeffLabel,
                    color = MainPurple.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatisticsScreenPreview() {
    PoopyrkaTheme {
        StatisticsScreenContent(
            statsState = StatisticsUiState(
                selectedMonth = YearMonth.of(2026, 5),
                daySummaries = listOf(
                    DaySummary(WorkShift(1, 1777939200000, true), 105, 105.0, "Коэф 1.0"),
                    DaySummary(WorkShift(2, 1778025600000, true), 105, 105.0, "Коэф 1.0"),
                    DaySummary(WorkShift(3, 1778112000000, true), 105, 105.0, "Коэф 1.0"),
                    DaySummary(WorkShift(4, 1778371200000, true), 105, 105.0, "Коэф 1.0"),
                    DaySummary(WorkShift(5, 1778457600000, true), 105, 105.0, "Коэф 1.0"),
                ),
                monthlyTotalEarnings = 709.0,
                monthlyTotalLines = 709,
                isLoading = false,
                canGoBack = true,
                canGoForward = false
            ),
            onMonthChange = {},
            onNavigateToDetails = {}
        )
    }
}
