package xyz.malefic.staticsite.model

/**
 * Represents different types of tasks with smart scheduling behavior
 */
enum class TaskType {
    /**
     * Single block completion - schedule in one free period
     */
    ONE_TIME_ASSIGNMENT,
    
    /**
     * Break into multiple work sessions
     */
    PROJECT,
    
    /**
     * Spaced repetition - distribute across multiple days
     */
    STUDY_SESSION,
    
    /**
     * Practice with question counts (e.g., SAT prep)
     */
    PRACTICE_SESSION,
    
    /**
     * Regular activities (daily/weekly patterns)
     */
    RECURRING_TASK
}
