package de.frederikbertling.kosc.udp.test

import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCFloat32
import de.frederikbertling.kosc.udp.OSCUDPSocket
import io.ktor.network.sockets.InetSocketAddress
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class OSCUDPSocketTest {

    private val testPacket = OSCMessage("/test", OSCFloat32(1.93127f))

    @Test
    fun testOscUdpSocket1() = runTest {
        // test sockets with only receiving and only sending functions
        val port = Random.nextInt(8080..8090)
        val listener = OSCUDPSocket(
            portIn = port
        )
        val client = OSCUDPSocket(
            remoteAddress = InetSocketAddress("127.0.0.1", port)
        )
        testClient(client, listener)
        listener.close()
        client.close()
    }

    @Test
    fun testOscUdpSocket2() = runTest {
        // test sockets with receiving and sending functions
        val port = Random.nextInt(8080..8090)
        val listenerClient = OSCUDPSocket(
            remoteAddress = InetSocketAddress("127.0.0.1", port),
            portIn = port
        )
        testClient(listenerClient, listenerClient)
        listenerClient.close()
    }

    @Test
    fun testOscUdpSocket3() = runTest {
        // test error when trying to send from only receiving sockets
        val port = Random.nextInt(8080..8090)
        val listenerClient = OSCUDPSocket(
            portIn = port
        )
        assertFails {
            testClient(listenerClient, listenerClient)
        }
        listenerClient.close()
    }
    
    private suspend fun testClient(client: OSCUDPSocket, listener: OSCUDPSocket) = coroutineScope {
        val packets = mutableListOf<OSCPacket>()

        val isCollectingFlow = MutableStateFlow(false)

        launch {
            listener
                .packetFlow
                .onSubscription {
                    isCollectingFlow.emit(true)
                }
                .take(5)
                .toCollection(packets)

            packets.forEach {
                assertEquals(testPacket, it)
            }
            assertEquals(5, packets.size)
        }

        // await collecting
        isCollectingFlow.filter { it }.first()

        launch {
            // some udp packets are allowed to fail
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
            client.send(testPacket)
        }
    }

}
