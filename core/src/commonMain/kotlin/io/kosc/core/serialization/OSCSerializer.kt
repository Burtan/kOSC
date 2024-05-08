package io.kosc.core.serialization

import io.kosc.core.spec.OSCAddressPattern
import io.kosc.core.spec.OSCBundle
import io.kosc.core.spec.OSCMessage
import io.kosc.core.spec.OSCPacket
import io.kosc.core.spec.args.*
import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import kotlinx.io.writeFloat

object OSCSerializer {

    fun serialize(obj: OSCPacket): ByteArray {
        val buffer = Buffer()
        serialize(buffer, obj)
        return buffer.readByteArray()
    }

    private fun serialize(buffer: Buffer, obj: OSCPacket) {
        when (obj) {
            is OSCMessage -> {
                // An OSC message consists of an OSC Address Pattern
                // followed by an OSC Type Tag String followed by zero or more OSC Arguments.
                buffer.write(obj.address)
                buffer.write(obj.arguments)
            }
            is OSCBundle -> {
                /**
                 * An OSC Bundle consists of the OSC-string “#bundle” followed by an OSC Time Tag,
                 * followed by zero or more OSC Bundle Elements.
                 *
                 * An OSC Bundle Element consists of its size and its contents.
                 * The size is an int32 representing the number of 8-bit bytes in the contents,
                 * and will always be a multiple of 4.
                 * The contents are either an OSC Message or an OSC Bundle.
                 *
                 * Note this recursive definition: a bundle may contain bundles.
                 *
                 * This table shows the parts of a two-or-more-element OSC Bundle and the size
                 * (in 8-bit bytes) of each part.
                 */
                buffer.write("#bundle")
                buffer.write(obj.time)
                obj.packets.forEach {
                    val data = serialize(it)
                    buffer.writeInt(data.size)
                    buffer.write(data)
                }
            }
        }
    }

    private fun Buffer.write(oscAddressPattern: OSCAddressPattern) {
        write(oscAddressPattern.address)
        align()
    }

    private fun Buffer.write(oscArguments: List<OSCArgument>) {
        writeByte(0x2C)
        oscArguments.forEach {
            writeTypeTag(it)
        }
        align()
        oscArguments.forEach {
            write(it)
        }
    }

    private fun Buffer.write(arg: OSCArgument) {
        when (arg) {
            is OSCString -> write(arg.value)
            is OSCBlob -> write(arg)
            // 32-bit big-endian IEEE 754 floating point number
            is OSCFloat32 -> writeFloat(arg.value)
            // 32-bit big-endian two’s complement integer
            is OSCInt32 -> writeInt(arg.value)
            // 64-bit big-endian fixed-point time tag
            is OSCTimeTag -> writeLong(arg.value)
        }
    }

    private fun Buffer.writeTypeTag(arg: OSCArgument) {
        writeASCII(
            when (arg) {
                is OSCString -> 's'
                is OSCBlob -> 'b'
                is OSCFloat32 -> 'f'
                is OSCInt32 -> 'i'
                is OSCTimeTag -> 't'
            }
        )
    }

    /**
     * An int32 size count, followed by that many 8-bit bytes of arbitrary binary data,
     * followed by 0-3 additional zero bytes to make the total number of bits a multiple of 32.
     */
    private fun Buffer.write(arg: OSCBlob) {
        writeInt(arg.value.size)
        write(arg.value)
        align()
    }

    /**
     * A sequence of non-null ASCII characters followed by a null,
     * followed by 0-3 additional null characters to make the total number of bits a multiple of 32.
     */
    private fun Buffer.write(string: String) {
        string.forEach {
            writeASCII(it)
        }
        writeNull()
        align()
    }

    private fun Buffer.writeASCII(c: Char) {
        writeByte(c.code.toByte())
    }

    private fun Buffer.writeNull() {
        writeByte(0)
    }

    private fun Buffer.align() {
        val overlap = size % 4
        val rest = (4 - overlap) % 4
        repeat(rest.toInt()) { _ ->
            writeNull()
        }
    }

}
