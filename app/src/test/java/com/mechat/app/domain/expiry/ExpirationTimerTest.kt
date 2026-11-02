package com.mechat.app.domain.expiry

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpirationTimerTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `off means no expiry`() {
        assertNull(ExpirationTimer.expiresAt(now, ExpirationTimer.Option.OFF))
        assertFalse(ExpirationTimer.isExpired(null, now))
    }

    @Test
    fun `expiry computed from start time`() {
        val e = ExpirationTimer.expiresAt(now, ExpirationTimer.Option.ONE_HOUR)!!
        assertEquals(now + 3_600_000L, e)
        assertTrue(ExpirationTimer.isExpired(e, now + 3_600_000L))
        assertFalse(ExpirationTimer.isExpired(e, now + 3_599_999L))
    }

    @Test
    fun `default is 24 hours`() {
        assertEquals(ExpirationTimer.Option.TWENTY_FOUR_HOURS, ExpirationTimer.Option.DEFAULT)
        assertEquals(86_400_000L, ExpirationTimer.Option.DEFAULT.ms)
    }

    @Test
    fun `countdown label formats hours and minutes`() {
        val e = now + 18 * 3_600_000L + 31 * 60_000L
        assertEquals("Disappearing in 18h 31m", ExpirationTimer.countdownLabel(e, now))
        assertEquals("Disappearing in 5m", ExpirationTimer.countdownLabel(now + 5 * 60_000L, now))
    }

    @Test
    fun `progress goes from 1 to 0`() {
        val opt = ExpirationTimer.Option.ONE_HOUR
        val e = ExpirationTimer.expiresAt(now, opt)!!
        assertEquals(1f, ExpirationTimer.progress(e, now, opt, now), 0.001f)
        assertEquals(0f, ExpirationTimer.progress(e, now, opt, e), 0.001f)
        assertEquals(0.5f, ExpirationTimer.progress(e, now, opt, now + opt.ms / 2), 0.01f)
    }
}
