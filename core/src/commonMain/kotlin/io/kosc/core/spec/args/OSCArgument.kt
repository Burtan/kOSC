package io.kosc.core.spec.args

/**
 * Atomic Data Types
 *
 * All OSC data is composed of the following fundamental data types:
 *
 * int32
 *     32-bit big-endian two’s complement integer
 * OSC-timetag
 *     64-bit big-endian fixed-point time tag, semantics defined below
 * float32
 *     32-bit big-endian IEEE 754 floating point number
 * OSC-string
 *     A sequence of non-null ASCII characters followed by a null, followed by 0-3 additional null characters to make
 *     the total number of bits a multiple of 32. (OSC-string examples) In this document, example OSC-strings will be
 *     written without the null characters, surrounded by double quotes.
 * OSC-blob
 *     An int32 size count, followed by that many 8-bit bytes of arbitrary binary data, followed by 0-3 additional zero
 *     bytes to make the total number of bits a multiple of 32.
 *
 * The size of every atomic data type in OSC is a multiple of 32 bits. This guarantees that if the beginning of a block
 * of OSC data is 32-bit aligned, every number in the OSC data will be 32-bit aligned.
 */
sealed interface OSCArgument
