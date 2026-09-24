package com.moodtracker

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.moodtracker.data.MoodDatabase
import com.moodtracker.utils.NotificationHelper
import com.moodtracker.utils.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Application 入口 — 初始化数据库、偏好设置、通知渠道，
 * 并通过 ProcessLifecycleOwner 监听前后台切换以触发锁屏。
 */
class MoodTrackerApp : Application() {

    lateinit var database: MoodDatabase
        private set

    lateinit var preferences: PreferencesManager
        private set

    /** 锁屏状态 — true 时显示锁屏界面 */
    private val _lockState = MutableStateFlow(false)
    val lockState: StateFlow<Boolean> = _lockState

    override fun onCreate() {
        super.onCreate()

        database = MoodDatabase.getInstance(this)
        preferences = PreferencesManager(this)

        // 创建通知渠道 (用于每日提醒)
        NotificationHelper.createNotificationChannel(this)

        // 若提醒已开启，启动时重新注册闹钟 (进程被杀后恢复)
        if (preferences.isReminderEnabled) {
            NotificationHelper.scheduleReminder(this, preferences.reminderTime)
        }

        // 应用回到前台时，若锁屏已开启则显示锁屏
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart() {
                if (preferences.isLockEnabled) {
                    _lockState.value = true
                }
            }
        })
    }

    /** 解锁 — 验证通过后调用 */
    fun unlock() {
        _lockState.value = false
    }
}
