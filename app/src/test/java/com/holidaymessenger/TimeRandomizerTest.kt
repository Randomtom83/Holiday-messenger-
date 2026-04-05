package com.holidaymessenger

import com.holidaymessenger.util.TimeRandomizer
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

class TimeRandomizerTest {

    @Test
    fun `randomTimeInWindow returns time within window`() {
        val date = LocalDate.of(2024, 6, 15)
        val startMinutes = 660  // 11:00 AM
        val endMinutes = 840    // 2:00 PM

        repeat(100) {
            val result = TimeRandomizer.randomTimeInWindow(startMinutes, endMinutes, date)

            val startMillis = date.atTime(11, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = date.atTime(14, 0)
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            assertTrue("Result $result should be >= start $startMillis", result >= startMillis)
            assertTrue("Result $result should be < end $endMillis", result < endMillis)
        }
    }

    @Test
    fun `randomTimeInWindow with fixed seed is deterministic`() {
        val date = LocalDate.of(2024, 6, 15)
        val random1 = Random(42)
        val random2 = Random(42)

        val result1 = TimeRandomizer.randomTimeInWindow(660, 840, date, random1)
        val result2 = TimeRandomizer.randomTimeInWindow(660, 840, date, random2)

        assertEquals(result1, result2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `randomTimeInWindow throws if end before start`() {
        TimeRandomizer.randomTimeInWindow(840, 660)
    }

    @Test
    fun `delayFromNow returns null for past time`() {
        val pastTime = System.currentTimeMillis() - 10000
        assertNull(TimeRandomizer.delayFromNow(pastTime))
    }

    @Test
    fun `delayFromNow returns positive for future time`() {
        val futureTime = System.currentTimeMillis() + 60000
        val delay = TimeRandomizer.delayFromNow(futureTime)
        assertNotNull(delay)
        assertTrue(delay!! > 0)
    }
}
