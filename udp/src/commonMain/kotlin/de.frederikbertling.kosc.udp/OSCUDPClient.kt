package de.frederikbertling.kosc.udp

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.transport.OSCClient
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer


/**
 * OSCUDPClient connects to sockets synchronously. Constructors should only be used in a
 * suspending thread.
 */
class OSCUDPClient(
    val remoteAddress: SocketAddress,
) : OSCClient {

    private var clientSocket: ConnectedDatagramSocket? = null

    init {
        clientSocket = runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)

            aSocket(selectorManager)
                .udp()
                .connect(remoteAddress)
        }
    }

    override suspend fun send(packet: OSCPacket) {
        val data = OSCSerializer.serialize(packet)
        val datagram = Datagram(
            packet = Buffer()
                .apply {
                    write(data)
                },
            address = clientSocket!!.remoteAddress
        )
        clientSocket!!.send(datagram)
    }

    override fun close() {
        clientSocket!!.close()
    }

}
