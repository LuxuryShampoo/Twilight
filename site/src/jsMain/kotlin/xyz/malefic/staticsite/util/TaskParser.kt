package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.Task
import xyz.malefic.staticsite.model.TaskType
import xyz.malefic.staticsite.model.UrgencyLevel
import kotlin.js.Date

/**
 * Parser for simplified task input
 * Example: "SAT Math Practice - 50 questions, 1 min each, due Friday, HIGH urgency"
 */
object TaskParser {
    
    /**
     * Parse a task from a natural language input string
     */
    fun parseTask(input: String, referenceDate: Date = Date()): Task? {
        try {
            val parts = input.split("-", limit = 2)
            if (parts.size < 2) return null
            
            val name = parts[0].trim()
            val detailsString = parts[1].trim()
            
            // Extract details using regex and keywords
            val questionCount = extractQuestionCount(detailsString)
            val minutesPerQuestion = extractMinutesPerQuestion(detailsString)
            val totalMinutes = extractTotalMinutes(detailsString)
            val urgency = extractUrgency(detailsString)
            val dueDate = extractDueDate(detailsString, referenceDate)
            val sessionLength = extractSessionLength(detailsString)
            val taskType = detectTaskType(input, questionCount, sessionLength)
            
            // Calculate total minutes if not explicitly provided
            val finalTotalMinutes = when {
                totalMinutes != null -> totalMinutes
                questionCount != null && minutesPerQuestion != null -> 
                    (questionCount * minutesPerQuestion).toInt()
                else -> 60 // default to 1 hour
            }
            
            return Task(
                id = "task-${Date.now().toLong()}-${kotlin.random.Random.nextInt(1000, 9999)}",
                name = name,
                taskType = taskType,
                urgencyLevel = urgency ?: UrgencyLevel.MEDIUM,
                dueDate = dueDate ?: DateParser.addDays(referenceDate, 7),
                totalMinutes = finalTotalMinutes,
                questionCount = questionCount,
                minutesPerQuestion = minutesPerQuestion,
                sessionLengthMinutes = sessionLength
            )
        } catch (e: Exception) {
            console.log("Error parsing task: ${e.message}")
            return null
        }
    }
    
    private fun extractQuestionCount(input: String): Int? {
        val regex = """(\d+)\s*questions?""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(input)?.groupValues?.get(1)?.toIntOrNull()
    }
    
    private fun extractMinutesPerQuestion(input: String): Double? {
        // Pattern: "X min each", "X minutes each", "X min per question"
        val regex = """(\d+\.?\d*)\s*min(?:ute)?s?\s*(?:each|per\s*question)""".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(input)?.groupValues?.get(1)?.toDoubleOrNull()
    }
    
    private fun extractTotalMinutes(input: String): Int? {
        // Pattern: "X hours", "X minutes", "X hrs"
        val hourRegex = """(\d+\.?\d*)\s*(?:hours?|hrs?)(?:\s+total)?""".toRegex(RegexOption.IGNORE_CASE)
        val minuteRegex = """(\d+)\s*(?:minutes?|mins?)(?:\s+total)?""".toRegex(RegexOption.IGNORE_CASE)
        
        hourRegex.find(input)?.let {
            val hours = it.groupValues[1].toDoubleOrNull()
            return hours?.let { h -> (h * 60).toInt() }
        }
        
        minuteRegex.find(input)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        
        return null
    }
    
    private fun extractUrgency(input: String): UrgencyLevel? {
        return when {
            input.contains("HIGH", ignoreCase = true) -> UrgencyLevel.HIGH
            input.contains("MEDIUM", ignoreCase = true) -> UrgencyLevel.MEDIUM
            input.contains("LOW", ignoreCase = true) -> UrgencyLevel.LOW
            else -> null
        }
    }
    
    private fun extractDueDate(input: String, referenceDate: Date): Date? {
        // Look for "due X" pattern
        val dueRegex = """due\s+(.+?)(?:,|${'$'})""".toRegex(RegexOption.IGNORE_CASE)
        val match = dueRegex.find(input) ?: return null
        
        val dateString = match.groupValues[1].trim()
        return DateParser.parseDate(dateString, referenceDate)
    }
    
    private fun extractSessionLength(input: String): Int? {
        // Pattern: "X min sessions", "X hour sessions"
        val minuteRegex = """(\d+)\s*min(?:ute)?s?\s*sessions?""".toRegex(RegexOption.IGNORE_CASE)
        val hourRegex = """(\d+\.?\d*)\s*(?:hour|hr)s?\s*sessions?""".toRegex(RegexOption.IGNORE_CASE)
        
        minuteRegex.find(input)?.let {
            return it.groupValues[1].toIntOrNull()
        }
        
        hourRegex.find(input)?.let {
            val hours = it.groupValues[1].toDoubleOrNull()
            return hours?.let { h -> (h * 60).toInt() }
        }
        
        return null
    }
    
    private fun detectTaskType(input: String, questionCount: Int?, sessionLength: Int?): TaskType {
        val lowerInput = input.lowercase()
        
        return when {
            questionCount != null -> TaskType.PRACTICE_SESSION
            lowerInput.contains("study") || lowerInput.contains("read") || lowerInput.contains("review") -> 
                TaskType.STUDY_SESSION
            lowerInput.contains("project") || lowerInput.contains("essay") || sessionLength != null -> 
                TaskType.PROJECT
            lowerInput.contains("daily") || lowerInput.contains("weekly") || lowerInput.contains("recurring") -> 
                TaskType.RECURRING_TASK
            else -> TaskType.ONE_TIME_ASSIGNMENT
        }
    }
}
