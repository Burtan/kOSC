package de.frederikbertling.kosc.core.spec

import de.frederikbertling.kosc.core.spec.args.OSCTimeTag

/**
 * An OSC Bundle consists of the OSC-string “#bundle” followed by an OSC Time Tag,
 * followed by zero or more OSC Bundle Elements.
 * The OSC-timetag is a 64-bit fixed point time tag whose semantics are described below.
 *
 * An OSC Bundle Element consists of its size and its contents.
 * The size is an int32 representing the number of 8-bit bytes in the contents, and will always be a multiple of 4.
 * The contents are either an OSC Message or an OSC Bundle.
 *
 * Note this recursive definition: a bundle may contain bundles.
 *
 * This table shows the parts of a two-or-more-element OSC Bundle and the size (in 8-bit bytes) of each part.
 */
data class OSCBundle(
    val time: OSCTimeTag,
    val packets: List<OSCPacket>,
) : OSCPacket
