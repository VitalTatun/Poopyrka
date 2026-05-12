package com.example.poopyrka.ui

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.poopyrka.ui.theme.MainPurple
import com.example.poopyrka.ui.theme.BackgroundLight
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEntryScreen(
    onBack: () -> Unit,
    onSave: (String, Int, Int) -> Unit
) {
    var count by remember { mutableStateOf("") }
    var selectedPoint by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableIntStateOf(1) }
    
    val points = listOf("WB Палома", "WB ЕСЦ", "Ozon Палома", "Ozon ЕСЦ", "EMall", "FBO WB", "FBO Ozon")
    val focusRequester = remember { FocusRequester() }
    val today = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")) }
    
    val isFormValid = count.isNotBlank() && (count.toIntOrNull() ?: 0) > 0 && selectedPoint.isNotBlank()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Новая отгрузка", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isFormValid) MainPurple else Color.LightGray)
                            .then(
                                if (isFormValid) {
                                    Modifier.clickable {
                                        onSave(selectedPoint, count.toInt(), selectedGroup)
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
            // Поле Дата
            OutlinedTextField(
                value = today,
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
                singleLine = true
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
                            Text(
                                text = point,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Номер отгрузки
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Номер отгрузки", fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(1, 2, 3).forEach { group ->
                        val isSelected = selectedGroup == group
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MainPurple else Color.White)
                                .clickable { selectedGroup = group }
                                .then(if (isSelected) Modifier else Modifier.border(1.dp, Color.LightGray, CircleShape)),
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
fun AddEntryScreenPreview() {
    PoopyrkaTheme {
        AddEntryScreen(onBack = {}, onSave = { _, _, _ -> })
    }
}
