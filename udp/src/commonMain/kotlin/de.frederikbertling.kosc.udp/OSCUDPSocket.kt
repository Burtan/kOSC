package de.frederikbertling.kosc.udp

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.transport.OSCClient
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
 * OSCUDPSocket connects to sockets synchronously. Constructors should only be used in a
 * suspending thread.
 */
class OSCUDPSocket(
    localAddress: SocketAddress?,
    remoteAddress: SocketAddress?,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
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
    ) : this(
        localAddress = InetSocketAddress("127.0.0.1", portIn),
        remoteAddress = null,
        scope=  scope,
        bufferCapacity = bufferCapacity
    )

    private val isClient = remoteAddress != null
    private var clientSocket: ConnectedDatagramSocket? = null
    private var serverSocket: BoundDatagramSocket? = null
    private val _packetFlow = MutableSharedFlow<OSCPacket>(
        0,
        bufferCapacity,
        BufferOverflow.SUSPEND
    )
    private val _errorFlow = MutableSharedFlow<Throwable>()
    override val packetFlow = _packetFlow.asSharedFlow()
    override val errorFlow = _errorFlow.asSharedFlow()

    init {
        val socket = runBlocking {
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
                clientSocket = socket
            // BoundDatagramSocket only receives
            else if (socket is BoundDatagramSocket)
                serverSocket = socket

            socket
        }

        scope.launch {
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
            try {
                val data = OSCSerializer.serialize(packet)
                val datagram = Datagram(
                    packet = Buffer()
                        .apply {
                            write(data)
                        },
                    address = clientSocket!!.remoteAddress
                )
                clientSocket!!.send(datagram)
            } catch (e: Throwable) {
                _errorFlow.emit(e)
            }
        }
    }

    fun close() {
        scope.launch {
            clientSocket?.close()
        }
        scope.launch {
            serverSocket?.close()
        }
    }
}
