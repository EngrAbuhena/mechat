package com.mechat.app.network

/** Pure exponential backoff: base * 2^attempt, capped at max. Unit-testable. */
object Backoff {
    fun delayMs(attempt: Int, baseMs: Long = 1000L, maxMs: Long = 60_000L): Long {
        val exp = 1L shl attempt.coerceAtMost(20)
        return (baseMs * exp).coerceAtMost(maxMs)
    }
}
