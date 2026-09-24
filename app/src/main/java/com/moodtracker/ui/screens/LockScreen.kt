package com.moodtracker.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.moodtracker.utils.PreferencesManager
import java.util.concurrent.Executors

/**
 * 锁屏页 — 数字密码 / 指纹生物识别解锁
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LockScreen(
    preferencesManager: PreferencesManager,
    onUnlock: () -> Unit
) {
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    val verifyAndUnlock: () -> Unit = {
        if (preferencesManager.lockPassword.isEmpty()) {
            onUnlock()
        } else if (preferencesManager.lockPassword == hashPassword(password)) {
            onUnlock()
        } else {
            error = true
        }
    }

    // 检查设备是否支持生物识别
    val canUseBiometric = remember {
        val bm = BiometricManager.from(context)
        bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
            BiometricManager.BIOMETRIC_SUCCESS
    }

    // 自动弹出指纹识别
    LaunchedEffect(canUseBiometric, preferencesManager.useBiometric) {
        if (canUseBiometric && preferencesManager.useBiometric) {
            showBiometricPrompt(context, onUnlock)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "请输入密码",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it.filter { c -> c.isDigit() }.take(4)
                error = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("密码") },
            singleLine = true,
            isError = error,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { verifyAndUnlock() })
        )

        if (error) {
            Spacer(Modifier.height(8.dp))
            Text(
                "密码错误",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = verifyAndUnlock,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("解锁")
        }

        // 指纹按钮
        if (canUseBiometric && preferencesManager.useBiometric) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showBiometricPrompt(context, onUnlock) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("指纹解锁")
            }
        }
    }
}

/**
 * 弹出系统生物识别提示
 */
private fun showBiometricPrompt(
    context: android.content.Context,
    onUnlock: () -> Unit
) {
    val executor = Executors.newSingleThreadExecutor()
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("解锁心情记录")
        .setSubtitle("使用指纹解锁应用")
        .setNegativeButtonText("取消")
        .build()

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            onUnlock()
        }
    }

    BiometricPrompt(
        context as androidx.fragment.app.FragmentActivity,
        executor,
        callback
    ).authenticate(promptInfo)
}

/** 密码哈希 (与 ViewModel 中一致) */
private fun hashPassword(password: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(password.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}
