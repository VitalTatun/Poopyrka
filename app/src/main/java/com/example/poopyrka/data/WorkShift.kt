package com.example.poopyrka.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_shifts")
data class WorkShift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val isClosed: Boolean = false
)
