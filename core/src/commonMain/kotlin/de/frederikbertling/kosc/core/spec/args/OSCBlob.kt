package de.frederikbertling.kosc.core.spec.args

/**
 * An int32 size count, followed by that many 8-bit bytes of arbitrary binary data, followed by 0-3 additional zero
 * bytes to make the total number of bits a multiple of 32.
 */
data class OSCBlob(
    val value: ByteArray
) : OSCArgument {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as OSCBlob

        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

}
