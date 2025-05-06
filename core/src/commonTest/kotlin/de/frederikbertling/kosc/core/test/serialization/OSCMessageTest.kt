package de.frederikbertling.kosc.core.test.serialization

import de.frederikbertling.kosc.core.serialization.OSCSerializer
import de.frederikbertling.kosc.core.spec.OSCMessage
import de.frederikbertling.kosc.core.spec.OSCPacket
import de.frederikbertling.kosc.core.spec.args.OSCBlob
import de.frederikbertling.kosc.core.spec.args.OSCFloat32
import de.frederikbertling.kosc.core.spec.args.OSCInt32
import de.frederikbertling.kosc.core.spec.args.OSCString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.io.Buffer
import kotlin.test.Test


class OSCMessageTest {

    private val msgRawData = listOf(
        // empty message
        OSCMessage("/empty") to byteArrayOf(47, 101, 109, 112, 116, 121, 0, 0, 44, 0, 0, 0),
        // FillerBeforeCommaNone
        OSCMessage("/abcdef") to byteArrayOf(47, 97, 98, 99, 100, 101, 102, 0, 44, 0, 0, 0),
        // FillerBeforeCommaOne
        OSCMessage("/abcde") to byteArrayOf(47, 97, 98, 99, 100, 101, 0, 0, 44, 0, 0, 0),
        // FillerBeforeCommaTwo
        OSCMessage("/abcd") to byteArrayOf(47, 97, 98, 99, 100, 0, 0, 0, 44, 0, 0, 0),
        // FillerBeforeCommaThree
        OSCMessage("/abcdefg") to byteArrayOf(47, 97, 98, 99, 100, 101, 102, 103, 0, 0, 0, 0, 44, 0, 0, 0),
        // ArgumentInteger
        OSCMessage("/int", OSCInt32(99)) to byteArrayOf(47, 105, 110, 116, 0, 0, 0, 0, 44, 105, 0, 0, 0, 0, 0, 99),
        // ArgumentFloat
        OSCMessage("/float", OSCFloat32(999.9f)) to byteArrayOf(47, 102, 108, 111, 97, 116, 0, 0, 44, 102, 0, 0, 68, 121, -7, -102),
    )

    private val msgData = listOf(
        // edge cases
        OSCMessage("/1234", OSCFloat32(Float.MIN_VALUE)),
        OSCMessage("/1234", OSCFloat32(Float.MAX_VALUE)),
        OSCMessage("/1234", OSCInt32(Int.MIN_VALUE)),
        OSCMessage("/1234", OSCInt32(Int.MAX_VALUE)),
        OSCMessage(
            address = "/sfbi",
            arguments = listOf(
                OSCString("string"),
                OSCFloat32(0.999f),
                OSCBlob(byteArrayOf(-1, 0, 1)),
                OSCInt32(4129312)
            )
        )
    )

    @Test
    fun testOscMessage() = runTest {
        msgRawData.forEach { (message, stream) ->
            val serializationResult = OSCSerializer.serialize(message)
            val deserializationResult = OSCSerializer.deserialize(stream)

            serializationResult shouldBe stream
            deserializationResult shouldBe message
        }

        msgData.forEach { message ->
            val serializationResult = OSCSerializer.serialize(message)
            val deserializationResult = OSCSerializer.deserialize(serializationResult)

            deserializationResult shouldBe message
        }
    }

    private fun OSCSerializer.deserialize(byteArray: ByteArray): OSCPacket {
        val buffer = Buffer()
        buffer.write(byteArray)
        return deserialize(buffer)
    }

}
