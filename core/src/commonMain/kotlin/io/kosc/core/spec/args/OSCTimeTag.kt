package io.kosc.core.spec.args

/**
 * Time tags are represented by a 64-bit fixed point number. The first 32 bits specify the number of seconds since
 * midnight on January 1, 1900, and the last 32 bits specify fractional parts of a second to a precision of about 200
 * picoseconds. This is the representation used by Internet NTP timestamps. The time tag value consisting of 63 zero
 * bits followed by a one in the least significant bit is a special case meaning “immediately.”
 */
data class OSCTimeTag(
    val value: Long
) : OSCArgument
