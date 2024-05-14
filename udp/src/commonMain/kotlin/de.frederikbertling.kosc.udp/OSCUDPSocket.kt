package de.frederikbertling.kosc.udp

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.transport.OSCClient
import de.frederikbertling.kosc.core.transport.OSCServer
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.io.Buffer

@Suppress("unused")
class OSCUDPSocket private constructor(
    localAddress: SocketAddress?,
    remoteAddress: SocketAddress?,
    // noCycle is only used to make the private constructor different from public constructors
    @Suppress("UNUSED_PARAMETER")
    noCycle: Int? = null,
    private val scope: CoroutineScope
) : OSCClient, OSCServer {
    
    constructor(
        localAddress: SocketAddress,
        remoteAddress: SocketAddress,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) : this(localAddress, remoteAddress, null, scope)

    constructor(
        externalHost: String,
        portIn: Int,
        portOut: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) : this(
        localAddress = InetSocketAddress("localhost", portIn),
        remoteAddress = InetSocketAddress(externalHost, portOut),
        noCycle = null,
        scope = scope
    )

    constructor(
        remoteAddress: SocketAddress,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) : this(null, remoteAddress, null, scope)

    constructor(
        externalHost: String,
        portOut: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) : this(null, InetSocketAddress(externalHost, portOut), null, scope)

    constructor(
        portIn: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    ) : this(InetSocketAddress("localhost", portIn), null, null, scope)

    private var clientSocket: ConnectedDatagramSocket? = null
    private var serverSocket: BoundDatagramSocket? = null
    private val _packetFlow = MutableSharedFlow<OSCPacket>()
    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val packetFlow = _packetFlow.asSharedFlow()
    override val errorFlow = _errorFlow.asSharedFlow()

    init {
        val selectorManager = SelectorManager(Dispatchers.IO)
        val socket = if (localAddress != null && remoteAddress != null) {
            // server and client
            aSocket(selectorManager)
                .udp()
                .connect(
                    localAddress = localAddress,
                    remoteAddress = remoteAddress,
                )
        } else if (remoteAddress != null) {
            // only client
            aSocket(selectorManager)
                .udp()
                .connect(
                    remoteAddress = remoteAddress
                )
        } else {
            // only server
            aSocket(selectorManager)
                .udp()
                .bind(localAddress)
        }

        if (socket is ConnectedDatagramSocket)
            clientSocket = socket
        else if (socket is BoundDatagramSocket)
            serverSocket = socket

        scope.launch {
            while (!socket.isClosed) {
                try {
                    val datagram = socket
                        .receive()

                    val oscPacket = datagram
                        .packet
                        .use {
                            val buffer = Buffer()
                            buffer.write(it.readBytes())
                            OSCSerializer.deserialize(buffer)
                        }

                    _packetFlow.emit(oscPacket)
                } catch (e: Throwable) {
                    _errorFlow.emit(e)
                }
            }
        }
    }

    override fun send(packet: OSCPacket) {
        scope.launch {
            clientSocket?.let {
                try {
                    val data = OSCSerializer.serialize(packet)
                    val datagram = Datagram(
                        packet = ByteReadPacket(data),
                        address = it.remoteAddress
                    )
                    it.send(datagram)
                } catch (e: Throwable) {
                    _errorFlow.emit(e)
                }
            }
        }
    }

    fun close() {
        clientSocket?.close()
        serverSocket?.close()
    }
}
