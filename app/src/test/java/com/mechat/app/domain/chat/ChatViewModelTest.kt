package com.mechat.app.domain.chat

import com.mechat.app.data.db.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatViewModelTest {

    @Test
    fun `date labels`() {
        val now = System.currentTimeMillis()
        val day = 24 * 60 * 60 * 1000L
        assertEquals("Today", dateLabel(now, now))
        assertEquals("Yesterday", dateLabel(now - day, now))
        assertEquals("5 days ago", dateLabel(now - 5 * day, now))
    }

    @Test
    fun `status ticks`() {
        assertEquals("…", statusTicks("sending"))
        assertEquals("✓", statusTicks("sent"))
        assertEquals("✓✓", statusTicks("delivered"))
        assertEquals("✗", statusTicks("failed"))
    }

    @Test
    fun `link detection finds urls only`() {
        val regex = Regex("https?://[^\\s]+")
        assertEquals("https://example.org/x", regex.find("see https://example.org/x now")?.value)
        assertEquals(null, regex.find("no link here")?.value)
    }
}
