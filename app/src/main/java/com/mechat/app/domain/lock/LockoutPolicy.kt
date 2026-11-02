package com.mechat.app.domain.lock

/**
 * Pure lockout logic: 5 wrong attempts = 15-minute lockout, then increasing
 * (doubling) delays. Counters are persisted by AppLockManager so they survive
 * app restarts. All times in milliseconds.
 */
object LockoutPolicy {
    const val MAX_ATTEMPTS = 5
    const val FIRST_LOCKOUT_MS = 15 * 60 * 1000L

    data class State(val failures: Int = 0, val lockoutUntil: Long = 0L)

    /** Returns the lockout deadline if `now` is still locked out, else 0. */
    fun remainingLockout(state: State, now: Long): Long =
        (state.lockoutUntil - now).coerceAtLeast(0L)

    /** Records a failure; returns the new state. */
    fun onFailure(state: State, now: Long): State {
        val failures = state.failures + 1
        if (failures < MAX_ATTEMPTS) return state.copy(failures = failures)
        val multiplier = 1L shl (failures - MAX_ATTEMPTS) // 1x, 2x, 4x, ...
        val lockoutMs = FIRST_LOCKOUT_MS * multiplier
        return State(failures = failures, lockoutUntil = now + lockoutMs)
    }

    fun onSuccess(): State = State()
}
