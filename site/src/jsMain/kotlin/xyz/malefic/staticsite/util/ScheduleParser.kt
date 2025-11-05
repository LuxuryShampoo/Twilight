package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.DayOfWeek
import xyz.malefic.staticsite.model.TimeBlock
import xyz.malefic.staticsite.model.WeeklySchedule
import kotlin.js.Date

/**
 * Parser for weekly schedule input
 */
object ScheduleParser {
    
    /**
     * Parse the example weekly schedule from the problem statement
     */
    fun parseDefaultSchedule(): WeeklySchedule {
        val schedule = WeeklySchedule()
        
        // MONDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "mon-period2",
            name = "Period 2 (FREE)",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 9, startMinute = 59,
            endHour = 10, endMinute = 34,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "mon-connections",
            name = "Connections (FREE)",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 11, startMinute = 53,
            endHour = 12, endMinute = 14,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "mon-home",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 17, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // TUESDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "tue-cluster",
            name = "Cluster (FREE)",
            dayOfWeek = DayOfWeek.TUESDAY,
            startHour = 11, startMinute = 30,
            endHour = 11, endMinute = 50,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "tue-home",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.TUESDAY,
            startHour = 17, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // WEDNESDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "wed-cluster",
            name = "Cluster (FREE)",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            startHour = 11, startMinute = 30,
            endHour = 11, endMinute = 50,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "wed-home1",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            startHour = 17, startMinute = 30,
            endHour = 20, endMinute = 0,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "wed-home2",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.WEDNESDAY,
            startHour = 21, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // THURSDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "thu-cluster",
            name = "Cluster (FREE)",
            dayOfWeek = DayOfWeek.THURSDAY,
            startHour = 11, startMinute = 30,
            endHour = 11, endMinute = 50,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "thu-home",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.THURSDAY,
            startHour = 17, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // FRIDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "fri-cluster",
            name = "Cluster (FREE)",
            dayOfWeek = DayOfWeek.FRIDAY,
            startHour = 11, startMinute = 30,
            endHour = 11, endMinute = 50,
            isFree = true
        ))
        schedule.timeBlocks.add(TimeBlock(
            id = "fri-home",
            name = "Home (FREE)",
            dayOfWeek = DayOfWeek.FRIDAY,
            startHour = 17, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // SATURDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "sat-free",
            name = "Free",
            dayOfWeek = DayOfWeek.SATURDAY,
            startHour = 12, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        // SUNDAY
        schedule.timeBlocks.add(TimeBlock(
            id = "sun-free",
            name = "All day FREE",
            dayOfWeek = DayOfWeek.SUNDAY,
            startHour = 0, startMinute = 0,
            endHour = 24, endMinute = 0,
            isFree = true
        ))
        
        return schedule
    }
    
    /**
     * Parse a text-based schedule
     * Format:
     * DAY:
     * - Block Name: HH:MMam/pm-HH:MMam/pm (FREE/BUSY)
     */
    fun parseScheduleText(text: String): WeeklySchedule {
        val schedule = WeeklySchedule()
        val lines = text.split("\n")
        var currentDay: DayOfWeek? = null
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            
            // Check if it's a day header
            when {
                trimmed.uppercase().startsWith("MONDAY") -> currentDay = DayOfWeek.MONDAY
                trimmed.uppercase().startsWith("TUESDAY") -> currentDay = DayOfWeek.TUESDAY
                trimmed.uppercase().startsWith("WEDNESDAY") -> currentDay = DayOfWeek.WEDNESDAY
                trimmed.uppercase().startsWith("THURSDAY") -> currentDay = DayOfWeek.THURSDAY
                trimmed.uppercase().startsWith("FRIDAY") -> currentDay = DayOfWeek.FRIDAY
                trimmed.uppercase().startsWith("SATURDAY") -> currentDay = DayOfWeek.SATURDAY
                trimmed.uppercase().startsWith("SUNDAY") -> currentDay = DayOfWeek.SUNDAY
                trimmed.startsWith("-") && currentDay != null -> {
                    // Parse time block
                    parseTimeBlockLine(trimmed, currentDay)?.let {
                        schedule.timeBlocks.add(it)
                    }
                }
            }
        }
        
        return schedule
    }
    
    private fun parseTimeBlockLine(line: String, day: DayOfWeek): TimeBlock? {
        try {
            // Example: "- Period 2 (FREE): 9:59am-10:34am"
            val parts = line.substring(1).trim().split(":")
            if (parts.size < 2) return null
            
            val namePart = parts[0].trim()
            val timePart = parts.drop(1).joinToString(":").trim()
            
            val isFree = namePart.contains("FREE", ignoreCase = true)
            val name = namePart.replace("(FREE)", "").replace("(BUSY)", "").trim()
            
            // Parse time range
            val timeRegex = """(\d+):(\d+)\s*([ap]m)\s*-\s*(\d+):(\d+)\s*([ap]m)""".toRegex(RegexOption.IGNORE_CASE)
            val match = timeRegex.find(timePart) ?: return null
            
            val startHour = convertTo24Hour(match.groupValues[1].toInt(), match.groupValues[3])
            val startMinute = match.groupValues[2].toInt()
            val endHour = convertTo24Hour(match.groupValues[4].toInt(), match.groupValues[6])
            val endMinute = match.groupValues[5].toInt()
            
            return TimeBlock(
                id = "${day.name.lowercase()}-${name.replace(" ", "-").lowercase()}",
                name = name,
                dayOfWeek = day,
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute,
                isFree = isFree
            )
        } catch (e: Exception) {
            console.log("Error parsing time block: ${e.message}")
            return null
        }
    }
    
    private fun convertTo24Hour(hour: Int, ampm: String): Int {
        return when {
            ampm.equals("am", ignoreCase = true) && hour == 12 -> 0
            ampm.equals("pm", ignoreCase = true) && hour != 12 -> hour + 12
            else -> hour
        }
    }
}
