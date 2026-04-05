package com.holidaymessenger.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val monthDay: String? = null,
    val isVariable: Boolean = false,
    val enabled: Boolean = true
)
