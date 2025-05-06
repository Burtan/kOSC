package de.frederikbertling.kosc.udp

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.transport.OSCClient
import de.frederikbertling.kosc.core.transport.OSCServer
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.ConnectedDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

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
        localAddress = InetSocketAddress("127.0.0.1", portIn),
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

    private var clientSocketFlow = MutableStateFlow<ConnectedDatagramSocket?>(null)
    private var serverSocketFlow = MutableStateFlow<BoundDatagramSocket?>(null)
    private val _packetFlow = MutableSharedFlow<OSCPacket>()
    private val _errorFlow = MutableSharedFlow<Throwable>()
    private val isClient = remoteAddress != null
    override val packetFlow = _packetFlow.asSharedFlow()
    override val errorFlow = _errorFlow.asSharedFlow()

    init {
        scope.launch {
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
                clientSocketFlow.emit(socket)
            else if (socket is BoundDatagramSocket)
                serverSocketFlow.emit(socket)

            while (!socket.isClosed) {
                try {
                    val datagram = socket
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

    override fun send(packet: OSCPacket) {
        if (!isClient)
            throw IllegalArgumentException(
                "$this is not configured to be an OSC client. Add a remote address."
            )

        scope.launch {
            val clientSocket = clientSocketFlow.filterNotNull().first()
            try {
                val data = OSCSerializer.serialize(packet)
                val datagram = Datagram(
                    packet = Buffer()
                        .apply {
                            write(data)
                        },
                    address = clientSocket.remoteAddress
                )
                clientSocket.send(datagram)
            } catch (e: Throwable) {
                _errorFlow.emit(e)
            }
        }
    }

    fun close() {
        scope.launch {
            val clientSocket = clientSocketFlow.filterNotNull().first()
            clientSocket.close()
        }
        scope.launch {
            val serverSocket = serverSocketFlow.filterNotNull().first()
            serverSocket.close()
        }
    }
}
