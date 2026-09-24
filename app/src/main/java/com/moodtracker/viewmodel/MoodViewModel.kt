package com.moodtracker.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodtracker.data.CustomTag
import com.moodtracker.data.Mood
import com.moodtracker.data.MoodDatabase
import com.moodtracker.data.MoodRecord
import com.moodtracker.utils.NotificationHelper
import com.moodtracker.utils.PreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * 全局 ViewModel — 管理所有心情记录、标签、偏好设置的数据与操作
 */
class MoodViewModel(
    private val database: MoodDatabase,
    val preferences: PreferencesManager,
    private val context: Context
) : ViewModel() {

    private val dao = database.moodDao()

    /** 所有心情记录 (响应式) */
    val allRecords: StateFlow<List<MoodRecord>> = dao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 所有自定义标签 (响应式) */
    val allTags: StateFlow<List<CustomTag>> = dao.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ════════ 查询 ════════

    /** 获取指定月份的记录流 */
    fun getRecordsForMonth(year: Int, month: Int): Flow<List<MoodRecord>> {
        val firstDay = LocalDate.of(year, month, 1)
        val start = firstDay.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = firstDay.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getRecordsBetween(start, end)
    }

    /** 获取指定日期的全部记录 */
    suspend fun getRecordsForDate(date: LocalDate): List<MoodRecord> {
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getRecordsBetweenList(start, end)
    }

    /** 按 ID 获取单条记录 */
    suspend fun getRecordById(id: Long): MoodRecord? = dao.getRecordById(id)

    // ════════ 记录操作 ════════

    fun insertRecord(mood: Mood, tags: List<String>, note: String) {
        viewModelScope.launch {
            dao.insertRecord(
                MoodRecord(
                    moodLevel = mood.level,
                    tags = tags,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateRecord(record: MoodRecord) {
        viewModelScope.launch { dao.updateRecord(record) }
    }

    fun deleteRecord(record: MoodRecord) {
        viewModelScope.launch { dao.deleteRecord(record) }
    }

    // ════════ 标签操作 ════════

    fun addCustomTag(name: String) {
        viewModelScope.launch { dao.insertTag(CustomTag(name = name)) }
    }

    fun deleteCustomTag(tag: CustomTag) {
        viewModelScope.launch { dao.deleteTag(tag) }
    }

    // ════════ CSV 导出 ════════

    suspend fun exportToCsv(): String {
        val records = dao.getAllRecordsList().reversed()
        val sb = StringBuilder()
        sb.append("ID,日期,时间,心情,精神状态标签,备注\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        for (record in records) {
            val date = dateFormat.format(Date(record.timestamp))
            val time = timeFormat.format(Date(record.timestamp))
            sb.append(record.id).append(",")
            sb.append(date).append(",")
            sb.append(time).append(",")
            sb.append(record.mood.label).append(",")
            sb.append(escapeCsv(record.tags.joinToString(","))).append(",")
            sb.append(escapeCsv(record.note)).append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(field: String): String {
        return if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            "\"${field.replace("\"", "\"\"")}\""
        } else {
            field
        }
    }

    // ════════ 偏好设置 ════════

    fun setLockEnabled(enabled: Boolean) {
        preferences.isLockEnabled = enabled
        if (!enabled) preferences.useBiometric = false
    }

    fun setLockPassword(password: String) {
        preferences.lockPassword = hashPassword(password)
    }

    fun hasLockPassword(): Boolean = preferences.lockPassword.isNotEmpty()

    fun verifyPassword(password: String): Boolean {
        return hashPassword(password) == preferences.lockPassword
    }

    fun setBiometricEnabled(enabled: Boolean) {
        preferences.useBiometric = enabled
    }

    fun setReminderEnabled(enabled: Boolean) {
        preferences.isReminderEnabled = enabled
        if (enabled) {
            NotificationHelper.scheduleReminder(context, preferences.reminderTime)
        } else {
            NotificationHelper.cancelReminder(context)
        }
    }

    fun setReminderTime(time: String) {
        preferences.reminderTime = time
        if (preferences.isReminderEnabled) {
            NotificationHelper.scheduleReminder(context, time)
        }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * ViewModel 工厂 — 注入数据库、偏好设置和上下文
 */
class MoodViewModelFactory(
    private val database: MoodDatabase,
    private val preferences: PreferencesManager,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(database, preferences, context.applicationContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
