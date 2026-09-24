# 默认 ProGuard 规则 — 项目未开启混淆 (isMinifyEnabled = false)
# 如需开启混淆，取消 release buildType 中的 isMinifyEnabled = true

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Compose
-dontwarn androidx.compose.**
