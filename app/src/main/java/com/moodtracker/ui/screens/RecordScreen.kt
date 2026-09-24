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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.RecordType
import com.moodtracker.ui.components.HistoryRecordCard
import com.moodtracker.viewmodel.MoodViewModel

/**
 * 历史页 — 带筛选的记录列表
 * 筛选: 全部 / 仅情绪 / 仅状态
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordScreen(
    viewModel: MoodViewModel
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val emotionRecords by viewModel.emotionRecords.collectAsStateWithLifecycle()
    val stateRecords by viewModel.stateRecords.collectAsStateWithLifecycle()

    var filter by remember { mutableStateOf(0) } // 0=全部, 1=情绪, 2=状态
    var showClearDialog by remember { mutableStateOf(false) }

    val displayRecords = when (filter) {
        1 -> emotionRecords
        2 -> stateRecords
        else -> allRecords
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情记录", color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清空", tint = Color.White)
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
            // ── 副标题 ──
            Text(
                "时间倒序 · 本地轨迹",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("历史记录", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                TextButton(onClick = { showClearDialog = true }) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── 筛选标签 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == 0,
                    onClick = { filter = 0 },
                    label = { Text("全部(${allRecords.size})") }
                )
                FilterChip(
                    selected = filter == 1,
                    onClick = { filter = 1 },
                    label = { Text("仅情绪") }
                )
                FilterChip(
                    selected = filter == 2,
                    onClick = { filter = 2 },
                    label = { Text("仅状态") }
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── 记录列表 ──
            if (displayRecords.isEmpty()) {
                Spacer(Modifier.height(64.dp))
                Text(
                    "暂无记录",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                displayRecords.forEach { record ->
                    HistoryRecordCard(
                        record = record,
                        onDelete = { viewModel.deleteRecord(record) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── 清空确认弹窗 ──
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清空全部记录") },
            text = { Text("确定要删除所有 ${allRecords.size} 条记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllRecords()
                        showClearDialog = false
                    }
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}
