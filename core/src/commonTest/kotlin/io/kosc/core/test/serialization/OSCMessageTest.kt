package io.kosc.core.test.serialization

import io.kosc.core.serialization.OSCSerializer
import io.kosc.core.spec.OSCMessage
import io.kosc.core.spec.args.OSCBlob
import io.kosc.core.spec.args.OSCFloat32
import io.kosc.core.spec.args.OSCInt32
import io.kotest.matchers.shouldBe
import kotlin.test.Test


class OSCMessageTest {

    @Test
    fun testEmpty() {
        val message = OSCMessage("/empty")
        val answer = byteArrayOf(47, 101, 109, 112, 116, 121, 0, 0, 44, 0, 0, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testFillerBeforeCommaNone() {
        val message = OSCMessage("/abcdef")
        // here we only have the addresses string terminator (0) before the ',' (44),
        // so the comma is 4 byte aligned
        val answer = byteArrayOf(47, 97, 98, 99, 100, 101, 102, 0, 44, 0, 0, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testFillerBeforeCommaOne() {
        val message = OSCMessage("/abcde")
        // here we have one padding 0 after the addresses string terminator (also 0)
        // and before the ',' (44), so the comma is 4 byte aligned
        val answer = byteArrayOf(47, 97, 98, 99, 100, 101, 0, 0, 44, 0, 0, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testFillerBeforeCommaTwo() {
        val message = OSCMessage("/abcd")
        // here we have two padding 0's after the addresses string terminator (also 0)
        // and before the ',' (44), so the comma is 4 byte aligned
        val answer = byteArrayOf(47, 97, 98, 99, 100, 0, 0, 0, 44, 0, 0, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testFillerBeforeCommaThree() {
        val message = OSCMessage("/abcdefg")
        // here we have three padding 0's after the addresses string terminator (also 0)
        // and before the ',' (44), so the comma is 4 byte aligned
        val answer = byteArrayOf(47, 97, 98, 99, 100, 101, 102, 103, 0, 0, 0, 0, 44, 0, 0, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testArgumentInteger() {
        val message = OSCMessage("/int", OSCInt32(99))
        val answer = byteArrayOf(47, 105, 110, 116, 0, 0, 0, 0, 44, 105, 0, 0, 0, 0, 0, 99)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testArgumentFloat() {
        val message = OSCMessage("/float", OSCFloat32(999.9f))
        val answer = byteArrayOf(47, 102, 108, 111, 97, 116, 0, 0, 44, 102, 0, 0, 68, 121, -7, -102)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

    @Test
    fun testArgumentBlob() {
        val message = OSCMessage("/blob", OSCBlob(byteArrayOf(-1, 0, 1)))
        val answer = byteArrayOf(47, 98, 108, 111, 98, 0, 0, 0, 44, 98, 0, 0, 0, 0, 0, 3, -1, 0, 1, 0)
        val result = OSCSerializer.serialize(message)
        result shouldBe answer
    }

}
