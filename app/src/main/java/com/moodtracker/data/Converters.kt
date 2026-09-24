package com.moodtracker.data

import androidx.room.TypeConverter

/**
 * Room 类型转换器 — List<String> 与 String 互转
 * 使用 "|" 作为分隔符 (标签名中极不可能出现该字符)
 */
class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String = value.joinToString("|")

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return value.split("|")
    }
}
