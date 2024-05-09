package io.kosc.core.test.serialization

import io.kosc.core.serialization.OSCSerializer
import io.kosc.core.spec.OSCBundle
import io.kosc.core.spec.OSCMessage
import io.kosc.core.spec.OSCPacket
import io.kosc.core.spec.args.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer


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
