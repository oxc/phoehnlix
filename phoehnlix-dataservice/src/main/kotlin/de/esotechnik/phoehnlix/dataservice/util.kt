package de.esotechnik.phoehnlix.dataservice

import java.math.BigInteger
import java.util.zip.CRC32

fun String.hexStringBytes(): List<Byte> {
  return this.chunkedSequence(2).map { it.toInt(16).toByte() }.toList()
}

/**
 * @author Bernhard Frauendienst
 */
fun String.hexStringChecksum(): Long {
  val checksum = CRC32()
  this.chunkedSequence(2).forEach { checksum.update(it.toInt(16)) }
  return checksum.value
}

fun String.hexStringSignature() = hexStringChecksum().toString(16).padStart(8, '0')

fun String.signHexString() = this + hexStringSignature()
