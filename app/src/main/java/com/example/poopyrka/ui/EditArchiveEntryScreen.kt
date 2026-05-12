package com.example.poopyrka.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.BackgroundLight
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditArchiveEntryScreen(
    entryId: Long,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.getEntry(entryId).collectAsState(initial = null)
    
    var count by remember { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableIntStateOf(1) }
    
    val dateText = remember(state) {
        state?.second?.let {
            val dt = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDateTime()
            dt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy, EEEE", java.util.Locale.forLanguageTag("ru")))
        } ?: ""
    }
    
    LaunchedEffect(state) {
        state?.first?.let {
            count = it.count.toString()
            selectedPoint = it.pointName
            selectedGroup = it.deliveryGroup
        }
    }

    val isFormValid = count.isNotBlank() && (count.toIntOrNull() ?: 0) > 0 && selectedPoint.isNotBlank()
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Редактирование", fontWeight = FontWeight.Bold)
                        if (dateText.isNotEmpty()) {
                            Text(dateText, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Normal)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteEntryById(entryId)
                        Toast.makeText(context, "Запись удалена, итог смены пересчитан", Toast.LENGTH_SHORT).show()
                        onBack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Gray)
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isFormValid) MainPurple else Color.LightGray)
                            .then(
                                if (isFormValid) {
                                    Modifier.clickable {
                                        state?.first?.let {
                                            viewModel.updateEntry(it.copy(
                                                pointName = selectedPoint,
                                                count = count.toInt(),
                                                deliveryGroup = selectedGroup
                                            ))
                                            Toast.makeText(context, "Данные обновлены, итог смены пересчитан", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить", tint = Color.White)
                    }
                }
            )
        },
        containerColor = BackgroundLight
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            val points = listOf("WB Палома", "WB ЕСЦ", "Ozon Палома", "Ozon ЕСЦ", "EMall", "FBO WB", "FBO Ozon")

            val displayDate = remember(state) {
                state?.second?.let {
                    val dt = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
                    dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                } ?: ""
            }

            // Поле Дата
            OutlinedTextField(
                value = displayDate,
                onValueChange = {},
                label = { Text("Дата") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Поле Количество
            OutlinedTextField(
                value = count,
                onValueChange = { if (it.all { char -> char.isDigit() }) count = it },
                label = { Text("Количество строк") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    if (count.isNotEmpty()) {
                        IconButton(onClick = { count = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            )

            // Направления
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Направление", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    points.forEach { point ->
                        val isSelected = selectedPoint == point
                        Surface(
                            onClick = { selectedPoint = point },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MainPurple else Color.White,
                            border = if (isSelected) null else BorderStroke(1.dp, Color.LightGray),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = point,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            // Номер отгрузки
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Отгрузка", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(1, 2, 3).forEach { group ->
                        val isSelected = selectedGroup == group
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MainPurple else Color(0xFFF2F2F7))
                                .clickable { selectedGroup = group }
                                .then(if (isSelected) Modifier else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = group.toString(),
                                color = if (isSelected) Color.White else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditArchiveEntryScreenPreview() {
    PoopyrkaTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Edit Screen Preview")
        }
    }
}
