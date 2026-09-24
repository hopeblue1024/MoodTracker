package com.moodtracker.data

import androidx.room.TypeConverter

/**
 * Room 类型转换器 — RecordType 与 String 互转
 */
class Converters {
    @TypeConverter
    fun fromRecordType(type: RecordType): String = type.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)
}
