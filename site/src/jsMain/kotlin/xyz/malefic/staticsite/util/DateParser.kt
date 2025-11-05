package xyz.malefic.staticsite.util

import kotlin.js.Date

/**
 * Utility for parsing dates from natural language
 */
object DateParser {
    
    /**
     * Parse a date string that may be in natural language
     * Supports: "tomorrow", "today", "Friday", "next Monday", "in 3 days", ISO dates
     * Reference date is 2025-11-05 (Wednesday)
     */
    fun parseDate(input: String, referenceDate: Date = Date()): Date? {
        val normalized = input.trim().lowercase()
        
        return when {
            normalized == "today" -> {
                getStartOfDay(referenceDate)
            }
            normalized == "tomorrow" -> {
                addDaysPrivate(getStartOfDay(referenceDate), 1)
            }
            normalized.startsWith("in ") && normalized.contains("day") -> {
                val parts = normalized.split(" ")
                val days = parts.getOrNull(1)?.toIntOrNull() ?: return null
                addDaysPrivate(getStartOfDay(referenceDate), days)
            }
            normalized == "monday" || normalized == "next monday" -> {
                getNextDayOfWeek(referenceDate, 1, normalized.contains("next"))
            }
            normalized == "tuesday" || normalized == "next tuesday" -> {
                getNextDayOfWeek(referenceDate, 2, normalized.contains("next"))
            }
            normalized == "wednesday" || normalized == "next wednesday" -> {
                getNextDayOfWeek(referenceDate, 3, normalized.contains("next"))
            }
            normalized == "thursday" || normalized == "next thursday" -> {
                getNextDayOfWeek(referenceDate, 4, normalized.contains("next"))
            }
            normalized == "friday" || normalized == "next friday" -> {
                getNextDayOfWeek(referenceDate, 5, normalized.contains("next"))
            }
            normalized == "saturday" || normalized == "next saturday" -> {
                getNextDayOfWeek(referenceDate, 6, normalized.contains("next"))
            }
            normalized == "sunday" || normalized == "next sunday" -> {
                getNextDayOfWeek(referenceDate, 0, normalized.contains("next"))
            }
            // Try parsing as ISO date or standard date format
            else -> {
                try {
                    Date(input)
                } catch (e: Exception) {
                    null
                }
            }
        }
    }
    
    /**
     * Get the start of day (midnight) for a given date
     */
    private fun getStartOfDay(date: Date): Date {
        return Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0)
    }
    
    /**
     * Add days to a date (public for use by other parsers)
     */
    fun addDays(date: Date, days: Int): Date {
        return Date(date.getTime() + days * 24 * 60 * 60 * 1000)
    }
    
    /**
     * Add days to a date (private version for internal use)
     */
    private fun addDaysPrivate(date: Date, days: Int): Date {
        return Date(date.getTime() + days * 24 * 60 * 60 * 1000)
    }
    
    /**
     * Get the next occurrence of a day of week
     * @param referenceDate the reference date
     * @param targetDay 0=Sunday, 1=Monday, ..., 6=Saturday
     * @param forceNext if true, always get next week even if today is the target day
     */
    private fun getNextDayOfWeek(referenceDate: Date, targetDay: Int, forceNext: Boolean = false): Date {
        val currentDay = referenceDate.getDay()
        var daysToAdd = targetDay - currentDay
        
        if (daysToAdd <= 0 || forceNext) {
            daysToAdd += 7
        }
        
        return addDaysPrivate(getStartOfDay(referenceDate), daysToAdd)
    }
    
    /**
     * Format a date to a readable string
     */
    fun formatDate(date: Date): String {
        val days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        
        return "${days[date.getDay()]}, ${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()}"
    }
    
    /**
     * Format time to readable string (e.g., "2:30 PM")
     */
    fun formatTime(date: Date): String {
        val hour = date.getHours()
        val minute = date.getMinutes()
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val displayMinute = minute.toString().padStart(2, '0')
        
        return "$displayHour:$displayMinute $ampm"
    }
    
    /**
     * Format date and time together
     */
    fun formatDateTime(date: Date): String {
        return "${formatDate(date)} at ${formatTime(date)}"
    }
}
