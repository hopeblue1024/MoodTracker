package com.moodtracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moodtracker.data.Mood
import com.moodtracker.ui.theme.Primary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 情绪变化折线图 — 展示每日心情分数变化
 * @param data 日期 -> 心情分数 (0~4)
 */
@Composable
fun MoodLineChart(
    data: List<Pair<LocalDate, Float>>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        val paddingLeft = 44.dp.toPx()
        val paddingRight = 12.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val paddingBottom = 24.dp.toPx()

        val plotW = size.width - paddingLeft - paddingRight
        val plotH = size.height - paddingTop - paddingBottom

        // ── 网格线 + Y 轴标签 ──
        val moodLabels = listOf("很差", "不太好", "普通", "不错", "很好")
        val gridColor = Color(0xFFE0E0E0)
        for (i in 0..4) {
            val y = paddingTop + plotH - (i / 4f) * plotH
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1f
            )
            val tp = android.graphics.Paint().apply {
                color = Color.Gray.toArgb()
                textSize = 9.sp.toPx()
                isAntiAlias = true
                textAlign = android.graphics.Paint.Align.RIGHT
            }
            drawContext.canvas.nativeCanvas.drawText(
                moodLabels[i],
                paddingLeft - 4.dp.toPx(),
                y + 3.dp.toPx(),
                tp
            )
        }

        // ── 折线 ──
        if (data.size >= 2) {
            val stepX = plotW / (data.size - 1)
            val path = Path()
            data.forEachIndexed { i, (_, score) ->
                val x = paddingLeft + i * stepX
                val y = paddingTop + plotH - (score / 4f) * plotH
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color = Primary, style = Stroke(width = 3f))
        }

        // ── 数据点 ──
        val stepX = if (data.size > 1) plotW / (data.size - 1) else 0f
        data.forEachIndexed { i, (_, score) ->
            val x = paddingLeft + i * stepX
            val y = paddingTop + plotH - (score / 4f) * plotH
            drawCircle(color = Primary, radius = 5f, center = Offset(x, y))
        }

        // ── X 轴标签 ──
        val labelInterval = if (data.size > 7) data.size / 7 else 1
        val dp = android.graphics.Paint().apply {
            color = Color.Gray.toArgb()
            textSize = 9.sp.toPx()
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }
        data.forEachIndexed { i, (date, _) ->
            if (i % labelInterval == 0 || i == data.size - 1) {
                val x = paddingLeft + i * stepX
                drawContext.canvas.nativeCanvas.drawText(
                    date.format(DateTimeFormatter.ofPattern("M/d")),
                    x,
                    size.height - 4.dp.toPx(),
                    dp
                )
            }
        }
    }
}

/**
 * 心情占比饼图 — 展示各心情等级的分布
 * @param data 心情 -> 数量
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodPieChart(
    data: List<Pair<Mood, Float>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.second.toDouble() }.toFloat()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            if (total <= 0f) return@Canvas

            val diameter = minOf(size.width, size.height) * 0.85f
            val tl = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)

            var startAngle = -90f
            data.forEach { (mood, value) ->
                val sweep = (value / total) * 360f
                drawArc(
                    color = mood.toColor(),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = tl,
                    size = Size(diameter, diameter)
                )
                startAngle += sweep
            }
        }

        Spacer(Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            data.forEach { (mood, value) ->
                val pct = if (total > 0) (value / total * 100).toInt() else 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(mood.toColor())
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${mood.emoji} ${mood.label} $pct%",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
