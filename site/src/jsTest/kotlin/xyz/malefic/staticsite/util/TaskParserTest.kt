package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.TaskType
import xyz.malefic.staticsite.model.UrgencyLevel
import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TaskParserTest {
    
    private val referenceDate = Date(2025, 10, 5, 12, 0, 0, 0) // Nov 5, 2025 (Wednesday)
    
    @Test
    fun testParsePracticeSession() {
        val input = "SAT Math Practice - 50 questions, 1 min each, due Friday, HIGH urgency"
        val task = TaskParser.parseTask(input, referenceDate)
        
        assertNotNull(task)
        assertEquals("SAT Math Practice", task.name)
        assertEquals(TaskType.PRACTICE_SESSION, task.taskType)
        assertEquals(50, task.questionCount)
        assertEquals(1.0, task.minutesPerQuestion)
        assertEquals(50, task.calculateTotalMinutes())
        assertEquals(UrgencyLevel.HIGH, task.urgencyLevel)
        assertEquals(7, task.dueDate.getDate()) // Friday the 7th
    }
    
    @Test
    fun testParseProjectWithHours() {
        val input = "History Essay - 3 hours total, due next Monday, MEDIUM urgency"
        val task = TaskParser.parseTask(input, referenceDate)
        
        assertNotNull(task)
        assertEquals("History Essay", task.name)
        assertEquals(TaskType.PROJECT, task.taskType)
        assertEquals(180, task.calculateTotalMinutes()) // 3 hours = 180 minutes
        assertEquals(UrgencyLevel.MEDIUM, task.urgencyLevel)
        assertEquals(10, task.dueDate.getDate()) // Next Monday the 10th
    }
    
    @Test
    fun testParseStudySession() {
        val input = "Read Chapter 5 - 45 minutes, due tomorrow, LOW urgency"
        val task = TaskParser.parseTask(input, referenceDate)
        
        assertNotNull(task)
        assertEquals("Read Chapter 5", task.name)
        assertEquals(TaskType.STUDY_SESSION, task.taskType)
        assertEquals(45, task.calculateTotalMinutes())
        assertEquals(UrgencyLevel.LOW, task.urgencyLevel)
        assertEquals(6, task.dueDate.getDate()) // Tomorrow the 6th
    }
    
    @Test
    fun testParseWithMinutesPerQuestion() {
        val input = "Chemistry Problems - 20 questions, 2 min each, due Friday, HIGH urgency"
        val task = TaskParser.parseTask(input, referenceDate)
        
        assertNotNull(task)
        assertEquals(20, task.questionCount)
        assertEquals(2.0, task.minutesPerQuestion)
        assertEquals(40, task.calculateTotalMinutes())
    }
    
    @Test
    fun testDefaultValues() {
        val input = "Simple Task - due tomorrow"
        val task = TaskParser.parseTask(input, referenceDate)
        
        assertNotNull(task)
        assertEquals("Simple Task", task.name)
        assertEquals(UrgencyLevel.MEDIUM, task.urgencyLevel) // Default urgency
        assertEquals(60, task.calculateTotalMinutes()) // Default 1 hour
    }
}
