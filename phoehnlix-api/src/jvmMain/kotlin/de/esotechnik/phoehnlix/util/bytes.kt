package de.esotechnik.phoehnlix.util

import java.math.BigInteger

/**
 * @author Bernhard Frauendienst
 */

fun List<Byte>.toBigInt() = BigInteger(1, this.toByteArray())