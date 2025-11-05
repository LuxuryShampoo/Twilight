package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.*
import kotlin.js.Date

/**
 * Auto-scheduling algorithm that intelligently places tasks in free time blocks
 */
object AutoScheduler {
    
    /**
     * Schedule tasks based on urgency and task type requirements
     */
    fun scheduleTasks(
        tasks: List<Task>,
        schedule: WeeklySchedule,
        weekStartDate: Date
    ): List<Task> {
        // Sort tasks by urgency (HIGH first) and then by due date
        val sortedTasks = tasks.sortedWith(
            compareBy<Task> { task ->
                when (task.urgencyLevel) {
                    UrgencyLevel.HIGH -> 0
                    UrgencyLevel.MEDIUM -> 1
                    UrgencyLevel.LOW -> 2
                }
            }.thenBy { it.dueDate.getTime() }
        )
        
        // Get date-specific free blocks
        val freeBlocks = schedule.getFreeBlocks()
            .map { it.toDateTimeBlock(weekStartDate) }
            .sortedBy { it.startTime.getTime() }
        
        // Track which blocks have been used and how much time is left
        val availableBlocks = freeBlocks.map { block ->
            AvailableBlock(
                block = block,
                remainingMinutes = block.durationMinutes,
                scheduledSessions = mutableListOf()
            )
        }.toMutableList()
        
        // Schedule each task
        for (task in sortedTasks) {
            scheduleTask(task, availableBlocks, weekStartDate)
        }
        
        return sortedTasks
    }
    
    private fun scheduleTask(
        task: Task,
        availableBlocks: MutableList<AvailableBlock>,
        weekStartDate: Date
    ) {
        when (task.taskType) {
            TaskType.ONE_TIME_ASSIGNMENT -> scheduleOneTimeTask(task, availableBlocks)
            TaskType.PROJECT -> scheduleProjectTask(task, availableBlocks)
            TaskType.STUDY_SESSION -> scheduleStudyTask(task, availableBlocks)
            TaskType.PRACTICE_SESSION -> schedulePracticeTask(task, availableBlocks)
            TaskType.RECURRING_TASK -> scheduleRecurringTask(task, availableBlocks)
        }
    }
    
    private fun scheduleOneTimeTask(task: Task, availableBlocks: MutableList<AvailableBlock>) {
        val requiredMinutes = task.calculateTotalMinutes()
        
        // Find the first block that can fit the entire task
        for (block in availableBlocks) {
            if (block.canFit(requiredMinutes) && isBeforeDueDate(block.block.startTime, task.dueDate)) {
                val session = createSession(task, block.block.startTime, requiredMinutes)
                task.scheduledSessions.add(session)
                block.allocate(requiredMinutes)
                break
            }
        }
    }
    
    private fun scheduleProjectTask(task: Task, availableBlocks: MutableList<AvailableBlock>) {
        val sessionLength = task.getSuggestedSessionLength()
        var remainingMinutes = task.calculateTotalMinutes()
        
        // Split into multiple sessions across different days
        var lastScheduledDay = -1
        
        for (block in availableBlocks) {
            if (remainingMinutes <= 0) break
            if (!isBeforeDueDate(block.block.startTime, task.dueDate)) continue
            
            // Try to schedule on different days for better distribution
            val currentDay = block.block.startTime.getDate()
            if (currentDay == lastScheduledDay) continue
            
            val minutesToSchedule = minOf(sessionLength, remainingMinutes, block.remainingMinutes)
            if (minutesToSchedule >= 30) { // Minimum 30 min session
                val session = createSession(task, block.block.startTime, minutesToSchedule)
                task.scheduledSessions.add(session)
                block.allocate(minutesToSchedule)
                remainingMinutes -= minutesToSchedule
                lastScheduledDay = currentDay
            }
        }
    }
    
    private fun scheduleStudyTask(task: Task, availableBlocks: MutableList<AvailableBlock>) {
        val sessionLength = task.getSuggestedSessionLength()
        var remainingMinutes = task.calculateTotalMinutes()
        
        // Space out study sessions for spaced repetition
        var sessionCount = 0
        val maxSessionsPerDay = 1 // Only one study session per day for this task
        var lastScheduledDay = -1
        
        for (block in availableBlocks) {
            if (remainingMinutes <= 0) break
            if (!isBeforeDueDate(block.block.startTime, task.dueDate)) continue
            
            val currentDay = block.block.startTime.getDate()
            
            // Skip if we already scheduled this task today
            if (currentDay == lastScheduledDay) continue
            
            // Prefer spacing out by at least a day
            val minutesToSchedule = minOf(sessionLength, remainingMinutes, block.remainingMinutes)
            if (minutesToSchedule >= 20) { // Minimum 20 min for study
                val session = createSession(task, block.block.startTime, minutesToSchedule)
                task.scheduledSessions.add(session)
                block.allocate(minutesToSchedule)
                remainingMinutes -= minutesToSchedule
                lastScheduledDay = currentDay
                sessionCount++
            }
        }
    }
    
    private fun schedulePracticeTask(task: Task, availableBlocks: MutableList<AvailableBlock>) {
        val totalQuestions = task.questionCount ?: 0
        val minutesPerQuestion = task.minutesPerQuestion ?: 1.0
        val sessionLength = task.getSuggestedSessionLength()
        
        var remainingQuestions = totalQuestions
        var questionStart = 1
        
        for (block in availableBlocks) {
            if (remainingQuestions <= 0) break
            if (!isBeforeDueDate(block.block.startTime, task.dueDate)) continue
            
            // Calculate how many questions can fit in this session
            val availableMinutes = minOf(sessionLength, block.remainingMinutes)
            val questionsInSession = (availableMinutes / minutesPerQuestion).toInt()
            
            if (questionsInSession > 0) {
                val actualQuestions = minOf(questionsInSession, remainingQuestions)
                val sessionMinutes = (actualQuestions * minutesPerQuestion).toInt()
                
                val session = createSession(
                    task, 
                    block.block.startTime, 
                    sessionMinutes,
                    Pair(questionStart, questionStart + actualQuestions - 1)
                )
                task.scheduledSessions.add(session)
                block.allocate(sessionMinutes)
                
                remainingQuestions -= actualQuestions
                questionStart += actualQuestions
            }
        }
    }
    
    private fun scheduleRecurringTask(task: Task, availableBlocks: MutableList<AvailableBlock>) {
        // For simplicity, schedule like a one-time task
        // In a real implementation, this would create multiple instances
        scheduleOneTimeTask(task, availableBlocks)
    }
    
    private fun createSession(
        task: Task,
        startTime: Date,
        durationMinutes: Int,
        questionRange: Pair<Int, Int>? = null
    ): ScheduledSession {
        val endTime = Date(startTime.getTime() + durationMinutes * 60 * 1000)
        return ScheduledSession(
            id = "session-${Date.now().toLong()}-${kotlin.random.Random.nextInt()}",
            taskId = task.id,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            questionRange = questionRange
        )
    }
    
    private fun isBeforeDueDate(scheduleTime: Date, dueDate: Date): Boolean {
        return scheduleTime.getTime() <= dueDate.getTime()
    }
    
    /**
     * Represents an available block with tracking for remaining time
     */
    private data class AvailableBlock(
        val block: DateTimeBlock,
        var remainingMinutes: Int,
        val scheduledSessions: MutableList<ScheduledSession>
    ) {
        fun canFit(minutes: Int): Boolean {
            return remainingMinutes >= minutes
        }
        
        fun allocate(minutes: Int) {
            remainingMinutes -= minutes
        }
    }
    
    /**
     * Calculate statistics for scheduled tasks
     */
    fun calculateStatistics(
        tasks: List<Task>,
        schedule: WeeklySchedule
    ): SchedulingStatistics {
        val totalFreeMinutes = schedule.getTotalFreeMinutes()
        val scheduledMinutes = tasks.sumOf { task ->
            task.scheduledSessions.sumOf { it.durationMinutes }
        }
        val remainingMinutes = totalFreeMinutes - scheduledMinutes
        
        return SchedulingStatistics(
            totalFreeHours = totalFreeMinutes / 60.0,
            scheduledHours = scheduledMinutes / 60.0,
            remainingFreeHours = remainingMinutes / 60.0,
            tasksScheduled = tasks.count { it.scheduledSessions.isNotEmpty() },
            tasksUnscheduled = tasks.count { it.scheduledSessions.isEmpty() },
            utilizationPercent = if (totalFreeMinutes > 0) {
                (scheduledMinutes.toDouble() / totalFreeMinutes * 100)
            } else 0.0
        )
    }
}

/**
 * Statistics about the scheduling
 */
data class SchedulingStatistics(
    val totalFreeHours: Double,
    val scheduledHours: Double,
    val remainingFreeHours: Double,
    val tasksScheduled: Int,
    val tasksUnscheduled: Int,
    val utilizationPercent: Double
)
