package xyz.malefic.staticsite.model

import kotlin.js.Date

data class CalendarEvent(
    val id: String,
    var title: String,
    var description: String,
    var startTime: Date,
    var endTime: Date,
    var color: String,
    var isRecurring: Boolean = false,
    var recurrenceFrequency: RecurrenceFrequency? = null,
    var recurrenceEndDate: Date? = null,
)
