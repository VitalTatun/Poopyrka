package com.example.poopyrka.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.ui.theme.BackgroundLight
import com.example.poopyrka.ui.theme.MainPurple
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

    val formatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("ru")) }
    val monthText = remember(dayState.currentShift) {
        dayState.currentShift?.let {
            Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        } ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(monthText, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        if (dayState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MainPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val date = dayState.currentShift?.let {
                    Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                }
                val dayName = date?.format(DateTimeFormatter.ofPattern("EEEE", Locale("ru"))) ?: ""
                
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    groupedEntries.forEach { (group, entries) ->
                        item {
                            Text(
                                text = when (group) {
                                    1 -> "Первая отгрузка"
                                    2 -> "Вторая отгрузка"
                                    3 -> "Третья отгрузка"
                                    else -> "$group отгрузка"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(entries) { entry ->
                            ShipmentItem(
                                entry = entry,
                                onClick = { onNavigateToEditEntry(entry.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
