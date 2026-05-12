package com.example.poopyrka.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.BackgroundLight
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: MainViewModel,
    onNavigateToDetails: (Long) -> Unit
) {
    val statsState by viewModel.statsState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = BackgroundLight
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
                onMonthChange = { viewModel.changeMonth(it) }
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
    onMonthChange: (Int) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy", Locale("ru")) }
    val monthText = remember(selectedMonth) {
        selectedMonth.format(formatter).replaceFirstChar { it.uppercase() }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = { onMonthChange(-1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev", tint = MainPurple)
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

        IconButton(onClick = { onMonthChange(1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = MainPurple)
        }
    }
}

@Composable
fun DaySummaryItem(
    summary: DaySummary,
    onClick: () -> Unit
) {
    val date = Instant.ofEpochMilli(summary.shift.date).atZone(ZoneId.systemDefault()).toLocalDate()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru")) }
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEEE", Locale("ru")) }

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
