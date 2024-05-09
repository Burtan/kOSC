package de.frederikbertling.kosc.core.spec.args

/**
 * 32-bit big-endian two’s complement integer
 */
data class OSCInt32(
    val value: Int
) : OSCArgument
