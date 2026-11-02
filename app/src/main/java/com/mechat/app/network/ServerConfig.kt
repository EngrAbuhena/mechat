package com.mechat.app.network

/**
 * Server configuration. The user can point MeChat at their own self-hosted server
 * in Settings; default is empty until configured (env-style, nothing hard-coded).
 *
 * Certificate pinning: pin the SHA-256 of the server certificate's public key
 * ("sha256/..."), primary + backup. Pins are public values (not secrets), but must
 * be set per deployment — placeholders below must be replaced before release.
 */
data class ServerConfig(
    val wssUrl: String, // e.g. wss://relay.example.org/ws — must be wss://
    val pins: List<String> = emptyList(), // sha256/<base64 pubkey hash>, primary + backup
) {
    init {
        require(wssUrl.startsWith("wss://")) { "Only wss:// URLs are allowed — no cleartext" }
    }
}
