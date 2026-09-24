package com.moodtracker.data

import androidx.compose.ui.graphics.Color

/**
 * 情绪状态 — 5 种心情，emoji + 颜色
 */
enum class Emotion(
    val label: String,
    val emoji: String,
    val color: Long
) {
    HAPPY("轻松愉悦", "😊", 0xFF81C784),
    CALM("平和安稳", "☕", 0xFFAED581),
    NEUTRAL("平淡无感", "😐", 0xFFB0BEC5),
    IRRITATED("轻微烦躁", "😤", 0xFFFFB74D),
    SAD("心情低落", "🌧️", 0xFF90A4AE);

    fun toColor(): Color = Color(color)
}

/**
 * 精神状态 — 5 种精力状态，emoji + 颜色
 */
enum class MentalState(
    val label: String,
    val emoji: String,
    val color: Long
) {
    ENERGETIC("精力充沛", "⚡", 0xFF66BB6A),
    NORMAL("状态一般", "🙂", 0xFFAED581),
    SLIGHTLY_TIRED("轻微乏力", "😮‍💨", 0xFFFFB74D),
    DIZZY("头脑昏沉", "🌀", 0xFFA1887F),
    EXHAUSTED("十分疲惫", "🔋", 0xFFB71C1C);

    fun toColor(): Color = Color(color)
}

/**
 * 记录类型
 */
enum class RecordType {
    EMOTION, STATE
}
