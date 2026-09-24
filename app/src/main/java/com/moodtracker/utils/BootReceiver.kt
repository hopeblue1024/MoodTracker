package com.moodtracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moodtracker.MoodTrackerApp

/**
 * 开机自启接收器 — 设备重启后重新注册提醒闹钟
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val app = context.applicationContext as MoodTrackerApp
            if (app.preferences.isReminderEnabled) {
                NotificationHelper.scheduleReminder(context, app.preferences.reminderTime)
            }
        }
    }
}
