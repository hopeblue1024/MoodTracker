package com.moodtracker.utils

import android.content.Context

/**
 * 应用偏好设置管理 — 基于 SharedPreferences
 * 管理锁屏、生物识别、提醒等设置 (不上传网络，仅本地存储)
 */
class PreferencesManager(context: Context) {

    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)

    /** 应用锁是否开启 */
    var isLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_LOCK_ENABLED, value).apply()

    /** 锁屏密码 (SHA-256 哈希值，明文不存储) */
    var lockPassword: String
        get() = prefs.getString(KEY_LOCK_PASSWORD, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LOCK_PASSWORD, value).apply()

    /** 是否使用指纹解锁 */
    var useBiometric: Boolean
        get() = prefs.getBoolean(KEY_USE_BIOMETRIC, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_BIOMETRIC, value).apply()

    /** 每日提醒是否开启 */
    var isReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    /** 提醒时间 (格式 "HH:mm"，默认 20:00) */
    var reminderTime: String
        get() = prefs.getString(KEY_REMINDER_TIME, "20:00") ?: "20:00"
        set(value) = prefs.edit().putString(KEY_REMINDER_TIME, value).apply()

    companion object {
        private const val KEY_LOCK_ENABLED = "lock_enabled"
        private const val KEY_LOCK_PASSWORD = "lock_password"
        private const val KEY_USE_BIOMETRIC = "use_biometric"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_TIME = "reminder_time"
    }
}
