package com.moodtracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.data.Mood
import com.moodtracker.data.MoodRecord
import com.moodtracker.ui.components.MoodRecordCard
import com.moodtracker.viewmodel.MoodViewModel
import kotlinx.coroutines.flow.collectLatest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.SortedMap

/**
 * 日历页 — 月视图日历，色块代表当日主要心情，点击日期查看当日记录
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: MoodViewModel,
    onEditRecord: (Long) -> Unit
) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var dayRecords by remember { mutableStateOf<List<MoodRecord>>(emptyList()) }

    // 监听当月记录变化
    val monthRecords by remember(currentMonth) {
        viewModel.getRecordsForMonth(currentMonth.year, currentMonth.monthValue)
    }.collectAsStateWithLifecycle(emptyList())

    val zoneId = ZoneId.systemDefault()

    // 按日期分组: 记算每天的主要心情 (取平均)
    val moodByDay: SortedMap<LocalDate, Mood> = remember(monthRecords) {
        val map = sortedMapOf<LocalDate, Mood>()
        monthRecords.groupBy { record ->
            java.time.Instant.ofEpochMilli(record.timestamp).atZone(zoneId).toLocalDate()
        }.forEach { (date, records) ->
            val avgLevel = records.map { it.moodLevel }.average().toInt()
            map[date] = Mood.fromLevel(avgLevel)
        }
        map
    }

    // 点击日期后加载当日记录
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            dayRecords = viewModel.getRecordsForDate(date)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日历") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // ── 月份导航 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "上月")
                }
                Text(
                    text = "${currentMonth.year}年${currentMonth.monthValue}月",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "下月")
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── 星期标题 ──
            Row(Modifier.fillMaxWidth()) {
                val weekDays = listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                )
                weekDays.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.CHINA),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── 日历网格 ──
            CalendarGrid(
                yearMonth = currentMonth,
                moodByDay = moodByDay,
                onDateClick = { date -> selectedDate = date }
            )

            Spacer(Modifier.height(16.dp))

            // ── 图例 ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Mood.entries.forEach { mood ->
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(mood.toColor())
                    )
                    Text(
                        mood.label,
                        modifier = Modifier.padding(start = 2.dp, end = 8.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }

    // ── 日期详情弹窗 ──
    if (selectedDate != null) {
        AlertDialog(
            onDismissRequest = { selectedDate = null },
            title = {
                Text(selectedDate!!.format(
                    DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINA)
                ))
            },
            text = {
                if (dayRecords.isEmpty()) {
                    Text("当天无记录")
                } else {
                    Column {
                        dayRecords.forEach { record ->
                            MoodRecordCard(
                                record = record,
                                onClick = {
                                    selectedDate = null
                                    onEditRecord(record.id)
                                }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { selectedDate = null }) {
                    Text("关闭")
                }
            }
        )
    }
}

/**
 * 日历网格 — 7 列 x N 行，第一列从周一开始
 */
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    moodByDay: Map<LocalDate, Mood>,
    onDateClick: (LocalDate) -> Unit
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // 周一 = 0, 周日 = 6
    val startOffset = (firstDay.dayOfWeek.value - 1)

    val today = LocalDate.now()

    val rows = ((startOffset + daysInMonth + 6) / 7)

    Column {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col - startOffset + 1
                    if (dayIndex in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayIndex)
                        val mood = moodByDay[date]
                        val isToday = date == today

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .padding(2.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(mood?.toColor()?.copy(alpha = 0.6f) ?: Color.Transparent)
                                .clickable { onDateClick(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayIndex.toString(),
                                fontSize = 14.sp,
                                color = when {
                                    isToday -> MaterialTheme.colorScheme.primary
                                    mood != null -> Color.Black.copy(alpha = 0.8f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
