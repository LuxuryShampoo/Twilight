package xyz.malefic.staticsite.util

import kotlin.js.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DateParserTest {
    
    // Reference date: 2025-11-05 (Wednesday)
    private val referenceDate = Date(2025, 10, 5, 12, 0, 0, 0) // Month is 0-indexed
    
    @Test
    fun testParseToday() {
        val result = DateParser.parseDate("today", referenceDate)
        assertNotNull(result)
        assertEquals(5, result.getDate())
        assertEquals(10, result.getMonth()) // November (0-indexed)
        assertEquals(2025, result.getFullYear())
    }
    
    @Test
    fun testParseTomorrow() {
        val result = DateParser.parseDate("tomorrow", referenceDate)
        assertNotNull(result)
        assertEquals(6, result.getDate())
        assertEquals(10, result.getMonth())
        assertEquals(2025, result.getFullYear())
    }
    
    @Test
    fun testParseInDays() {
        val result = DateParser.parseDate("in 3 days", referenceDate)
        assertNotNull(result)
        assertEquals(8, result.getDate())
        assertEquals(10, result.getMonth())
        assertEquals(2025, result.getFullYear())
    }
    
    @Test
    fun testParseFriday() {
        // Wednesday Nov 5 -> Friday Nov 7
        val result = DateParser.parseDate("Friday", referenceDate)
        assertNotNull(result)
        assertEquals(7, result.getDate())
        assertEquals(5, result.getDay()) // Friday
    }
    
    @Test
    fun testParseNextMonday() {
        // Wednesday Nov 5 -> Monday Nov 10
        val result = DateParser.parseDate("next Monday", referenceDate)
        assertNotNull(result)
        assertEquals(10, result.getDate())
        assertEquals(1, result.getDay()) // Monday
    }
    
    @Test
    fun testFormatDate() {
        val date = Date(2025, 10, 5, 14, 30, 0, 0)
        val formatted = DateParser.formatDate(date)
        assertTrue(formatted.contains("Wednesday"))
        assertTrue(formatted.contains("Nov"))
        assertTrue(formatted.contains("5"))
        assertTrue(formatted.contains("2025"))
    }
    
    @Test
    fun testFormatTime() {
        val date1 = Date(2025, 10, 5, 14, 30, 0, 0)
        val formatted1 = DateParser.formatTime(date1)
        assertEquals("2:30 PM", formatted1)
        
        val date2 = Date(2025, 10, 5, 9, 5, 0, 0)
        val formatted2 = DateParser.formatTime(date2)
        assertEquals("9:05 AM", formatted2)
    }
    
    @Test
    fun testAddDays() {
        val date = Date(2025, 10, 5, 12, 0, 0, 0)
        val result = DateParser.addDays(date, 5)
        assertEquals(10, result.getDate())
        assertEquals(10, result.getMonth())
        assertEquals(2025, result.getFullYear())
    }
}
