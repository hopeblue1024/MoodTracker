package com.moodtracker.data

import androidx.compose.ui.graphics.Color

/**
 * 情绪类型 — 9 种情绪，搭配 emoji 和颜色
 */
enum class Emotion(
    val label: String,
    val emoji: String,
    val description: String,
    val color: Long
) {
    HAPPY("开心", "😊", "心花怒放，神采飞扬", 0xFF81C784),
    CALM("平静", "☕", "心平气和，恬淡从容", 0xFFAED581),
    JOYFUL("欣喜", "✨", "小确幸降临，心怀喜悦", 0xFFDCE775),
    RELAXED("放松", "☀️", "松弛舒适，从容不迫", 0xFFAED581),
    ANXIOUS("焦虑", "⚠️", "思绪翻涌，坐立不安", 0xFFFFB74D),
    TIRED("疲惫", "🔋", "身心透支，需要休息", 0xFFB0BEC5),
    SAD("难过", "🌧️", "郁郁寡欢，情绪低落", 0xFF90A4AE),
    ANGRY("生气", "🔥", "胸口憋闷，火冒三丈", 0xFFE57373),
    DOWN("低落", "😞", "提不起劲，沉默寡言", 0xFF9E9E9E);

    fun toColor(): Color = Color(color)
}

/**
 * 身体/精神状态 — 8 种状态
 */
enum class MentalState(
    val label: String,
    val emoji: String,
    val description: String,
    val color: Long
) {
    ENERGETIC("精力充沛", "⚡", "体力充沛，行动力强", 0xFF66BB6A),
    ALERT("精神饱满", "📈", "头脑清醒，反应敏捷", 0xFF81C784),
    SLEEPY("困倦", "🌙", "眼皮打架，哈欠连天", 0xFFB0BEC5),
    FATIGUED("疲劳", "🕐", "肢体酸痛，略显疲态", 0xFF90A4AE),
    TENSE("紧张", "🛡️", "肌肉紧绷，呼吸急促", 0xFFFFB74D),
    DIZZY("头晕昏沉", "🎯", "思维迟滞，大脑模糊", 0xFFA1887F),
    RELAXED("轻松自如", "🍃", "呼吸均匀，步伐轻盈", 0xFFAED581),
    EXHAUSTED("虚脱乏力", "🔋", "能量耗尽，只想躺平", 0xFFB71C1C);

    fun toColor(): Color = Color(color)
}

/**
 * 记录类型
 */
enum class RecordType {
    EMOTION, STATE
}
