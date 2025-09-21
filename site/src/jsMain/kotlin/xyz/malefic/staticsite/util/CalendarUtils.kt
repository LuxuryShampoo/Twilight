package xyz.malefic.staticsite.util

import kotlin.js.Date
import kotlin.math.floor
import kotlin.random.Random

/**
 * Utility functions for calendar operations.
 */
object CalendarUtils {

    /**
     * Formats a date as a time string (HH:MM).
     * @param date The date to format
     * @return The formatted time string
     */
    fun formatTime(date: Date): String {
        val hours = date.getHours().toString().padStart(2, '0')
        val minutes = date.getMinutes().toString().padStart(2, '0')
        return "$hours:$minutes"
    }

    /**
     * Formats a date as a date string (YYYY-MM-DD).
     * @param date The date to format
     * @return The formatted date string
     */
    fun formatDate(date: Date): String {
        val year = date.getFullYear()
        val month = (date.getMonth() + 1).toString().padStart(2, '0')
        val day = date.getDate().toString().padStart(2, '0')
        return "$year-$month-$day"
    }

    /**
     * Checks if two events overlap.
     * @param event1 The first event
     * @param event2 The second event
     * @return True if the events overlap, false otherwise
     */
    fun eventsOverlap(event1: CalendarEvent, event2: CalendarEvent): Boolean {
        return event1.startTime.getTime() < event2.endTime.getTime() && 
               event1.endTime.getTime() > event2.startTime.getTime()
    }

    /**
     * Gets the events for a specific date.
     * @param calendar The calendar containing the events
     * @param date The date for which to get events
     * @return A list of events for the specified date
     */
    fun getEventsForDate(calendar: Calendar, date: Date): List<CalendarEvent> {
        val dateString = formatDate(date)
        return calendar.events.filter { event ->
            val eventDateString = formatDate(event.startTime)
            if (event.isRecurring && event.recurrenceFrequency != null) {
                isRecurringEventOnDate(event, date)
            } else {
                eventDateString == dateString
            }
        }
    }

    /**
     * Checks if a recurring event occurs on a specific date.
     * @param event The recurring event
     * @param date The date to check
     * @return True if the event occurs on the specified date, false otherwise
     */
    fun isRecurringEventOnDate(event: CalendarEvent, date: Date): Boolean {
        if (!event.isRecurring || event.recurrenceFrequency == null) {
            return false
        }

        val eventStart = event.startTime

        // If the check date is before the event start, it can't be a recurrence
        if (isSameOrBefore(date, eventStart)) {
            return false
        }

        // If the event has an end date and the check date is after it, it's not a recurrence
        val endDate = event.recurrenceEndDate
        if (endDate != null && isSameOrAfter(date, endDate)) {
            return false
        }

        when (event.recurrenceFrequency) {
            RecurrenceFrequency.DAILY -> {
                return true
            }
            RecurrenceFrequency.WEEKLY -> {
                return eventStart.getDay() == date.getDay()
            }
            RecurrenceFrequency.MONTHLY -> {
                return eventStart.getDate() == date.getDate()
            }
            RecurrenceFrequency.YEARLY -> {
                return eventStart.getMonth() == date.getMonth() && 
                       eventStart.getDate() == date.getDate()
            }
            else -> return false
        }
    }

    /**
     * Checks if date1 is the same as or before date2.
     */
    private fun isSameOrBefore(date1: Date, date2: Date): Boolean {
        return date1.getTime() <= date2.getTime()
    }

    /**
     * Checks if date1 is the same as or after date2.
     */
    private fun isSameOrAfter(date1: Date, date2: Date): Boolean {
        return date1.getTime() >= date2.getTime()
    }

    /**
     * Gets the time slot index for a specific time.
     * @param time The time to get the index for
     * @param intervalMinutes The interval between time slots in minutes
     * @return The index of the time slot
     */
    fun getTimeSlotIndex(time: Date, intervalMinutes: Int = 15): Int {
        val minutes = time.getHours() * 60 + time.getMinutes()
        return floor(minutes.toDouble() / intervalMinutes).toInt()
    }

    /**
     * Creates a new unique ID for an event.
     * @return A new unique ID
     */
    fun createEventId(): String {
        return "event_${Date().getTime()}_${Random.nextInt(1000)}"
    }

    /**
     * Creates a new unique ID for a calendar.
     * @return A new unique ID
     */
    fun createCalendarId(): String {
        return "calendar_${Date().getTime()}_${Random.nextInt(1000)}"
    }

    /**
     * Gets the day names for a week.
     * @return A list of day names
     */
    fun getDayNames(): List<String> {
        return listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

    /**
     * Gets the short day names for a week.
     * @return A list of short day names
     */
    fun getShortDayNames(): List<String> {
        return listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    }

    /**
     * Gets the month names.
     * @return A list of month names
     */
    fun getMonthNames(): List<String> {
        return listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    /**
     * Gets the short month names.
     * @return A list of short month names
     */
    fun getShortMonthNames(): List<String> {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }
}
