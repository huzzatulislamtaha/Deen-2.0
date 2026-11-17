package com.example.core.i18n

enum class AppLanguage(val code: String, val nativeLabel: String) {
    ENGLISH("en", "English"),
    BENGALI("bn", "বাংলা");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code == code } ?: ENGLISH
    }
}
