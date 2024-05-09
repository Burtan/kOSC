package de.frederikbertling.kosc.core.spec.args

/**
 * 32-bit big-endian IEEE 754 floating point number
 */
data class OSCFloat32(
    val value: Float
) : OSCArgument
