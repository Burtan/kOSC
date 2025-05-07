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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlinx.io.readByteArray


class OSCUDPSocket private constructor(
    localAddress: SocketAddress?,
    remoteAddress: SocketAddress?,
    private val scope: CoroutineScope,
    bufferCapacity: Int = 10,
) : OSCClient, OSCServer {

    // constructor for sockets that send and receive.
    constructor(
        remoteAddress: SocketAddress,
        portIn: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        bufferCapacity: Int = 10,
    ) : this(
        localAddress = InetSocketAddress("127.0.0.1", portIn),
        remoteAddress = remoteAddress,
        scope = scope,
        bufferCapacity = bufferCapacity
    )

    // constructor for sockets that only send
    constructor(
        remoteAddress: SocketAddress,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        bufferCapacity: Int = 10,
    ) : this(null, remoteAddress, scope, bufferCapacity)

    // constructor for sockets that only receive
    constructor(
        portIn: Int,
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        bufferCapacity: Int = 10,
    ) : this(InetSocketAddress("localhost", portIn), null, scope, bufferCapacity)

    private val isClient = remoteAddress != null
    private var clientSocketFlow = MutableStateFlow<ConnectedDatagramSocket?>(null)
    private var serverSocketFlow = MutableStateFlow<BoundDatagramSocket?>(null)
    private val _packetFlow = MutableSharedFlow<OSCPacket>(0, bufferCapacity, BufferOverflow.SUSPEND)
    private val _errorFlow = MutableSharedFlow<Throwable>()
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

            // ConnectedDatagramSocket sends and receives
            if (socket is ConnectedDatagramSocket)
                clientSocketFlow.emit(socket)
            // BoundDatagramSocket only receives
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
