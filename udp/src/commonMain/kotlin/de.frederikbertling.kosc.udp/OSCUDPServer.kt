package de.frederikbertling.kosc.udp

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.transport.OSCServer
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.io.Buffer
import kotlinx.io.readByteArray


/**
 * OSCUDPServer connects to sockets synchronously. Constructors should only be used in a
 * suspending thread.
 */
class OSCUDPServer(
    port: Int,
    scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    bufferCapacity: Int = 10,
) : OSCServer {

    private var socket: BoundDatagramSocket? = null
    private val _packetFlow = MutableSharedFlow<OSCPacket>(
        0,
        bufferCapacity,
        BufferOverflow.SUSPEND
    )
    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val packetFlow = _packetFlow.asSharedFlow()
    override val errorFlow = _errorFlow.asSharedFlow()

    init {
        socket = runBlocking {
            val selectorManager = SelectorManager(Dispatchers.IO)

            aSocket(selectorManager)
                .udp()
                .bind(port = port)

        }

        scope.launch {
            while (!socket!!.isClosed) {
                try {
                    val datagram = socket!!
                        .receive()

                    val oscPacket = datagram
                        .packet
                        .use {
                            val buffer = Buffer()
                            buffer.write(it.readByteArray())
                            OSCSerializer.deserialize(buffer)
                        }

                    _packetFlow.emit(oscPacket)
                } catch (e: Throwable) {
                    _errorFlow.emit(e)
                }
            }
        }
    }

    override fun close() {
        socket?.close()
    }

}
