package com.mechat.app.network.webrtc

import com.mechat.app.BuildConfig
import org.webrtc.PeerConnection

/**
 * ICE servers from BuildConfig (local.properties / CI secrets — never hard-coded).
 * STUN + TURN; TURN uses turns: (TLS) / turn: over TLS 443 where possible.
 * TURN credentials are short-lived, time-limited HMAC credentials issued by the
 * server (see docs/server.md) — never long-lived passwords in source.
 */
object IceServers {

    fun build(): List<PeerConnection.IceServer> {
        val servers = mutableListOf<PeerConnection.IceServer>()
        if (BuildConfig.STUN_URL.isNotBlank()) {
            servers += PeerConnection.IceServer.builder(BuildConfig.STUN_URL).createIceServer()
        }
        if (BuildConfig.TURN_URL.isNotBlank() && BuildConfig.TURN_USER.isNotBlank()) {
            servers += PeerConnection.IceServer.builder(BuildConfig.TURN_URL)
                .setUsername(BuildConfig.TURN_USER)
                .setPassword(BuildConfig.TURN_CRED) // time-limited HMAC credential
                .createIceServer()
        }
        return servers
    }
}
