package com.moodtracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Mood
import com.moodtracker.data.MoodRecord
import com.moodtracker.ui.components.MoodButton
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.ui.components.TagChip
import com.moodtracker.viewmodel.MoodViewModel

/**
 * 新增 / 编辑记录页 — 核心录入界面
 * 心情选择 (5档) + 精神状态标签 (多选) + 文本备注
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecordScreen(
    viewModel: MoodViewModel,
    recordId: Long,
    onDone: () -> Unit
) {
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var noteText by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<MoodRecord?>(null) }

    // 若 recordId > 0 表示编辑模式，加载已有记录
    LaunchedEffect(recordId) {
        if (recordId > 0) {
            val record = viewModel.getRecordById(recordId)
            if (record != null) {
                editingRecord = record
                selectedMood = record.mood
                selectedTags = record.tags.toSet()
                noteText = record.note
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (recordId > 0) "编辑记录" else "新增记录") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (editingRecord != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
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
                .imePadding()
        ) {
            // ── 心情选择 ──
            SectionHeader("心情")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Mood.entries.forEach { mood ->
                    MoodButton(
                        mood = mood,
                        isSelected = selectedMood == mood,
                        onClick = { selectedMood = mood }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 精神状态标签 ──
            SectionHeader("精神状态")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 预设标签
                Mood.DEFAULT_TAGS.forEach { tag ->
                    TagChip(
                        label = tag,
                        isSelected = selectedTags.contains(tag),
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag)) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        }
                    )
                }
                // 用户自定义标签
                allTags.forEach { tag ->
                    TagChip(
                        label = tag.name,
                        isSelected = selectedTags.contains(tag.name),
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag.name)) {
                                selectedTags - tag.name
                            } else {
                                selectedTags + tag.name
                            }
                        }
                    )
                }
            }

            // 添加自定义标签
            var newTagText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                OutlinedTextField(
                    value = newTagText,
                    onValueChange = { newTagText = it },
                    label = { Text("新增标签") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val name = newTagText.trim()
                        if (name.isNotEmpty()) {
                            viewModel.addCustomTag(name)
                            selectedTags = selectedTags + name
                            newTagText = ""
                        }
                    },
                    enabled = newTagText.trim().isNotEmpty()
                ) {
                    Text("添加")
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── 备注 ──
            SectionHeader("备注 (可选)")
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("写点什么...") },
                minLines = 2,
                maxLines = 4
            )

            Spacer(Modifier.height(24.dp))

            // ── 保存按钮 ──
            Button(
                onClick = {
                    val mood = selectedMood ?: return@Button
                    if (editingRecord != null) {
                        // 编辑模式: 更新现有记录
                        viewModel.updateRecord(
                            editingRecord!!.copy(
                                moodLevel = mood.level,
                                tags = selectedTags.toList(),
                                note = noteText.trim()
                            )
                        )
                    } else {
                        // 新增模式
                        viewModel.insertRecord(
                            mood = mood,
                            tags = selectedTags.toList(),
                            note = noteText.trim()
                        )
                    }
                    onDone()
                },
                enabled = selectedMood != null,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (recordId > 0) "保存修改" else "保存记录")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // ── 删除确认弹窗 ──
    if (showDeleteDialog && editingRecord != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除记录") },
            text = { Text("确定删除这条记录吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(editingRecord!!)
                        showDeleteDialog = false
                        onDone()
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}


