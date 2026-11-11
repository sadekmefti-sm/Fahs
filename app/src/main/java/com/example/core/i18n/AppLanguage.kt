package com.example.core.i18n

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    AR("ar", "العربية", true),
    FR("fr", "Français", false),
    EN("en", "English", false);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: FR
        }
    }
}
