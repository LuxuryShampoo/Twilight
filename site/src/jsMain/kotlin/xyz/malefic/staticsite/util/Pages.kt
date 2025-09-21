package xyz.malefic.staticsite.util

enum class Pages(
    val value: String,
    val route: String,
) {
    INDEX("Calendar", "/"),
    ABOUT("About", "/about"),
    CALENDAR_SETTINGS("Settings", "/settings"),
    CREATE_CALENDAR("Create Calendar", "/create"),
}
