package de.esotechnik.phoehnlix.dataservice

import java.util.zip.CRC32

/**
 * @author Bernhard Frauendienst
 */
fun String.hexStringChecksum(): Long {
    val checksum = CRC32()
    this.chunkedSequence(2).forEach { checksum.update(it.toInt(16)) }
    return checksum.value
}

fun String.hexStringSignature() = hexStringChecksum().toString(16)

fun String.signHexString() = this + hexStringSignature()