package com.mechat.app.domain.lock

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

private val Context.lockPrefs by preferencesDataStore(name = "mechat_lock")

/** Lock delay options (user-selected). */
enum class LockDelay(val ms: Long) {
    IMMEDIATE(0L), THIRTY_SECONDS(30_000L), FIVE_MINUTES(5 * 60_000L);

    companion object {
        fun fromOrdinal(o: Int) = entries[o.coerceIn(0, entries.size - 1)]
    }
}

/**
 * App lock state. PIN hash + salt + lockout counters live in DataStore, so they
 * survive app restarts. PIN-only by design — no biometrics (see docs).
 */
@Singleton
private const val AUTO_WIPE_THRESHOLD = 10

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pinHasher: com.mechat.app.crypto.PinHasher,
) {
    private val pinHashKey = stringPreferencesKey("pin_hash_v1")
    private val pinSaltKey = stringPreferencesKey("pin_salt_v1")
    private val failuresKey = intPreferencesKey("lock_failures")
    private val lockoutUntilKey = longPreferencesKey("lockout_until")
    private val delayKey = intPreferencesKey("lock_delay_ordinal")
    private val panicHashKey = stringPreferencesKey("panic_hash_v1")
    private val panicSaltKey = stringPreferencesKey("panic_salt_v1")
    private val autoWipeKey = booleanPreferencesKey("auto_wipe_enabled")

    private val _locked = MutableStateFlow(true)
    val locked: StateFlow<Boolean> = _locked

    val lockDelay: LockDelay
        get() = runBlocking {
            LockDelay.fromOrdinal(context.lockPrefs.data.first()[delayKey] ?: 0)
        }

    fun setLockDelay(delay: LockDelay) = runBlocking {
        context.lockPrefs.edit { it[delayKey] = delay.ordinal }
    }

    fun hasPin(): Boolean = runBlocking {
        context.lockPrefs.data.first()[pinHashKey] != null
    }

    fun setPin(pin: String) = runBlocking {
        val h = pinHasher.hash(pin)
        context.lockPrefs.edit {
            it[pinHashKey] = Base64.encodeToString(h.hash, Base64.NO_WRAP)
            it[pinSaltKey] = Base64.encodeToString(h.salt, Base64.NO_WRAP)
        }
    }

    /** Verifies PIN, applies lockout policy. Returns true on success. */
    fun unlock(pin: String): Boolean = runBlocking {
        val prefs = context.lockPrefs.data.first()
        val hashB64 = prefs[pinHashKey] ?: return@runBlocking false
        val saltB64 = prefs[pinSaltKey] ?: return@runBlocking false
        val stored = com.mechat.app.crypto.PinHasher.PinHash(
            hash = Base64.decode(hashB64, Base64.NO_WRAP),
            salt = Base64.decode(saltB64, Base64.NO_WRAP),
        )
        var state = LockoutPolicy.State(
            failures = prefs[failuresKey] ?: 0,
            lockoutUntil = prefs[lockoutUntilKey] ?: 0L,
        )
        val now = System.currentTimeMillis()
        if (LockoutPolicy.remainingLockout(state, now) > 0L) return@runBlocking false

        if (pinHasher.verify(pin, stored)) {
            context.lockPrefs.edit {
                it[failuresKey] = 0
                it[lockoutUntilKey] = 0L
            }
            _locked.value = false
            true
        } else {
            state = LockoutPolicy.onFailure(state, now)
            context.lockPrefs.edit {
                it[failuresKey] = state.failures
                it[lockoutUntilKey] = state.lockoutUntil
            }
            false
        }
    }

    fun lock() { _locked.value = true }

    // ---- Panic PIN (must differ from the main PIN; same Argon2id scheme) ----

    fun hasPanicPin(): Boolean = runBlocking {
        context.lockPrefs.data.first()[panicHashKey] != null
    }

    /** Sets the panic PIN. Returns false if it equals the main PIN. */
    fun setPanicPin(pin: String): Boolean = runBlocking {
        val prefs = context.lockPrefs.data.first()
        val mainHash = prefs[pinHashKey]?.let { h ->
            com.mechat.app.crypto.PinHasher.PinHash(
                Base64.decode(h, Base64.NO_WRAP),
                Base64.decode(prefs[pinSaltKey]!!, Base64.NO_WRAP),
            )
        }
        if (mainHash != null && pinHasher.verify(pin, mainHash)) return@runBlocking false
        val h = pinHasher.hash(pin)
        context.lockPrefs.edit {
            it[panicHashKey] = Base64.encodeToString(h.hash, Base64.NO_WRAP)
            it[panicSaltKey] = Base64.encodeToString(h.salt, Base64.NO_WRAP)
        }
        true
    }

    /** True if `pin` is the panic PIN (constant-time Argon2id verify). */
    fun isPanicPin(pin: String): Boolean = runBlocking {
        val prefs = context.lockPrefs.data.first()
        val hashB64 = prefs[panicHashKey] ?: return@runBlocking false
        val saltB64 = prefs[panicSaltKey] ?: return@runBlocking false
        pinHasher.verify(
            pin,
            com.mechat.app.crypto.PinHasher.PinHash(
                Base64.decode(hashB64, Base64.NO_WRAP),
                Base64.decode(saltB64, Base64.NO_WRAP),
            ),
        )
    }

    /** Auto-wipe after 10 failed attempts (default ON). */
    fun isAutoWipeEnabled(): Boolean = runBlocking {
        context.lockPrefs.data.first()[autoWipeKey] ?: true
    }

    fun setAutoWipeEnabled(enabled: Boolean) = runBlocking {
        context.lockPrefs.edit { it[autoWipeKey] = enabled }
    }

    /** Called on each failed attempt; returns true if the auto-wipe threshold was hit. */
    fun onFailedAttemptAutoWipe(): Boolean = runBlocking {
        val prefs = context.lockPrefs.data.first()
        if (prefs[autoWipeKey] == false) return@runBlocking false
        (prefs[failuresKey] ?: 0) >= AUTO_WIPE_THRESHOLD
    }

    /** Remaining lockout in ms for UI countdown, 0 if not locked out. */
    fun remainingLockoutMs(): Long = runBlocking {
        val prefs = context.lockPrefs.data.first()
        LockoutPolicy.remainingLockout(
            LockoutPolicy.State(prefs[failuresKey] ?: 0, prefs[lockoutUntilKey] ?: 0L),
            System.currentTimeMillis(),
        )
    }
}
