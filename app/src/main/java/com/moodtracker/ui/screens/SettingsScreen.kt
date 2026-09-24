package com.moodtracker.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moodtracker.ui.components.SectionHeader
import com.moodtracker.viewmodel.MoodViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 设置页 — 隐私锁、提醒、数据导出
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MoodViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var lockEnabled by remember { mutableStateOf(viewModel.preferences.isLockEnabled) }
    var biometricEnabled by remember { mutableStateOf(viewModel.preferences.useBiometric) }
    var reminderEnabled by remember { mutableStateOf(viewModel.preferences.isReminderEnabled) }
    var reminderTime by remember { mutableStateOf(viewModel.preferences.reminderTime) }

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showExportToast by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
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
            // ── 隐私与安全 ──
            SectionHeader("隐私与安全")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("应用锁", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "启动时要求密码/指纹解锁",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = lockEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // 开启锁: 需先设置密码
                                    showPasswordDialog = true
                                } else {
                                    lockEnabled = false
                                    biometricEnabled = false
                                    viewModel.setLockEnabled(false)
                                    viewModel.setBiometricEnabled(false)
                                }
                            }
                        )
                    }

                    if (lockEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text("指纹解锁", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "使用生物识别解锁",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = biometricEnabled,
                                onCheckedChange = { enabled ->
                                    biometricEnabled = enabled
                                    viewModel.setBiometricEnabled(enabled)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 提醒 ──
            SectionHeader("每日提醒")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("打卡提醒", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "每天提醒你记录心情",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                viewModel.setReminderEnabled(enabled)
                            }
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("提醒时间", style = MaterialTheme.typography.bodyLarge)
                            // 简易时间输入: 弹出选择
                            Text(
                                reminderTime,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                        // 快捷时间选择
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("08:00", "12:00", "18:00", "20:00", "22:00").forEach { time ->
                                androidx.compose.material3.FilterChip(
                                    selected = reminderTime == time,
                                    onClick = {
                                        reminderTime = time
                                        viewModel.setReminderTime(time)
                                    },
                                    label = { Text(time) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 数据 ──
            SectionHeader("数据备份")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "导出全部记录为 CSV 文件",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "保存到手机 Download 目录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    val csv = viewModel.exportToCsv()
                                    val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                        .format(Date())
                                    val fileName = "mood_export_$dateStr.csv"
                                    // 保存到公共 Download 目录
                                    val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(
                                        android.os.Environment.DIRECTORY_DOWNLOADS
                                    )
                                    val file = File(downloadDir, fileName)
                                    FileWriter(file).use { it.write(csv) }
                                    exportMessage = "已导出到: ${file.absolutePath}"
                                } catch (e: Exception) {
                                    exportMessage = "导出失败: ${e.message}"
                                }
                                showExportToast = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("导出 CSV")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── 关于 ──
            SectionHeader("关于")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "极简心情记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "版本 1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "所有数据保存在本机，不上传网络。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    // ── 设置密码弹窗 ──
    if (showPasswordDialog) {
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var error by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                lockEnabled = false
            },
            title = { Text("设置密码") },
            text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("密码 (4位数字)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("确认密码") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                    )
                    if (error.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(error, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when {
                            password.length < 4 -> error = "密码至少4位"
                            password != confirmPassword -> error = "两次密码不一致"
                            else -> {
                                viewModel.setLockPassword(password)
                                viewModel.setLockEnabled(true)
                                lockEnabled = true
                                showPasswordDialog = false
                            }
                        }
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        lockEnabled = false
                    }
                ) { Text("取消") }
            }
        )
    }

    // ── 导出结果提示 ──
    if (showExportToast) {
        androidx.compose.runtime.LaunchedEffect(showExportToast) {
            Toast.makeText(context, exportMessage, Toast.LENGTH_LONG).show()
            showExportToast = false
        }
    }
}
