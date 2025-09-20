package xyz.malefic.staticsite.util

enum class Pages(
    val value: String,
    val route: String,
) {
    INDEX("Index", "/"),
    ABOUT("About", "/about"),
}
