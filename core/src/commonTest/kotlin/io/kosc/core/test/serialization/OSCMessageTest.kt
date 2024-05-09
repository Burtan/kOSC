package io.kosc.core.test.serialization

import io.kosc.core.serialization.OSCSerializer
import io.kosc.core.spec.OSCMessage
import io.kosc.core.spec.OSCPacket
import io.kosc.core.spec.args.OSCFloat32
import io.kosc.core.spec.args.OSCInt32
import io.kotest.core.spec.style.StringSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import kotlinx.io.Buffer


class OSCMessageTest : StringSpec() {

    private val data = listOf(
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

    init {
        withData(data) { (message, stream) ->
            OSCSerializer.serialize(message) shouldBe stream
            OSCSerializer.deserialize(stream) shouldBe message
        }
    }

    private fun OSCSerializer.deserialize(byteArray: ByteArray): OSCPacket {
        val buffer = Buffer()
        buffer.write(byteArray)
        return deserialize(buffer)
    }

}
