package com.moodtracker.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 心情记录实体
 * @param id 自增主键
 * @param moodLevel 心情等级 (0=很差 ~ 4=很好)
 * @param tags 精神状态标签列表 (经 TypeConverter 序列化为字符串存储)
 * @param note 文本备注 (可空)
 * @param timestamp 记录时间 (epoch 毫秒)
 */
@Entity(tableName = "mood_records", indices = [Index("timestamp")])
data class MoodRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moodLevel: Int,
    val tags: List<String>,
    val note: String,
    val timestamp: Long
) {
    /** 便捷属性: 获取对应 Mood 枚举 */
    val mood: Mood get() = Mood.fromLevel(moodLevel)
}
