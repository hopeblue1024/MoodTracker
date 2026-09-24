package com.moodtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Emotion
import com.moodtracker.data.MentalState
import com.moodtracker.data.MoodRecord
import com.moodtracker.data.RecordType
import com.moodtracker.ui.components.GridItemCard
import com.moodtracker.ui.components.PillTag
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.viewmodel.MoodViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 记录页 — 网格式选择仪表盘
 * 上方摘要卡 → 当前情绪 3x3 网格 → 身体/精神状态 3x3 网格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MoodViewModel,
    onSettings: () -> Unit
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()

    val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    val todayRecords = allRecords.filter {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.timestamp)) == today
    }
    val lastRecord = allRecords.firstOrNull()
    val dateText = SimpleDateFormat("M月d日 EEEE", Locale.CHINA).format(Date())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情记录", color = Color(0xFF1A1A1A)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5F0)
                ),
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color(0xFF1A1A1A))
                    }
                }
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
            // ── 摘要卡 ──
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(dateText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("心情记录 · 本地手账", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                        PillTag(text = "今日已记 ${todayRecords.size}次")
                    }

                    if (lastRecord != null) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🕐", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text("上次打卡：", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                lastRecord.label,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(lastRecord.timestamp)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("本地已存", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 当前情绪 ──
            SectionHeader(title = "当前情绪", subtitle = "(点击立即记录)", accentColor = MaterialTheme.colorScheme.primary)

            // 3 列网格
            val emotions = Emotion.entries
            val rows = (emotions.size + 2) / 3
            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        if (index < emotions.size) {
                            val emotion = emotions[index]
                            Box(modifier = Modifier.weight(1f)) {
                                GridItemCard(
                                    emoji = emotion.emoji,
                                    label = emotion.label,
                                    accentColor = emotion.toColor(),
                                    onClick = { viewModel.insertEmotion(emotion) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // ── 身体/精神状态 ──
            SectionHeader(title = "身体/精神状态", subtitle = "(点击立即记录)", accentColor = MaterialTheme.colorScheme.secondary)

            val states = MentalState.entries
            val stateRows = (states.size + 2) / 3
            for (row in 0 until stateRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (col in 0 until 3) {
                        val index = row * 3 + col
                        if (index < states.size) {
                            val state = states[index]
                            Box(modifier = Modifier.weight(1f)) {
                                GridItemCard(
                                    emoji = state.emoji,
                                    label = state.label,
                                    accentColor = state.toColor(),
                                    onClick = { viewModel.insertState(state) }
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
