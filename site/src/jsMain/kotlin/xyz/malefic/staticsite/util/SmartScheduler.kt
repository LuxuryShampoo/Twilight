package xyz.malefic.staticsite.util

import kotlin.js.Date

/**
 * Smart scheduler that automatically organizes tasks into available FREE time blocks
 */
object SmartScheduler {
    
    /**
     * Finds all FREE time blocks in the calendar
     */
    fun findFreeBlocks(events: List<CalendarEvent>): List<CalendarEvent> {
        return events.filter { it.isFreeBlock }.sortedBy { it.startTime.getTime() }
    }
    
    /**
     * Schedules tasks into available FREE time blocks based on urgency and task type
     */
    fun scheduleTasksIntoFreeBlocks(
        tasks: List<CalendarEvent>,
        existingEvents: List<CalendarEvent>
    ): List<CalendarEvent> {
        val freeBlocks = findFreeBlocks(existingEvents).toMutableList()
        val scheduledTasks = mutableListOf<CalendarEvent>()
        
        // Sort tasks by urgency (CRITICAL > HIGH > MEDIUM > LOW) and then by calculated time
        val sortedTasks = tasks.sortedWith(
            compareByDescending<CalendarEvent> { it.urgencyLevel?.ordinal ?: 0 }
                .thenBy { it.calculatedTimeInHours ?: it.durationInHours }
        )
        
        for (task in sortedTasks) {
            val requiredHours = task.calculatedTimeInHours ?: task.durationInHours
            
            when (task.taskType) {
                TaskType.PROJECT -> {
                    // Break projects into multiple sessions
                    scheduleProjectTask(task, requiredHours, freeBlocks, scheduledTasks)
                }
                TaskType.SAT_STUDY -> {
                    // Schedule SAT study in optimal time blocks (not too long, not too short)
                    scheduleSATStudy(task, requiredHours, freeBlocks, scheduledTasks)
                }
                TaskType.ASSIGNMENT, TaskType.HOMEWORK, TaskType.PRACTICE -> {
                    // Try to schedule in one sitting
                    scheduleOneShotTask(task, requiredHours, freeBlocks, scheduledTasks)
                }
                else -> {
                    // Default behavior: schedule in one block if possible
                    scheduleOneShotTask(task, requiredHours, freeBlocks, scheduledTasks)
                }
            }
        }
        
        return scheduledTasks
    }
    
    /**
     * Schedule a project task by breaking it into multiple work sessions
     */
    private fun scheduleProjectTask(
        task: CalendarEvent,
        totalHours: Double,
        freeBlocks: MutableList<CalendarEvent>,
        scheduledTasks: MutableList<CalendarEvent>
    ) {
        val maxSessionHours = 2.0 // Don't work on projects for more than 2 hours at a time
        var remainingHours = totalHours
        var sessionNumber = 1
        
        while (remainingHours > 0 && freeBlocks.isNotEmpty()) {
            val sessionHours = minOf(remainingHours, maxSessionHours)
            val block = findSuitableBlock(sessionHours, freeBlocks)
            
            if (block != null) {
                val scheduled = createScheduledTask(
                    task,
                    block,
                    sessionHours,
                    "${task.title} (Session $sessionNumber)"
                )
                scheduledTasks.add(scheduled)
                
                // Update the free block
                updateFreeBlock(block, scheduled, freeBlocks)
                
                remainingHours -= sessionHours
                sessionNumber++
            } else {
                break
            }
        }
    }
    
    /**
     * Schedule SAT study in optimal time blocks (45-90 minute sessions)
     */
    private fun scheduleSATStudy(
        task: CalendarEvent,
        totalHours: Double,
        freeBlocks: MutableList<CalendarEvent>,
        scheduledTasks: MutableList<CalendarEvent>
    ) {
        val optimalSessionHours = 1.25 // 75 minutes is a good SAT study session
        var remainingHours = totalHours
        var sessionNumber = 1
        
        while (remainingHours > 0 && freeBlocks.isNotEmpty()) {
            val sessionHours = minOf(remainingHours, optimalSessionHours)
            val block = findSuitableBlock(sessionHours, freeBlocks)
            
            if (block != null) {
                val scheduled = createScheduledTask(
                    task,
                    block,
                    sessionHours,
                    if (sessionNumber == 1 && remainingHours <= optimalSessionHours) task.title 
                    else "${task.title} (Session $sessionNumber)"
                )
                scheduledTasks.add(scheduled)
                
                // Update the free block
                updateFreeBlock(block, scheduled, freeBlocks)
                
                remainingHours -= sessionHours
                sessionNumber++
            } else {
                break
            }
        }
    }
    
    /**
     * Schedule a task that should be completed in one sitting
     */
    private fun scheduleOneShotTask(
        task: CalendarEvent,
        requiredHours: Double,
        freeBlocks: MutableList<CalendarEvent>,
        scheduledTasks: MutableList<CalendarEvent>
    ) {
        val block = findSuitableBlock(requiredHours, freeBlocks)
        
        if (block != null) {
            val scheduled = createScheduledTask(task, block, requiredHours, task.title)
            scheduledTasks.add(scheduled)
            
            // Update the free block
            updateFreeBlock(block, scheduled, freeBlocks)
        }
    }
    
    /**
     * Find a suitable free block for the given duration
     */
    private fun findSuitableBlock(
        requiredHours: Double,
        freeBlocks: List<CalendarEvent>
    ): CalendarEvent? {
        // Find the first block that has enough time
        return freeBlocks.firstOrNull { it.durationInHours >= requiredHours }
    }
    
    /**
     * Create a scheduled task event from a task and a free block
     */
    private fun createScheduledTask(
        task: CalendarEvent,
        block: CalendarEvent,
        durationHours: Double,
        title: String
    ): CalendarEvent {
        val startTime = block.startTime
        val endTime = Date(startTime.getTime() + (durationHours * 60 * 60 * 1000).toLong())
        
        return task.copy(
            id = CalendarUtils.createEventId(),
            title = title,
            startTime = startTime,
            endTime = endTime,
            mode = when (task.urgencyLevel) {
                UrgencyLevel.CRITICAL, UrgencyLevel.HIGH -> EventMode.ACTIVE
                else -> EventMode.PASSIVE
            },
            color = task.color ?: getColorForUrgency(task.urgencyLevel)
        )
    }
    
    /**
     * Update the free block after scheduling a task in it
     */
    private fun updateFreeBlock(
        block: CalendarEvent,
        scheduled: CalendarEvent,
        freeBlocks: MutableList<CalendarEvent>
    ) {
        freeBlocks.remove(block)
        
        // If there's remaining time in the block, add it back
        val remainingTime = block.endTime.getTime() - scheduled.endTime.getTime()
        if (remainingTime > 15 * 60 * 1000) { // At least 15 minutes remaining
            val newBlock = block.copy(
                id = CalendarUtils.createEventId(),
                startTime = scheduled.endTime
            )
            freeBlocks.add(0, newBlock) // Add to beginning to prioritize it
        }
    }
    
    /**
     * Get color based on urgency level
     */
    private fun getColorForUrgency(urgency: UrgencyLevel?): String {
        return when (urgency) {
            UrgencyLevel.CRITICAL -> "#dc2626" // Red
            UrgencyLevel.HIGH -> "#f59e0b"     // Orange
            UrgencyLevel.MEDIUM -> "#3b82f6"   // Blue
            UrgencyLevel.LOW -> "#10b981"      // Green
            null -> "#6b7280"                   // Gray
        }
    }
}
