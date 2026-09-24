package com.moodtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 自定义精神状态标签
 */
@Entity(tableName = "custom_tags")
data class CustomTag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
