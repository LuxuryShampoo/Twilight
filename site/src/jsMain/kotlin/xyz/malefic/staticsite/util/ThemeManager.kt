package xyz.malefic.staticsite.util

import kotlinx.browser.localStorage

object ThemeManager {
    private const val THEME_STORAGE_KEY = "calendar_theme_mode"

    // Track if dark mode is enabled
    private var _isDarkMode = localStorage.getItem(THEME_STORAGE_KEY) == "dark"
    val isDarkMode: Boolean get() = _isDarkMode

    // Define color sets
    object Colors {
        val background get() = if (isDarkMode) "#121212" else "#F5F5F5"
        val calendarBackground get() = if (isDarkMode) "#1E1E1E" else "#FFFFFF"
        val headerBackground get() = if (isDarkMode) "#2C2C2C" else "#F0F0F0"
        val text get() = if (isDarkMode) "#E0E0E0" else "#333333"
        val secondaryText get() = if (isDarkMode) "#A0A0A0" else "#666666"
        val buttonBackground get() = if (isDarkMode) "#3D3D3D" else "#E0E0E0"
        val buttonText get() = if (isDarkMode) "#FFFFFF" else "#333333"
        val primaryButton get() = if (isDarkMode) "#5C6BC0" else "#3F51B5"
        val todayHighlight get() = if (isDarkMode) "#5C6BC0" else "#E3F2FD"
        val selectedDay get() = if (isDarkMode) "#7986CB" else "#BBDEFB"
        val eventDefault get() = if (isDarkMode) "#5C6BC0" else "#3F51B5"
        val border get() = if (isDarkMode) "#3D3D3D" else "#E0E0E0"
    }

    // Toggle between light and dark mode
    fun toggleDarkMode() {
        _isDarkMode = !_isDarkMode
        localStorage.setItem(THEME_STORAGE_KEY, if (_isDarkMode) "dark" else "light")
    }
}
