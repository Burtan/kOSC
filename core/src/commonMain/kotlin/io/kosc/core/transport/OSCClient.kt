package io.kosc.core.transport

import io.kosc.core.spec.OSCPacket

/**
 * The unit of transmission of OSC is an OSC Packet. Any application that sends OSC Packets is an OSC Client
 */
interface OSCClient {
    fun send(packet: OSCPacket)
}
