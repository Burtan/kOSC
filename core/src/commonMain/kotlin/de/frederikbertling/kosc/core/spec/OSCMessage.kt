package de.frederikbertling.kosc.core.spec

import de.frederikbertling.kosc.core.spec.args.OSCArgument

/**
 * An OSC message consists of an OSC Address Pattern followed by an OSC Type Tag String followed by zero or more
 * OSC Arguments.
 */
data class OSCMessage(
    val address: OSCAddressPattern,
    val arguments: List<OSCArgument> = emptyList()
) : OSCPacket {

    constructor(
        address: String,
        arguments: List<OSCArgument> = emptyList()
    ) : this(OSCAddressPattern(address), arguments)

    constructor(
        address: String,
        argument: OSCArgument
    ) : this(OSCAddressPattern(address), listOf(argument))
}
