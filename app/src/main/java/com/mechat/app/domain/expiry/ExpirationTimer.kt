package com.mechat.app.domain.expiry

/**
 * Pure disappearing-message timer logic (unit-testable).
 * Timer starts when the message is READ (incoming) or SENT (outgoing).
 */
object ExpirationTimer {

    enum class Option(val ms: Long) {
        OFF(0L),
        ONE_HOUR(60 * 60 * 1000L),
        TWENTY_FOUR_HOURS(24 * 60 * 60 * 1000L), // default
        SEVEN_DAYS(7 * 24 * 60 * 60 * 1000L);

        companion object {
            val DEFAULT = TWENTY_FOUR_HOURS
            fun fromMs(ms: Long) = entries.firstOrNull { it.ms == ms } ?: OFF
        }
    }

    /** expiresAt from the moment the timer starts (read for incoming, sent for outgoing). */
    fun expiresAt(startedAt: Long, option: Option): Long? =
        if (option == Option.OFF) null else startedAt + option.ms

    fun isExpired(expiresAt: Long?, now: Long): Boolean =
        expiresAt != null && now >= expiresAt

    /** Live countdown label, e.g. "Disappearing in 18h 31m". */
    fun countdownLabel(expiresAt: Long, now: Long): String {
        val remaining = (expiresAt - now).coerceAtLeast(0L)
        val h = remaining / 3_600_000
        val m = (remaining % 3_600_000) / 60_000
        return if (h > 0) "Disappearing in ${h}h ${m}m" else "Disappearing in ${m}m"
    }

    /** Thin progress line fraction 0..1 remaining. */
    fun progress(expiresAt: Long, startedAt: Long, option: Option, now: Long): Float {
        if (option == Option.OFF) return 1f
        val total = option.ms.toFloat()
        val remaining = (expiresAt - now).coerceIn(0L, option.ms)
        return remaining / total
    }
}
