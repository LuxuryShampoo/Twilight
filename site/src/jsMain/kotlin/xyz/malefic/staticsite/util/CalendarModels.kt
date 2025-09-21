package xyz.malefic.staticsite.util

import androidx.compose.runtime.Stable
import com.varabyte.kobweb.compose.ui.graphics.Color
import kotlin.js.Date

/**
 * Represents the mode of a calendar event.
 * - PASSIVE: Events that are just for reference, not requiring action
 * - ACTIVE: Events that require adherence to the schedule
 * - CUSTOM: Events where the user can choose whether they are active or passive
 */
enum class EventMode {
    PASSIVE,
    ACTIVE,
    CUSTOM
}

/**
 * Represents the frequency of a recurring event.
 */
enum class RecurrenceFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    CUSTOM
}

/**
 * Represents a theme for a calendar.
 * @property name The name of the theme
 * @property primaryColor The primary color of the theme
 * @property secondaryColor The secondary color of the theme
 * @property backgroundImage Optional background image URL
 */
data class CalendarTheme(
    val name: String,
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String = "#ffffff",
    val backgroundImage: String? = null
)

/**
 * Represents a calendar event.
 * @property id Unique identifier for the event
 * @property title The title of the event
 * @property description Optional description of the event
 * @property startTime The start time of the event
 * @property endTime The end time of the event
 * @property mode The mode of the event (PASSIVE, ACTIVE, CUSTOM)
 * @property isRecurring Whether the event is recurring
 * @property recurrenceFrequency The frequency of recurrence if the event is recurring
 * @property recurrenceEndDate The end date of recurrence if the event is recurring
 * @property color Optional color for the event
 * @property isHoliday Whether the event is a holiday
 */
@Stable
data class CalendarEvent(
    val id: String,
    var title: String,
    var description: String = "",
    var startTime: Date,
    var endTime: Date,
    var mode: EventMode = EventMode.PASSIVE,
    var isRecurring: Boolean = false,
    var recurrenceFrequency: RecurrenceFrequency? = null,
    var recurrenceEndDate: Date? = null,
    var color: String? = null,
    var isHoliday: Boolean = false
)

/**
 * Represents a calendar.
 * @property id Unique identifier for the calendar
 * @property title The title of the calendar
 * @property theme The theme of the calendar
 * @property events The list of events in the calendar
 */
@Stable
data class Calendar(
    val id: String,
    var title: String,
    var theme: CalendarTheme,
    val events: MutableList<CalendarEvent> = mutableListOf()
)

/**
 * Represents the state of the calendar application.
 * @property calendars The list of calendars
 * @property activeCalendarId The ID of the currently active calendar
 * @property availableThemes The list of available themes
 */
@Stable
data class CalendarState(
    val calendars: MutableList<Calendar> = mutableListOf(),
    var activeCalendarId: String? = null,
    val availableThemes: MutableList<CalendarTheme> = mutableListOf(
        CalendarTheme(
            name = "Default",
            primaryColor = "#9c27b0", // Deep Purple
            secondaryColor = "#e91e63" // Pink
        ),
        CalendarTheme(
            name = "Ocean",
            primaryColor = "#1976d2", // Blue
            secondaryColor = "#00bcd4" // Cyan
        ),
        CalendarTheme(
            name = "Forest",
            primaryColor = "#388e3c", // Green
            secondaryColor = "#8bc34a" // Light Green
        ),
        CalendarTheme(
            name = "Sunset",
            primaryColor = "#f57c00", // Orange
            secondaryColor = "#ffeb3b" // Yellow
        )
    )
)
