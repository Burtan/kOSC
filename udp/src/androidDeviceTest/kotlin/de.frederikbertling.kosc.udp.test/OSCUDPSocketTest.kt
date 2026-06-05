package de.frederikbertling.kosc.udp.test

import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCFloat32
import de.frederikbertling.kosc.udp.OSCUDPClient
import de.frederikbertling.kosc.udp.OSCUDPServer
import io.ktor.network.sockets.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
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
        val listener = OSCUDPServer(
            port = port
        )
        val client = OSCUDPClient(
            remoteAddress = InetSocketAddress("127.0.0.1", port)
        )
        testClient(client, listener)
        listener.close()
        client.close()
    }

    @Test
    fun testOscUdpSocket2() = runTest {
        // test error on initiation

        // invalid in port
        assertFails {
            OSCUDPServer(
                port = 100000
            )
        }

        // invalid out port
        assertFails {
            OSCUDPClient(
                remoteAddress = InetSocketAddress("127.0.0.1", 100000),
            )
        }

        // invalid out host
        assertFails {
            OSCUDPClient(
                remoteAddress = InetSocketAddress("fdskjbsad", 8008),
            )
        }
    }

    private suspend fun testClient(client: OSCUDPClient, listener: OSCUDPServer) = coroutineScope {
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
