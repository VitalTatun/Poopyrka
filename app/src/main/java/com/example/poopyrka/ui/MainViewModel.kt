package com.example.poopyrka.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poopyrka.data.AppDao
import com.example.poopyrka.data.EarningsCalculator
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.data.WorkShift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val dao: AppDao,
    private val calculator: EarningsCalculator
) : ViewModel() {

    // Main Screen State
    val uiState: StateFlow<MainUiState> = dao.getCurrentOpenShift()
        .flatMapLatest { shift ->
            if (shift == null) {
                flowOf(MainUiState(isLoading = false))
            } else {
                dao.getEntriesForShift(shift.id).combine(flowOf(shift)) { entries, currentShift ->
                    val totalLines = entries.sumOf { it.count }
                    MainUiState(
                        currentShift = currentShift,
                        entries = entries,
                        totalLines = totalLines,
                        totalEarnings = calculator.calculate(totalLines),
                        isLoading = false
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )

    // Statistics State
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val statsState: StateFlow<StatisticsUiState> = combine(
        dao.getClosedShifts(),
        dao.getAllEntries(),
        _selectedMonth
    ) { shifts, allEntries, month ->
        val filteredShifts = shifts.filter {
            val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            YearMonth.from(date) == month
        }

        val daySummaries = filteredShifts.map { shift ->
            val entries = allEntries.filter { it.shiftId == shift.id }
            val totalLines = entries.sumOf { it.count }
            DaySummary(
                shift = shift,
                totalLines = totalLines,
                earnings = calculator.calculate(totalLines),
                coeffLabel = calculator.getCoeffLabel(totalLines)
            )
        }

        StatisticsUiState(
            selectedMonth = month,
            daySummaries = daySummaries,
            monthlyTotalEarnings = daySummaries.sumOf { it.earnings },
            monthlyTotalLines = daySummaries.sumOf { it.totalLines },
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsUiState()
    )

    fun changeMonth(delta: Int) {
        _selectedMonth.value = _selectedMonth.value.plusMonths(delta.toLong())
    }

    fun getDayDetails(shiftId: Long): Flow<MainUiState> = flow {
        val shift = dao.getShiftById(shiftId)
        if (shift != null) {
            dao.getEntriesForShift(shiftId).collect { entries ->
                val totalLines = entries.sumOf { it.count }
                emit(MainUiState(
                    currentShift = shift,
                    entries = entries,
                    totalLines = totalLines,
                    totalEarnings = calculator.calculate(totalLines),
                    isLoading = false
                ))
            }
        }
    }

    fun startShift() {
        viewModelScope.launch {
            dao.upsertShift(WorkShift(date = System.currentTimeMillis()))
        }
    }

    fun closeShift() {
        val currentShift = uiState.value.currentShift ?: return
        viewModelScope.launch {
            dao.upsertShift(currentShift.copy(isClosed = true))
        }
    }

    fun addEntry(pointName: String, count: Int, deliveryGroup: Int) {
        val shiftId = uiState.value.currentShift?.id ?: return
        viewModelScope.launch {
            dao.upsertEntry(
                ShipmentEntry(
                    shiftId = shiftId,
                    pointName = pointName,
                    count = count,
                    deliveryGroup = deliveryGroup
                )
            )
        }
    }

    fun deleteEntry(entry: ShipmentEntry) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
        }
    }
}
