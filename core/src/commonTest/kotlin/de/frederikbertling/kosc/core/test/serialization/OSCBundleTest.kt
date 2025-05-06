package de.frederikbertling.kosc.core.test.serialization

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCBundle
import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCBlob
import de.frederikbertling.kosc.core.spec.args.OSCInt32
import de.frederikbertling.kosc.core.spec.args.OSCString
import de.frederikbertling.kosc.core.spec.args.OSCTimeTag
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlin.test.Test
import kotlin.test.assertEquals


class OSCBundleTest {

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

    @Test
    fun testOscBundle() = runTest {
        bundleData.forEach { bundle ->
            val serializationResult = OSCSerializer.serialize(bundle)
            val deserializationResult = OSCSerializer.deserialize(serializationResult)

            assertEquals(bundle, deserializationResult)
        }
    }

    private fun OSCSerializer.deserialize(byteArray: ByteArray): OSCPacket {
        val buffer = Buffer()
        buffer.write(byteArray)
        return deserialize(buffer)
    }

}
