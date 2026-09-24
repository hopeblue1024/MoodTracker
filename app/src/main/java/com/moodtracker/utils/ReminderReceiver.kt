package com.moodtracker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.moodtracker.MoodTrackerApp

/**
 * 每日提醒接收器 — 触发通知并重新调度明天的闹钟
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // 显示提醒通知
        NotificationHelper.showReminderNotification(context)

        // 重新调度明天的闹钟
        val app = context.applicationContext as MoodTrackerApp
        if (app.preferences.isReminderEnabled) {
            NotificationHelper.scheduleReminder(context, app.preferences.reminderTime)
        }
    }
}
