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
}
