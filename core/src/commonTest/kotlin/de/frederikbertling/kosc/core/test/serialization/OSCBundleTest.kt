package de.frederikbertling.kosc.core.test.serialization

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCBundle
import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.io.*


class OSCBundleTest : StringSpec() {

    private val bundleData = listOf(
        OSCBundle(
            time = OSCTimeTag(131231L),
            packets = listOf(
                OSCMessage(
                    address = "/msgstring",
                    argument = OSCString("bundletest")
                )
            )
        ),
        OSCBundle(
            time = OSCTimeTag(131231L),
            packets = listOf(
                OSCMessage(
                    address = "/msgstring",
                    argument = OSCString("bundletest")
                ),
                OSCMessage(
                    address = "/msgint",
                    argument = OSCInt32(12305123)
                ),
                OSCMessage(
                    address = "/msgblob",
                    argument = OSCBlob(byteArrayOf(-1, 0, 1))
                )
            )
        ),
        OSCBundle(
            time = OSCTimeTag(131231L),
            packets = listOf(
                OSCMessage(
                    address = "/msgstring",
                    argument = OSCString("bundletest")
                ),
                OSCBundle(
                    time = OSCTimeTag(131231L),
                    packets = listOf(
                        OSCMessage(
                            address = "/msgstring",
                            argument = OSCString("bundletest")
                        ),
                        OSCMessage(
                            address = "/msgint",
                            argument = OSCInt32(12305123)
                        ),
                        OSCMessage(
                            address = "/msgblob",
                            argument = OSCBlob(byteArrayOf(-1, 0, 1))
                        )
                    )
                ),
                OSCMessage(
                    address = "/msgblob",
                    argument = OSCBlob(byteArrayOf(-1, 0, 1))
                )
            )
        )
    )

    init {
        withData(bundleData) { bundle ->
            val serializationResult = OSCSerializer.serialize(bundle)
            val deserializationResult = OSCSerializer.deserialize(serializationResult)
            deserializationResult shouldBe bundle
        }
    }

    private fun OSCSerializer.deserialize(byteArray: ByteArray): OSCPacket {
        val buffer = Buffer()
        buffer.write(byteArray)
        return deserialize(buffer)
    }

}
