package io.kosc.core.spec

/**
 * The unit of transmission of OSC is an OSC Packet.
 * Any application that sends OSC Packets is an OSC Client;
 * any application that receives OSC Packets is an OSC Server.
 *
 * An OSC packet consists of its contents, a contiguous block of binary data, and its size, the number of 8-bit bytes
 * that comprise the contents.
 * The size of an OSC packet is always a multiple of 4.
 *
 * The underlying network that delivers an OSC packet is responsible for delivering both the contents and the size to
 * the OSC application.
 * An OSC packet can be naturally represented by a datagram by a network protocol such as UDP.
 * In a stream-based protocol such as TCP, the stream should begin with an int32 giving the size of the first packet,
 * followed by the contents of the first packet, followed by the size of the second packet, etc.
 *
 * The contents of an OSC packet must be either an OSC Message or an OSC Bundle.
 * The first byte of the packet’s contents
 * unambiguously distinguishes between these two alternatives.
 */
sealed interface OSCPacket
