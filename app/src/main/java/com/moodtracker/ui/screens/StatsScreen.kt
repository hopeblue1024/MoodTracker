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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Mood
import com.moodtracker.data.MoodRecord
import com.moodtracker.ui.components.MoodLineChart
import com.moodtracker.ui.components.MoodPieChart
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.viewmodel.MoodViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TreeMap

/**
 * 统计页 — 折线图 (情绪变化) + 饼图 (心情占比)，支持本周/本月切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MoodViewModel) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    var showWeek by remember { mutableStateOf(true) }
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now()

    // 根据选择的时间范围筛选记录
    val periodRecords = remember(allRecords, showWeek) {
        if (showWeek) {
            // 本周 (周一为起始)
            val monday = today.with(DayOfWeek.MONDAY)
            val nextMonday = monday.plusWeeks(1)
            allRecords.filter { record ->
                val d = Instant.ofEpochMilli(record.timestamp).atZone(zoneId).toLocalDate()
                d >= monday && d < nextMonday
            }
        } else {
            // 本月
            allRecords.filter { record ->
                val d = Instant.ofEpochMilli(record.timestamp).atZone(zoneId).toLocalDate()
                d.year == today.year && d.month == today.month
            }
        }
    }

    // 折线图数据: 日期 -> 平均心情分数
    val lineData = remember(periodRecords) {
        val byDate = TreeMap<LocalDate, MutableList<Int>>()
        periodRecords.forEach { record ->
            val d = Instant.ofEpochMilli(record.timestamp).atZone(zoneId).toLocalDate()
            byDate.getOrPut(d) { mutableListOf() }.add(record.moodLevel)
        }
        byDate.map { (date, scores) ->
            date to scores.average().toFloat()
        }
    }

    // 饼图数据: 心情 -> 数量
    val pieData = remember(periodRecords) {
        Mood.entries.map { mood ->
            mood to periodRecords.count { it.moodLevel == mood.level }.toFloat()
        }.filter { it.second > 0 }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("统计") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ── 时间范围切换 ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = showWeek,
                    onClick = { showWeek = true },
                    label = { Text("本周") }
                )
                FilterChip(
                    selected = !showWeek,
                    onClick = { showWeek = false },
                    label = { Text("本月") }
                )
            }

            if (periodRecords.isEmpty()) {
                Spacer(Modifier.height(64.dp))
                Text(
                    text = "当前时段暂无记录",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // ── 概览摘要 ──
                PeriodSummary(periodRecords)

                Spacer(Modifier.height(16.dp))

                // ── 折线图 ──
                SectionHeader("情绪变化")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(8.dp)) {
                        MoodLineChart(data = lineData)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── 饼图 ──
                SectionHeader("心情占比")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        MoodPieChart(data = pieData)
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/**
 * 时段概览摘要 — 记录条数、平均心情
 */
@Composable
private fun PeriodSummary(records: List<MoodRecord>) {
    val avgScore = records.map { it.moodLevel }.average().toFloat()
    val avgMood = Mood.fromLevel(avgScore.toInt())
    val recordDays = records.map {
        Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
    }.toSet().size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(avgMood.emoji, fontSize = 32.sp)
                Text(
                    avgMood.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "平均心情",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${records.size}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "记录条数",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$recordDays",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "记录天数",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}


