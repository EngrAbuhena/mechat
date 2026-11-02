package com.mechat.app.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

/**
 * WebSocket connection manager: wss:// only, certificate pinning (primary+backup),
 * exponential backoff with automatic reconnect on network change. No connectivity
 * probes to Google or anyone — we only react to system network callbacks.
 */
@Singleton
class ConnectionManager @Inject constructor(
    private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _state = MutableStateFlow(ConnectionState.DISCONNECTED)
    val state: StateFlow<ConnectionState> = _state

    private var ws: WebSocket? = null
    private var attempt = 0
    private var config: ServerConfig? = null
    private var running = false

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
        // Certificate pinning applied per-config in connect().
        builder.build()
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { if (running) reconnect() }
        override fun onLost(network: Network) {
            _state.value = ConnectionState.DISCONNECTED
        }
    }

    fun start(cfg: ServerConfig) {
        config = cfg
        running = true
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.registerNetworkCallback(NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), networkCallback)
        connect()
    }

    fun stop() {
        running = false
        runCatching {
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                .unregisterNetworkCallback(networkCallback)
        }
        ws?.close(1000, "bye")
        ws = null
        _state.value = ConnectionState.DISCONNECTED
    }

    private fun connect() {
        val cfg = config ?: return
        _state.value = ConnectionState.CONNECTING
        val pinner = okhttp3.CertificatePinner.Builder()
            .apply { cfg.pins.forEach { add(cfg.wssUrl.removePrefix("wss://").substringBefore("/"), it) } }
            .build()
        val request = Request.Builder().url(cfg.wssUrl).build()
        ws = client.newBuilder().certificatePinner(pinner).build()
            .newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    attempt = 0
                    _state.value = ConnectionState.CONNECTED
                }
                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    _state.value = ConnectionState.DISCONNECTED
                    scheduleReconnect()
                }
                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    _state.value = ConnectionState.DISCONNECTED
                    scheduleReconnect()
                }
            })
    }

    private fun reconnect() {
        ws?.close(1000, "reconnect")
        connect()
    }

    private fun scheduleReconnect() {
        if (!running) return
        scope.launch {
            delay(Backoff.delayMs(attempt++))
            if (running) connect()
        }
    }
}
