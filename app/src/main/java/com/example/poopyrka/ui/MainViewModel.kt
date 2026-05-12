package com.example.poopyrka.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.poopyrka.data.AppDao
import com.example.poopyrka.data.EarningsCalculator
import com.example.poopyrka.data.ShipmentEntry
import com.example.poopyrka.data.WorkShift
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val dao: AppDao,
    private val calculator: EarningsCalculator
) : ViewModel() {

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
