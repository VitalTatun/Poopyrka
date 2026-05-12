package com.example.poopyrka.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("SELECT * FROM work_shifts WHERE isClosed = 0 LIMIT 1")
    fun getCurrentOpenShift(): Flow<WorkShift?>

    @Query("SELECT * FROM shipment_entries WHERE shiftId = :shiftId")
    fun getEntriesForShift(shiftId: Long): Flow<List<ShipmentEntry>>

    @Query("SELECT * FROM shipment_entries WHERE shiftId = :shiftId")
    suspend fun getEntriesForShiftSync(shiftId: Long): List<ShipmentEntry>

    @Query("SELECT * FROM shipment_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): ShipmentEntry?

    @Query("SELECT * FROM work_shifts WHERE isClosed = 1 ORDER BY date DESC")
    fun getClosedShifts(): Flow<List<WorkShift>>

    @Query("SELECT * FROM work_shifts WHERE id = :id")
    suspend fun getShiftById(id: Long): WorkShift?

    @Query("SELECT * FROM shipment_entries")
    fun getAllEntries(): Flow<List<ShipmentEntry>>

    @Upsert
    suspend fun upsertShift(shift: WorkShift): Long

    @Upsert
    suspend fun upsertEntry(entry: ShipmentEntry)

    @Delete
    suspend fun deleteEntry(entry: ShipmentEntry)
}
