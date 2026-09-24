package com.moodtracker.data

import androidx.compose.ui.graphics.Color

/**
 * 心情等级枚举 — 5 档，搭配 emoji 和颜色
 */
enum class Mood(
    val level: Int,
    val label: String,
    val emoji: String,
    val color: Long
) {
    VERY_BAD(0, "很差", "😞", 0xFFE57373),
    BAD(1, "不太好", "😕", 0xFFFFB74D),
    NORMAL(2, "普通", "😐", 0xFFFFF176),
    GOOD(3, "不错", "🙂", 0xFFAED581),
    VERY_GOOD(4, "很好", "😄", 0xFF81C784);

    /** 转换为 Compose Color */
    fun toColor(): Color = Color(color)

    companion object {
        fun fromLevel(level: Int): Mood = entries.find { it.level == level } ?: NORMAL

        /** 预设精神状态标签 */
        val DEFAULT_TAGS = listOf("焦虑", "疲惫", "失眠", "注意力差", "压力大", "亢奋", "麻木")
    }
}
