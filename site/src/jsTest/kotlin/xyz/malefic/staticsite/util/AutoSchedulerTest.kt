package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.*
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AutoSchedulerTest {
    
    private val weekStartDate = Date(2025, 10, 2, 0, 0, 0, 0) // Sunday, Nov 2, 2025
    
    @Test
    fun testScheduleOneTimeTask() {
        val schedule = WeeklySchedule()
        // Add a 2-hour free block on Monday
        schedule.timeBlocks.add(TimeBlock(
            id = "test-block",
            name = "Free Time",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        
        val task = Task(
            id = "task1",
            name = "Test Task",
            taskType = TaskType.ONE_TIME_ASSIGNMENT,
            urgencyLevel = UrgencyLevel.HIGH,
            dueDate = Date(2025, 10, 10, 0, 0, 0, 0),
            totalMinutes = 60
        )
        
        val tasks = listOf(task)
        AutoScheduler.scheduleTasks(tasks, schedule, weekStartDate)
        
        assertEquals(1, task.scheduledSessions.size)
        assertTrue(task.scheduledSessions[0].durationMinutes >= 60)
    }
    
    @Test
    fun testScheduleByUrgency() {
        val schedule = WeeklySchedule()
        // Add multiple free blocks
        schedule.timeBlocks.add(TimeBlock(
            id = "mon-block",
            name = "Monday Free",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "tue-block",
            name = "Tuesday Free",
            dayOfWeek = DayOfWeek.TUESDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        
        val lowTask = Task(
            id = "low",
            name = "Low Priority",
            taskType = TaskType.ONE_TIME_ASSIGNMENT,
            urgencyLevel = UrgencyLevel.LOW,
            dueDate = Date(2025, 10, 15, 0, 0, 0, 0),
            totalMinutes = 60
        )
        
        val highTask = Task(
            id = "high",
            name = "High Priority",
            taskType = TaskType.ONE_TIME_ASSIGNMENT,
            urgencyLevel = UrgencyLevel.HIGH,
            dueDate = Date(2025, 10, 15, 0, 0, 0, 0),
            totalMinutes = 60
        )
        
        val tasks = listOf(lowTask, highTask)
        AutoScheduler.scheduleTasks(tasks, schedule, weekStartDate)
        
        // High priority task should be scheduled first (Monday)
        assertTrue(highTask.scheduledSessions.isNotEmpty())
        if (highTask.scheduledSessions.isNotEmpty() && lowTask.scheduledSessions.isNotEmpty()) {
            val highDay = DayOfWeek.fromDate(highTask.scheduledSessions[0].startTime)
            val lowDay = DayOfWeek.fromDate(lowTask.scheduledSessions[0].startTime)
            // High priority should be earlier in the week
            assertTrue(highDay.ordinal <= lowDay.ordinal)
        }
    }
    
    @Test
    fun testCalculateStatistics() {
        val schedule = WeeklySchedule()
        schedule.timeBlocks.add(TimeBlock(
            id = "block1",
            name = "Free Time",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        
        val task = Task(
            id = "task1",
            name = "Test Task",
            taskType = TaskType.ONE_TIME_ASSIGNMENT,
            urgencyLevel = UrgencyLevel.MEDIUM,
            dueDate = Date(2025, 10, 10, 0, 0, 0, 0),
            totalMinutes = 60
        )
        task.scheduledSessions.add(ScheduledSession(
            id = "session1",
            taskId = "task1",
            startTime = Date(2025, 10, 3, 10, 0, 0, 0),
            endTime = Date(2025, 10, 3, 11, 0, 0, 0),
            durationMinutes = 60
        ))
        
        val stats = AutoScheduler.calculateStatistics(listOf(task), schedule)
        
        assertEquals(2.0, stats.totalFreeHours) // 2 hours free
        assertEquals(1.0, stats.scheduledHours) // 1 hour scheduled
        assertEquals(1.0, stats.remainingFreeHours) // 1 hour remaining
        assertEquals(1, stats.tasksScheduled)
        assertEquals(0, stats.tasksUnscheduled)
    }
    
    @Test
    fun testScheduleProject() {
        val schedule = WeeklySchedule()
        // Add free blocks on different days
        for (day in 0..6) {
            schedule.timeBlocks.add(TimeBlock(
                id = "day$day",
                name = "Free Time",
                dayOfWeek = DayOfWeek.fromIndex(day),
                startHour = 10,
                startMinute = 0,
                endHour = 12,
                endMinute = 0,
                isFree = true
            ))
        }
        
        val projectTask = Task(
            id = "project",
            name = "Big Project",
            taskType = TaskType.PROJECT,
            urgencyLevel = UrgencyLevel.HIGH,
            dueDate = Date(2025, 10, 10, 0, 0, 0, 0),
            totalMinutes = 180, // 3 hours
            sessionLengthMinutes = 60 // 1-hour sessions
        )
        
        val tasks = listOf(projectTask)
        AutoScheduler.scheduleTasks(tasks, schedule, weekStartDate)
        
        // Should be split into multiple sessions
        assertTrue(projectTask.scheduledSessions.size > 1)
    }
}
