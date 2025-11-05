package xyz.malefic.staticsite.model

/**
 * Represents the urgency level of a task
 */
enum class UrgencyLevel {
    HIGH,
    MEDIUM,
    LOW;
    
    /**
     * Get the color for this urgency level
     */
    fun getColor(): String = when (this) {
        HIGH -> "#ef4444"    // red
        MEDIUM -> "#eab308"  // yellow
        LOW -> "#22c55e"     // green
    }
}
