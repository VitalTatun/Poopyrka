package com.example.poopyrka.ui

import com.example.poopyrka.data.WorkShift
import java.time.YearMonth

data class StatisticsUiState(
    val selectedMonth: YearMonth = YearMonth.now(),
    val daySummaries: List<DaySummary> = emptyList(),
    val monthlyTotalEarnings: Double = 0.0,
    val monthlyTotalLines: Int = 0,
    val isLoading: Boolean = true
)

data class DaySummary(
    val shift: WorkShift,
    val totalLines: Int,
    val earnings: Double,
    val coeffLabel: String
)
