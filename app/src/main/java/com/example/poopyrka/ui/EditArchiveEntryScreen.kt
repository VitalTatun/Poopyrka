package com.example.poopyrka.ui

import android.widget.Toast
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.poopyrka.ui.theme.PoopyrkaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    
    val displayDate = remember(state) {
        state?.second?.let {
            val dt = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            dt.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        } ?: ""
    }
    
    val isFormValid = count.isNotBlank() && (count.toIntOrNull() ?: 0) > 0 && selectedPoint.isNotBlank()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state) {
        state?.first?.let {
            if (count.isEmpty()) count = it.count.toString()
            if (selectedPoint.isEmpty()) selectedPoint = it.pointName
            selectedGroup = it.deliveryGroup
        }
    }

    EditArchiveEntryContent(
        count = count,
        onCountChange = { if (it.all { char -> char.isDigit() }) count = it },
        selectedPoint = selectedPoint,
        onPointSelect = { selectedPoint = it },
        selectedGroup = selectedGroup,
        onGroupSelect = { selectedGroup = it },
        displayDate = displayDate,
        isFormValid = isFormValid,
        focusRequester = focusRequester,
        onBack = onBack,
        onDelete = {
            viewModel.deleteEntryById(entryId)
            Toast.makeText(context, "Запись удалена", Toast.LENGTH_SHORT).show()
            onBack()
        },
        onSave = {
            state?.first?.let {
                viewModel.updateEntry(it.copy(
                    pointName = selectedPoint,
                    count = count.toInt(),
                    deliveryGroup = selectedGroup
                ))
                Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                onBack()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditArchiveEntryContent(
    count: String,
    onCountChange: (String) -> Unit,
    selectedPoint: String,
    onPointSelect: (String) -> Unit,
    selectedGroup: Int,
    onGroupSelect: (Int) -> Unit,
    displayDate: String,
    isFormValid: Boolean,
    focusRequester: FocusRequester,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit
) {
    val points = listOf("WB Палома", "WB ЕСЦ", "Ozon Палома", "Ozon ЕСЦ", "EMall", "FBO WB", "FBO Ozon")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактирование", fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                    FilledIconButton(
                        onClick = onSave,
                        enabled = isFormValid,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Сохранить")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Поле Дата
            OutlinedTextField(
                value = displayDate,
                onValueChange = {},
                label = { Text("Дата") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Поле Количество
            OutlinedTextField(
                value = count,
                onValueChange = onCountChange,
                label = { Text("Количество строк") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Направления
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Направление",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    points.forEach { point ->
                        val isSelected = selectedPoint == point
                        FilterChip(
                            selected = isSelected,
                            onClick = { onPointSelect(point) },
                            label = { Text(point) },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outline,
                                selectedBorderColor = Color.Transparent
                            )
                        )
                    }
                }
            }

            // Номер отгрузки
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Номер отгрузки",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    listOf(1, 2, 3).forEach { group ->
                        val isSelected = selectedGroup == group
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { onGroupSelect(group) }
                                .then(
                                    if (isSelected) Modifier 
                                    else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = group.toString(),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                                        else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
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
        EditArchiveEntryContent(
            count = "42",
            onCountChange = {},
            selectedPoint = "WB Палома",
            onPointSelect = {},
            selectedGroup = 1,
            onGroupSelect = {},
            displayDate = "12 мая 2024",
            isFormValid = true,
            focusRequester = remember { FocusRequester() },
            onBack = {},
            onDelete = {},
            onSave = {}
        )
    }
}
