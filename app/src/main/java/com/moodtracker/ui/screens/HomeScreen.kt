package com.moodtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Mood
import com.moodtracker.data.MoodRecord
import com.moodtracker.ui.components.MoodRecordCard
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.viewmodel.MoodViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 首页 — 展示今日记录列表 + 本月情绪概览
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MoodViewModel,
    onAddRecord: () -> Unit,
    onEditRecord: (Long) -> Unit,
    onSettings: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val zoneId = ZoneId.systemDefault()
    val today = LocalDate.now()

    val todayRecords = remember(allRecords) {
        allRecords.filter {
            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate() == today
        }
    }
    val monthRecords = remember(allRecords) {
        allRecords.filter {
            val d = Instant.ofEpochMilli(it.timestamp).atZone(zoneId).toLocalDate()
            d.year == today.year && d.month == today.month
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("今日心情") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRecord) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = today.format(
                    DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            if (todayRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "今天还没有记录\n点击 + 添加",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                todayRecords.forEach { record ->
                    MoodRecordCard(
                        record = record,
                        onClick = { onEditRecord(record.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionHeader("本月概览")
            MonthlySummary(monthRecords)
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * 本月情绪概览卡片
 */
@Composable
private fun MonthlySummary(monthRecords: List<MoodRecord>) {
    if (monthRecords.isEmpty()) {
        Text(
            "本月暂无记录",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val avgScore = monthRecords.map { it.moodLevel }.average().toFloat()
    val avgMood = Mood.fromLevel(avgScore.toInt())
    val moodCounts = Mood.entries.map { mood ->
        mood to monthRecords.count { it.moodLevel == mood.level }
    }.filter { it.second > 0 }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(avgMood.emoji, fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "本月平均",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        avgMood.label,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "共 ${monthRecords.size} 条",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (moodCounts.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    moodCounts.forEach { (mood, count) ->
                        Box(
                            Modifier
                                .weight(count.toFloat())
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(mood.toColor())
                        )
                    }
                }
            }
        }
    }
}
