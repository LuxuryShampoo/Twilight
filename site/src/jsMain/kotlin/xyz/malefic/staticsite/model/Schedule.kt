package xyz.malefic.staticsite.model

import kotlin.js.Date

/**
 * Represents a day of the week
 */
enum class DayOfWeek {
    SUNDAY,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY;
    
    companion object {
        fun fromDate(date: Date): DayOfWeek {
            return values()[date.getDay()]
        }
        
        fun fromIndex(index: Int): DayOfWeek {
            return values()[index % 7]
        }
    }
}

/**
 * Represents a time block in the schedule
 */
data class TimeBlock(
    val id: String,
    val name: String,
    val dayOfWeek: DayOfWeek,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val isFree: Boolean
) {
    /**
     * Get the duration of this time block in minutes
     */
    val durationMinutes: Int
        get() {
            val startTotalMinutes = startHour * 60 + startMinute
            val endTotalMinutes = endHour * 60 + endMinute
            return endTotalMinutes - startTotalMinutes
        }
    
    /**
     * Check if this time block can fit a task of given duration
     */
    fun canFit(durationMinutes: Int): Boolean {
        return isFree && this.durationMinutes >= durationMinutes
    }
    
    /**
     * Convert to a date-specific time block for a given week
     */
    fun toDateTimeBlock(weekStartDate: Date): DateTimeBlock {
        // Calculate the offset from week start (assuming weekStartDate is Sunday)
        val dayOffset = dayOfWeek.ordinal
        val blockDate = Date(weekStartDate.getTime() + dayOffset * 24 * 60 * 60 * 1000)
        
        val startTime = Date(
            blockDate.getFullYear(),
            blockDate.getMonth(),
            blockDate.getDate(),
            startHour,
            startMinute
        )
        
        val endTime = Date(
            blockDate.getFullYear(),
            blockDate.getMonth(),
            blockDate.getDate(),
            endHour,
            endMinute
        )
        
        return DateTimeBlock(
            id = id,
            name = name,
            startTime = startTime,
            endTime = endTime,
            isFree = isFree
        )
    }
}

/**
 * Represents a time block with specific dates
 */
data class DateTimeBlock(
    val id: String,
    val name: String,
    val startTime: Date,
    val endTime: Date,
    val isFree: Boolean
) {
    val durationMinutes: Int
        get() {
            val diffMs = endTime.getTime() - startTime.getTime()
            return (diffMs / (60 * 1000)).toInt()
        }
    
    fun canFit(durationMinutes: Int): Boolean {
        return isFree && this.durationMinutes >= durationMinutes
    }
}

/**
 * Represents a weekly schedule with free and busy time blocks
 */
data class WeeklySchedule(
    val timeBlocks: MutableList<TimeBlock> = mutableListOf()
) {
    /**
     * Get all free time blocks
     */
    fun getFreeBlocks(): List<TimeBlock> {
        return timeBlocks.filter { it.isFree }
    }
    
    /**
     * Get free blocks for a specific day
     */
    fun getFreeBlocksForDay(dayOfWeek: DayOfWeek): List<TimeBlock> {
        return timeBlocks.filter { it.dayOfWeek == dayOfWeek && it.isFree }
    }
    
    /**
     * Get total free time in minutes
     */
    fun getTotalFreeMinutes(): Int {
        return getFreeBlocks().sumOf { it.durationMinutes }
    }
    
    /**
     * Get total free time in hours
     */
    fun getTotalFreeHours(): Double {
        return getTotalFreeMinutes() / 60.0
    }
    
    /**
     * Convert to date-specific schedule for a given week
     */
    fun toDateSchedule(weekStartDate: Date): List<DateTimeBlock> {
        return timeBlocks.map { it.toDateTimeBlock(weekStartDate) }
    }
}
