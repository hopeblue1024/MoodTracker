package com.moodtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Emotion
import com.moodtracker.data.MentalState
import com.moodtracker.data.MoodRecord
import com.moodtracker.data.RecordType
import com.moodtracker.ui.components.DistributionBarChart
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.ui.components.StatsSummaryCard
import com.moodtracker.ui.components.WeeklyBarChart
import com.moodtracker.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * 统计页 — 卡片式仪表盘
 * 情绪统计 / 状态统计 切换 → 摘要卡 → 分布占比 → 近7日走势
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MoodViewModel) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val emotionRecords by viewModel.emotionRecords.collectAsStateWithLifecycle()
    val stateRecords by viewModel.stateRecords.collectAsStateWithLifecycle()

    var showEmotion by remember { mutableStateOf(true) }

    val currentRecords = if (showEmotion) emotionRecords else stateRecords

    // 分布占比数据
    val distributionData = remember(currentRecords) {
        if (showEmotion) {
            Emotion.entries.mapNotNull { e ->
                val count = currentRecords.count { it.label == e.label }
                if (count > 0) Triple("${e.emoji} ${e.label}", count, count.toFloat() / currentRecords.size * 100f) else null
            }.sortedByDescending { it.second }
        } else {
            MentalState.entries.mapNotNull { s ->
                val count = currentRecords.count { it.label == s.label }
                if (count > 0) Triple("${s.emoji} ${s.label}", count, count.toFloat() / currentRecords.size * 100f) else null
            }.sortedByDescending { it.second }
        }
    }

    // 高频项
    val dominant = distributionData.firstOrNull()

    // 近7日走势
    val weeklyData = remember(currentRecords) {
        val cal = Calendar.getInstance()
        val df = SimpleDateFormat("EEE", Locale.CHINA)
        val today = Calendar.getInstance()
        val result = mutableListOf<Pair<String, Int>>()
        for (i in 6 downTo 0) {
            val day = today.clone() as Calendar
            day.add(Calendar.DAY_OF_MONTH, -i)
            val dayStart = day.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val dayEnd = day.clone() as Calendar
            dayEnd.add(Calendar.DAY_OF_MONTH, 1)
            val count = currentRecords.count { it.timestamp >= dayStart && it.timestamp < dayEnd.timeInMillis }
            result.add(df.format(Date(day.timeInMillis)) to count)
        }
        result
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情记录", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── 副标题 ──
            Text(
                "数据洞察 · 本地统计",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("身心统计分析", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(12.dp))

            // ── 切换标签 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showEmotion,
                    onClick = { showEmotion = true },
                    label = { Text("😊 情绪统计") }
                )
                FilterChip(
                    selected = !showEmotion,
                    onClick = { showEmotion = false },
                    label = { Text("💓 状态统计") }
                )
            }

            if (currentRecords.isEmpty()) {
                Spacer(Modifier.height(64.dp))
                Text(
                    "当前类型暂无记录",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // ── 摘要卡 ──
                Spacer(Modifier.height(8.dp))
                StatsSummaryCard(
                    title = if (showEmotion) "情绪手账小结" else "状态手账小结",
                    badgeText = "累计 ${currentRecords.size} 次",
                    dominantEmoji = dominant?.first?.split(" ")?.firstOrNull() ?: "📝",
                    dominantLabel = dominant?.first?.split(" ")?.drop(1)?.joinToString(" ") ?: "",
                    dominantPercent = dominant?.third?.toInt() ?: 0,
                    description = if (showEmotion)
                        "在记录期间，${dominant?.first?.split(" ")?.drop(1)?.joinToString(" ") ?: ""}是你最主要的情绪体验。"
                    else
                        "在记录期间，${dominant?.first?.split(" ")?.drop(1)?.joinToString(" ") ?: ""}是你最常见的身体/精神状态。"
                )

                // ── 分布占比 ──
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "分布占比与频次", trailing = {
                    Text("共 ${distributionData.size} 种", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                })
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        DistributionBarChart(data = distributionData)
                    }
                }

                // ── 近7日走势 ──
                Spacer(Modifier.height(16.dp))
                SectionHeader(title = "近7日记录走势", trailing = {
                    Text("按每日记录次数", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                })
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        WeeklyBarChart(data = weeklyData)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
