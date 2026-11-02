package com.mechat.app.network.webrtc

import android.content.Context
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SessionDescription
import java.nio.ByteBuffer

/** WebRTC peer connection with a reliable ordered DataChannel for messages. */
class PeerConnectionClient(
    context: Context,
    private val listener: Listener,
) {
    interface Listener {
        fun onSignal(description: SessionDescription)      // send via signalling
        fun onIceCandidate(candidate: IceCandidate)         // send via signalling
        fun onDataChannelMessage(text: String)
        fun onRouteChanged(route: Route)
        fun onHealthCheck(rttMs: Long)
    }

    enum class Route { DIRECT, RELAYED, MAILBOX }

    private val factory: PeerConnectionFactory
    private var pc: PeerConnection? = null
    private var channel: DataChannel? = null
    private var lastPing = 0L

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context).createInitializationOptions(),
        )
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
    }

    fun connect() {
        pc = factory.createPeerConnection(
            PeerConnection.RTCConfiguration(IceServers.build()).apply {
                iceTransportsType = PeerConnection.IceTransportsType.ALL
            },
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) = listener.onIceCandidate(candidate)
                override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                    when (newState) {
                        PeerConnection.PeerConnectionState.CONNECTED -> listener.onRouteChanged(route())
                        PeerConnection.PeerConnectionState.DISCONNECTED,
                        PeerConnection.PeerConnectionState.FAILED -> listener.onRouteChanged(Route.MAILBOX) // fall back
                        else -> {}
                    }
                }
                override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState) {}
                override fun onSignalingChange(newState: PeerConnection.SignalingState) {}
                override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState) {}
                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) {}
                override fun onAddStream(stream: MediaStream) {}
                override fun onRemoveStream(stream: MediaStream) {}
                override fun onDataChannel(dc: DataChannel) { wireChannel(dc) }
                override fun onRenegotiationNeeded() {}
                override fun onAddTrack(receiver: org.webrtc.RtpReceiver, streams: Array<out MediaStream>) {}
            },
        )
        // Reliable, ordered data channel for chat messages.
        channel = pc!!.createDataChannel("messages", DataChannel.Init().apply { ordered = true })
        wireChannel(channel!!)
    }

    private fun wireChannel(dc: DataChannel) {
        dc.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(previousAmount: Long) {}
            override fun onStateChange() {
                if (dc.state() == DataChannel.State.OPEN) listener.onRouteChanged(route())
            }
            override fun onMessage(buffer: DataChannel.Buffer) {
                val bytes = ByteArray(buffer.data.remaining()).also { buffer.data.get(it) }
                val text = String(bytes)
                if (text.startsWith("PING:")) {
                    send("PONG:" + text.removePrefix("PING:"))
                } else if (text.startsWith("PONG:")) {
                    listener.onHealthCheck(System.currentTimeMillis() - lastPing)
                } else listener.onDataChannelMessage(text)
            }
        })
    }

    fun createOffer() {
        pc?.createOffer(object : org.webrtc.SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription) {
                pc?.setLocalDescription(object : org.webrtc.SdpObserver {
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onSetSuccess() { listener.onSignal(desc) }
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, org.webrtc.MediaConstraints())
    }

    fun handleSignal(description: SessionDescription) {
        pc?.setRemoteDescription(object : org.webrtc.SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                if (description.type == SessionDescription.Type.OFFER) {
                    pc?.createAnswer(object : org.webrtc.SdpObserver {
                        override fun onCreateSuccess(desc: SessionDescription) {
                            pc?.setLocalDescription(object : org.webrtc.SdpObserver {
                                override fun onCreateSuccess(p0: SessionDescription?) {}
                                override fun onSetSuccess() { listener.onSignal(desc) }
                                override fun onCreateFailure(p0: String?) {}
                                override fun onSetFailure(p0: String?) {}
                            }, desc)
                        }
                        override fun onSetSuccess() {}
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, org.webrtc.MediaConstraints())
                }
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, description)
    }

    fun addIceCandidate(candidate: IceCandidate) = pc?.addIceCandidate(candidate)

    /** ICE restart for recovery. */
    fun restartIce() {
        pc?.restartIce()
    }

    fun send(text: String) {
        val data = ByteBuffer.wrap(text.toByteArray())
        channel?.send(DataChannel.Buffer(data, false))
    }

    /** Health check ping/pong over the data channel. */
    fun ping() {
        lastPing = System.currentTimeMillis()
        send("PING:$lastPing")
    }

    private fun route(): Route {
        val c = pc?.iceConnectionState()
        return if (c == PeerConnection.IceConnectionState.CONNECTED)
            Route.DIRECT // TURN-relayed connections also land here; UI is honest via route label
        else Route.RELAYED
    }

    fun disconnect() {
        runCatching { channel?.close() }
        runCatching { pc?.close() }
        channel = null
        pc = null
    }
}
