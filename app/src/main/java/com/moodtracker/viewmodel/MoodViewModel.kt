package com.moodtracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.moodtracker.data.Emotion
import com.moodtracker.data.MentalState
import com.moodtracker.data.MoodDao
import com.moodtracker.data.MoodDatabase
import com.moodtracker.data.MoodRecord
import com.moodtracker.data.RecordType
import com.moodtracker.utils.PreferencesManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodViewModel(application: Application) : AndroidViewModel(application) {

    private val dao: MoodDao = MoodDatabase.getInstance(application).moodDao()
    val preferences = PreferencesManager(application)

    val allRecords: StateFlow<List<MoodRecord>> = dao.getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emotionRecords: StateFlow<List<MoodRecord>> = dao.getRecordsByType(RecordType.EMOTION.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stateRecords: StateFlow<List<MoodRecord>> = dao.getRecordsByType(RecordType.STATE.name)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertEmotion(emotion: Emotion, note: String = "") {
        viewModelScope.launch {
            dao.insertRecord(
                MoodRecord(
                    type = RecordType.EMOTION.name,
                    label = emotion.label,
                    description = emotion.label,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun insertState(state: MentalState, note: String = "") {
        viewModelScope.launch {
            dao.insertRecord(
                MoodRecord(
                    type = RecordType.STATE.name,
                    label = state.label,
                    description = state.label,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteRecord(record: MoodRecord) {
        viewModelScope.launch { dao.deleteRecord(record) }
    }

    fun clearAllRecords() {
        viewModelScope.launch { dao.deleteAllRecords() }
    }

    fun getRecordById(id: Long, onResult: (MoodRecord?) -> Unit) {
        viewModelScope.launch { onResult(dao.getRecordById(id)) }
    }

    suspend fun exportToCsv(): String {
        val records = dao.getAllRecordsList()
        val sb = StringBuilder()
        sb.append("ID,类型,标签,描述,备注,时间\n")
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        records.forEach { r ->
            sb.append("${r.id},${r.type},${r.label},${r.description},${r.note},${df.format(Date(r.timestamp))}\n")
        }
        return sb.toString()
    }

    fun setLockEnabled(enabled: Boolean) { preferences.isLockEnabled = enabled }
    fun setLockPassword(password: String) {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
        preferences.lockPassword = bytes.joinToString("") { "%02x".format(it) }
    }
    fun setBiometricEnabled(enabled: Boolean) { preferences.useBiometric = enabled }
    fun setReminderEnabled(enabled: Boolean) { preferences.isReminderEnabled = enabled }
    fun setReminderTime(time: String) { preferences.reminderTime = time }

    companion object {
        fun factory(app: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return MoodViewModel(app) as T
                }
            }
        }
    }
}
