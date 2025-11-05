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
 * Represents the type of task/event.
 */
enum class TaskType {
    PROJECT,        // Multi-session project that should be broken into multiple work sessions
    ASSIGNMENT,     // One-off assignment that can be completed in one sitting
    SAT_STUDY,      // SAT studying that needs to be time-managed wisely
    HOMEWORK,       // Regular homework
    EXAM_PREP,      // Exam preparation
    READING,        // Reading assignments
    PRACTICE,       // Practice problems/exercises
    OTHER           // Other types
}

/**
 * Represents the urgency level of a task.
 */
enum class UrgencyLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
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
 * @property isCustom Whether the event is custom
 * @property isPassive Whether the event is passive (computed property)
 * @property date The date of the event (computed property)
 * @property hour The hour of the event (computed property)
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
    var isHoliday: Boolean = false,
    var isCustom: Boolean = false,
    // New fields for enhanced task management
    var taskType: TaskType? = null,
    var numQuestions: Int? = null,
    var timePerQuestion: Double? = null, // in minutes
    var urgencyLevel: UrgencyLevel? = null
) {
    /**
     * Computed property to check if the event is passive
     */
    val isPassive: Boolean
        get() = mode == EventMode.PASSIVE

    /**
     * Computed property to get the date of the event
     */
    val date: Date
        get() = startTime

    /**
     * Computed property to get the hour of the event
     */
    val hour: Int
        get() = startTime.getHours()

    /**
     * Computed property to get the duration in hours
     */
    val durationInHours: Double
        get() {
            val diffInMs = endTime.getTime() - startTime.getTime()
            return diffInMs / (1000 * 60 * 60) // Convert milliseconds to hours
        }

    /**
     * Computed property to get the duration in 30-minute slots
     */
    val durationInSlots: Int
        get() = (durationInHours * 2).toInt().coerceAtLeast(1) // At least 1 slot (30 minutes)

    /**
     * Calculate required time based on number of questions and time per question
     */
    val calculatedTimeInMinutes: Double?
        get() = numQuestions?.let { questions ->
            timePerQuestion?.let { time ->
                questions * time
            }
        }

    /**
     * Calculate required time in hours
     */
    val calculatedTimeInHours: Double?
        get() = calculatedTimeInMinutes?.let { it / 60.0 }

    /**
     * Check if this is a FREE block that can be used for scheduling
     */
    val isFreeBlock: Boolean
        get() = title.contains("FREE", ignoreCase = true)

    /**
     * Creates a copy of the event with the specified hour
     */
    fun copy(hour: Int): CalendarEvent {
        val newStartTime = Date(
            startTime.getFullYear(),
            startTime.getMonth(),
            startTime.getDate(),
            hour
        )
        val newEndTime = Date(
            endTime.getFullYear(),
            endTime.getMonth(),
            endTime.getDate(),
            hour + 1
        )
        
        return copy(startTime = newStartTime, endTime = newEndTime)
    }

    companion object {
        /**
         * Represents a drag end event for updating event position
         */
        data class DragEnd(
            val eventId: String,
            val date: Date,
            val hour: Int
        )
    }
}

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
