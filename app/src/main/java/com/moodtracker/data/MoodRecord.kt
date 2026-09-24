package com.moodtracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 心情/状态记录实体
 * @param id 自增主键
 * @param type 记录类型 (EMOTION=情绪, STATE=状态)
 * @param label 标签名 (如 "开心", "精力充沛")
 * @param description 描述 (如 "心花怒放，神采飞扬")
 * @param note 用户备注 (可空)
 * @param timestamp 记录时间 (epoch 毫秒)
 */
@Entity(tableName = "mood_records", indices = [Index("timestamp"), Index("type")])
data class MoodRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val label: String,
    val description: String,
    val note: String,
    val timestamp: Long
) {
    val recordType: RecordType get() = RecordType.valueOf(type)
    val emoji: String get() = when (recordType) {
        RecordType.EMOTION -> Emotion.entries.find { it.label == label }?.emoji ?: "📝"
        RecordType.STATE -> MentalState.entries.find { it.label == label }?.emoji ?: "📝"
    }
    val accentColor: Long get() = when (recordType) {
        RecordType.EMOTION -> Emotion.entries.find { it.label == label }?.color ?: 0xFFAED581
        RecordType.STATE -> MentalState.entries.find { it.label == label }?.color ?: 0xFFB0BEC5
    }
}
