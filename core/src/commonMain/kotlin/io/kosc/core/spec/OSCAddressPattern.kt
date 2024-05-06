package io.kosc.core.spec

/**
 * An OSC Address Pattern is an OSC-string beginning with the character ‘/’ (forward slash).
 */
data class OSCAddressPattern(
    val address: String
) {

    init {
        require(address.startsWith("/")) { "Address must start with a '/'" }
    }

}
