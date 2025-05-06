package de.frederikbertling.kosc.udp.test

import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCFloat32
import de.frederikbertling.kosc.udp.OSCUDPSocket
import io.ktor.network.sockets.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals

class OSCUDPSocketTest {

    private val testPacket = OSCMessage("/test", OSCFloat32(1.93127f))

    @Test
    fun testOscUdpSocket1() = runTest {
        val port = Random.nextInt(8080..8090)
        val listener = OSCUDPSocket(port)
        val client = OSCUDPSocket("127.0.0.1", port)
        testClient(client, listener)
        listener.close()
        client.close()
    }

    @Test
    fun testOscUdpSocket2() = runTest {
        val port = Random.nextInt(8080..8090)
        val listener = OSCUDPSocket(port)
        val client = OSCUDPSocket(InetSocketAddress("127.0.0.1", port))
        testClient(client, listener)
        listener.close()
        client.close()
    }

    @Test
    fun testOscUdpSocket3() = runTest {
        val port = Random.nextInt(8080..8090)
        val listenerClient = OSCUDPSocket("127.0.0.1", port, port)
        testClient(listenerClient, listenerClient)
        listenerClient.close()
    }

    @Test
    fun testOscUdpSocket4() = runTest {
        val port = Random.nextInt(8080..8090)
        val listenerClient = OSCUDPSocket(
            localAddress = InetSocketAddress("127.0.0.1", port),
            remoteAddress = InetSocketAddress("127.0.0.1", port)
        )
        testClient(listenerClient, listenerClient)
        listenerClient.close()
    }
    
    private suspend fun testClient(client: OSCUDPSocket, listener: OSCUDPSocket) = coroutineScope {
        val packets = mutableListOf<OSCPacket>()

        launch {
            listener
                .packetFlow
                .take(5)
                .toCollection(packets)

            packets.forEach {
                assertEquals(testPacket, it)
            }
            assertEquals(5, packets.size)
        }

        launch {
            // some udp packets are allowed to fail
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
        }
    }

}
