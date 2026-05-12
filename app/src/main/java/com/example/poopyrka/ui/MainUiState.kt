package com.example.poopyrka.ui

import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.data.WorkShift

data class MainUiState(
    val currentShift: WorkShift? = null,
    val entries: List<ShipmentEntry> = emptyList(),
    val totalLines: Int = 0,
    val totalEarnings: Double = 0.0,
    val isLoading: Boolean = true
)
