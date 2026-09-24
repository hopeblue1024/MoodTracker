package com.moodtracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.moodtracker.ui.navigation.AppNavigation
import com.moodtracker.ui.screens.LockScreen
import com.moodtracker.ui.theme.MoodTrackerTheme

/**
 * 应用唯一 Activity — 使用 FragmentActivity 以支持 BiometricPrompt 指纹验证
 */
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MoodTrackerApp
        setContent {
            MoodTrackerTheme {
                val lockState by app.lockState.collectAsStateWithLifecycle()
                if (lockState) {
                    LockScreen(
                        preferencesManager = app.preferences,
                        onUnlock = { app.unlock() }
                    )
                } else {
                    AppNavigation()
                }
            }
        }
    }
}
