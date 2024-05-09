package de.frederikbertling.kosc.core.spec.args

/**
 * A sequence of non-null ASCII characters followed by a null, followed by 0-3 additional null characters to make the
 * total number of bits a multiple of 32. (OSC-string examples) In this document, example OSC-strings will be written
 * without the null characters, surrounded by double quotes.
 */
data class OSCString(
    val value: String
) : OSCArgument

