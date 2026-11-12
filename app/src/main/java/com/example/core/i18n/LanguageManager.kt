package com.example.core.i18n

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

val LocalAppLanguage = compositionLocalOf { AppLanguage.FR }

object LanguagePreferences {
    private const val PREFS_NAME = "fahs_prefs"
    private const val KEY_LANGUAGE = "selected_language"
    private const val KEY_ONBOARDING_DONE = "onboarding_completed"
    private const val KEY_DEVELOPER_MODE = "developer_mode_enabled"
    private const val KEY_LAST_DONGLE_ADDRESS = "last_successful_dongle_address"
    private const val KEY_LAST_DONGLE_NAME = "last_successful_dongle_name"
    private const val KEY_READ_ALOUD_ENABLED = "read_aloud_enabled"

    fun getInitialLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_LANGUAGE, null)
        if (saved != null) {
            return AppLanguage.fromCode(saved)
        }
        // Detect system locale default
        val systemLang = Locale.getDefault().language.lowercase()
        return when {
            systemLang.startsWith("ar") -> AppLanguage.AR
            systemLang.startsWith("fr") -> AppLanguage.FR
            systemLang.startsWith("en") -> AppLanguage.EN
            else -> AppLanguage.FR // Default to French as requested
        }
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingCompleted(context: Context, completed: Boolean = true) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }

    fun isDeveloperModeEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DEVELOPER_MODE, false)
    }

    fun setDeveloperModeEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DEVELOPER_MODE, enabled).apply()
    }

    fun getLastSuccessfulDongleAddress(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_DONGLE_ADDRESS, null)?.takeIf { it.isNotBlank() }
    }

    fun getLastSuccessfulDongleName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LAST_DONGLE_NAME, null)?.takeIf { it.isNotBlank() }
    }

    fun saveLastSuccessfulDongle(context: Context, address: String, name: String) {
        if (address.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LAST_DONGLE_ADDRESS, address.trim())
            .putString(KEY_LAST_DONGLE_NAME, name.trim())
            .apply()
    }

    fun clearLastSuccessfulDongle(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_LAST_DONGLE_ADDRESS)
            .remove(KEY_LAST_DONGLE_NAME)
            .apply()
    }

    fun isReadAloudEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_READ_ALOUD_ENABLED, false)
    }

    fun setReadAloudEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_READ_ALOUD_ENABLED, enabled).apply()
    }
}
