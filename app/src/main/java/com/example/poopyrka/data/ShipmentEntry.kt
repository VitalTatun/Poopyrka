package com.example.poopyrka.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shipment_entries",
    foreignKeys = [
        ForeignKey(
            entity = WorkShift::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["shiftId"])]
)
data class ShipmentEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shiftId: Long,
    val pointName: String,
    val count: Int,
    val deliveryGroup: Int
)
