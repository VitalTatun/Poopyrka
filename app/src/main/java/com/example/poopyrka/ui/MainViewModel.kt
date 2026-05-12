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
        dao.getFirstShiftDate(),
        _selectedMonth
    ) { shifts, allEntries, firstShiftDate, month ->
        val filteredShifts = shifts.filter {
            val date = Instant.ofEpochMilli(it.date).atZone(ZoneId.systemDefault()).toLocalDate()
            YearMonth.from(date) == month
        }

        val daySummaries = filteredShifts.map { shift ->
            DaySummary(
                shift = shift,
                totalLines = shift.totalLines,
                earnings = shift.totalEarnings,
                coeffLabel = calculator.getCoeffLabel(shift.totalLines)
            )
        }

        val firstMonth = if (firstShiftDate != null) {
            YearMonth.from(Instant.ofEpochMilli(firstShiftDate).atZone(ZoneId.systemDefault()).toLocalDate())
        } else {
            YearMonth.now()
        }
        val currentMonth = YearMonth.now()

        StatisticsUiState(
            selectedMonth = month,
            daySummaries = daySummaries,
            monthlyTotalEarnings = daySummaries.sumOf { it.earnings },
            monthlyTotalLines = daySummaries.sumOf { it.totalLines },
            isLoading = false,
            canGoBack = month.isAfter(firstMonth),
            canGoForward = month.isBefore(currentMonth)
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
                emit(MainUiState(
                    currentShift = shift,
                    entries = entries,
                    totalLines = shift.totalLines,
                    totalEarnings = shift.totalEarnings,
                    isLoading = false
                ))
            }
        }
    }

    fun getEntry(id: Long): Flow<Pair<ShipmentEntry, WorkShift>?> = flow {
        val entry = dao.getEntryById(id)
        if (entry != null) {
            val shift = dao.getShiftById(entry.shiftId)
            if (shift != null) {
                emit(entry to shift)
            } else {
                emit(null)
            }
        } else {
            emit(null)
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
            updateShiftTotals(currentShift.id)
            val updatedShift = dao.getShiftById(currentShift.id) ?: currentShift
            dao.upsertShift(updatedShift.copy(isClosed = true))
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
            updateShiftTotals(shiftId)
        }
    }

    fun updateEntry(entry: ShipmentEntry) {
        viewModelScope.launch {
            dao.upsertEntry(entry)
            updateShiftTotals(entry.shiftId)
        }
    }

    fun deleteEntry(entry: ShipmentEntry) {
        viewModelScope.launch {
            dao.deleteEntry(entry)
            updateShiftTotals(entry.shiftId)
        }
    }

    fun deleteEntryById(entryId: Long) {
        viewModelScope.launch {
            val entry = dao.getEntryById(entryId) ?: return@launch
            dao.deleteEntry(entry)
            updateShiftTotals(entry.shiftId)
        }
    }

    private suspend fun updateShiftTotals(shiftId: Long) {
        val shift = dao.getShiftById(shiftId) ?: return
        val entries = dao.getEntriesForShiftSync(shiftId)
        val totalLines = entries.sumOf { it.count }
        val totalEarnings = calculator.calculate(totalLines)
        
        dao.upsertShift(shift.copy(
            totalLines = totalLines,
            totalEarnings = totalEarnings
        ))
    }
}
