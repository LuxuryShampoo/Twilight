package xyz.malefic.staticsite.model

import kotlin.js.Date

/**
 * Represents a task that needs to be scheduled
 */
data class Task(
    val id: String,
    var name: String,
    var taskType: TaskType,
    var urgencyLevel: UrgencyLevel,
    var dueDate: Date,
    var totalMinutes: Int,
    var questionCount: Int? = null,
    var minutesPerQuestion: Double? = null,
    var sessionLengthMinutes: Int? = null,
    var description: String = "",
    var isCompleted: Boolean = false,
    var scheduledSessions: MutableList<ScheduledSession> = mutableListOf()
) {
    /**
     * Get the total duration in hours
     */
    val totalHours: Double
        get() = totalMinutes / 60.0
    
    /**
     * Calculate total time needed based on questions if applicable
     */
    fun calculateTotalMinutes(): Int {
        return if (questionCount != null && minutesPerQuestion != null) {
            (questionCount!! * minutesPerQuestion!!).toInt()
        } else {
            totalMinutes
        }
    }
    
    /**
     * Get the suggested session length based on task type
     */
    fun getSuggestedSessionLength(): Int {
        return sessionLengthMinutes ?: when (taskType) {
            TaskType.ONE_TIME_ASSIGNMENT -> totalMinutes
            TaskType.PROJECT -> 60 // 1 hour sessions
            TaskType.STUDY_SESSION -> 30 // 30 minute sessions for better retention
            TaskType.PRACTICE_SESSION -> 45 // 45 minute sessions
            TaskType.RECURRING_TASK -> totalMinutes
        }
    }
    
    /**
     * Calculate how many sessions are needed
     */
    fun getRequiredSessionCount(): Int {
        val totalTime = calculateTotalMinutes()
        val sessionLength = getSuggestedSessionLength()
        return (totalTime.toDouble() / sessionLength).toInt().coerceAtLeast(1)
    }
}

/**
 * Represents a scheduled session for a task
 */
data class ScheduledSession(
    val id: String,
    val taskId: String,
    var startTime: Date,
    var endTime: Date,
    var durationMinutes: Int,
    var questionRange: Pair<Int, Int>? = null // For practice sessions
)
