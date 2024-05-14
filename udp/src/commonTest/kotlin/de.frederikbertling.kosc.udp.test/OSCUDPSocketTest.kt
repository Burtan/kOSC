package de.frederikbertling.kosc.udp.test

import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCFloat32
import de.frederikbertling.kosc.udp.OSCUDPSocket
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.ktor.network.sockets.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.random.nextInt

class OSCUDPSocketTest : StringSpec() {

    private val testPacket = OSCMessage("/test", OSCFloat32(1.93127f))

    init {
        "OSCUDPSocket test #1" {
            val port = Random.nextInt(8080..8090)
            val listener = OSCUDPSocket(port)
            val client = OSCUDPSocket("localhost", port)
            testClient(client, listener)
            listener.close()
            client.close()
        }

        "OSCUDPSocket test #2" {
            val port = Random.nextInt(8080..8090)
            val listener = OSCUDPSocket(port)
            val client = OSCUDPSocket(InetSocketAddress("localhost", port))
            testClient(client, listener)
            listener.close()
            client.close()
        }

        "OSCUDPSocket test #3" {
            val port = Random.nextInt(8080..8090)
            val listenerClient = OSCUDPSocket("localhost", port, port)
            testClient(listenerClient, listenerClient)
            listenerClient.close()
        }

        "OSCUDPSocket test #4" {
            val port = Random.nextInt(8080..8090)
            val listenerClient = OSCUDPSocket(
                localAddress = InetSocketAddress("localhost", port),
                remoteAddress = InetSocketAddress("localhost", port)
            )
            testClient(listenerClient, listenerClient)
            listenerClient.close()
        }
    }
    
    private suspend fun testClient(client: OSCUDPSocket, listener: OSCUDPSocket) = coroutineScope {
        val packets = mutableListOf<OSCPacket>()

        launch {
            listener
                .packetFlow
                .take(5)
                .toCollection(packets)

            packets.forEach {
                it shouldBe testPacket
            }
            packets.size shouldBe 5
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
