package xyz.malefic.staticsite.model

data class Calendar(
    val id: String,
    var name: String,
    val events: MutableList<CalendarEvent> = mutableListOf(),
)
