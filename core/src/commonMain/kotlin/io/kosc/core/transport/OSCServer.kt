package io.kosc.core.transport

import io.kosc.core.spec.OSCPacket
import kotlinx.coroutines.flow.SharedFlow

/**
 * The unit of transmission of OSC is an OSC Packet. Any application that receives OSC Packets is an OSC Server.
 */
interface OSCServer {
    val packetFlow: SharedFlow<OSCPacket>
    val errorsFlow: SharedFlow<Throwable>
}
