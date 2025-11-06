package xyz.malefic.staticsite.util

import xyz.malefic.staticsite.model.DayOfWeek
import xyz.malefic.staticsite.model.WeeklySchedule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduleParserTest {
    
    @Test
    fun testParseDefaultSchedule() {
        val schedule = ScheduleParser.parseDefaultSchedule()
        
        // Should have free blocks
        assertTrue(schedule.timeBlocks.isNotEmpty())
        
        // Check total free time is calculated correctly
        val totalFreeHours = schedule.getTotalFreeHours()
        assertTrue(totalFreeHours > 0)
        
        // Verify some specific blocks exist
        val mondayBlocks = schedule.getFreeBlocksForDay(DayOfWeek.MONDAY)
        assertTrue(mondayBlocks.isNotEmpty())
        
        val sundayBlocks = schedule.getFreeBlocksForDay(DayOfWeek.SUNDAY)
        assertTrue(sundayBlocks.isNotEmpty())
    }
    
    @Test
    fun testGetFreeBlocks() {
        val schedule = WeeklySchedule()
        schedule.timeBlocks.add(xyz.malefic.staticsite.model.TimeBlock(
            id = "free1",
            name = "Free Time",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        schedule.timeBlocks.add(xyz.malefic.staticsite.model.TimeBlock(
            id = "busy1",
            name = "Busy Time",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 14,
            startMinute = 0,
            endHour = 16,
            endMinute = 0,
            isFree = false
        ))
        
        val freeBlocks = schedule.getFreeBlocks()
        assertEquals(1, freeBlocks.size)
        assertEquals("free1", freeBlocks[0].id)
    }
    
    @Test
    fun testGetFreeBlocksForDay() {
        val schedule = WeeklySchedule()
        schedule.timeBlocks.add(xyz.malefic.staticsite.model.TimeBlock(
            id = "mon-free",
            name = "Monday Free",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        schedule.timeBlocks.add(xyz.malefic.staticsite.model.TimeBlock(
            id = "tue-free",
            name = "Tuesday Free",
            dayOfWeek = DayOfWeek.TUESDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        ))
        
        val mondayBlocks = schedule.getFreeBlocksForDay(DayOfWeek.MONDAY)
        assertEquals(1, mondayBlocks.size)
        assertEquals("mon-free", mondayBlocks[0].id)
        
        val tuesdayBlocks = schedule.getFreeBlocksForDay(DayOfWeek.TUESDAY)
        assertEquals(1, tuesdayBlocks.size)
        assertEquals("tue-free", tuesdayBlocks[0].id)
    }
    
    @Test
    fun testTimeBlockDuration() {
        val block = xyz.malefic.staticsite.model.TimeBlock(
            id = "test",
            name = "Test Block",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 30,
            endHour = 12,
            endMinute = 45,
            isFree = true
        )
        
        // 2 hours 15 minutes = 135 minutes
        assertEquals(135, block.durationMinutes)
    }
    
    @Test
    fun testCanFit() {
        val block = xyz.malefic.staticsite.model.TimeBlock(
            id = "test",
            name = "Test Block",
            dayOfWeek = DayOfWeek.MONDAY,
            startHour = 10,
            startMinute = 0,
            endHour = 12,
            endMinute = 0,
            isFree = true
        )
        
        assertTrue(block.canFit(60)) // Can fit 1 hour
        assertTrue(block.canFit(120)) // Can fit 2 hours
        assertTrue(!block.canFit(150)) // Cannot fit 2.5 hours
    }
}
