package io.neoply.neopinconnect.extension

private const val HEX_CHAR = "0123456789abcdef"
private val HEX_CHARS = HEX_CHAR.toCharArray()

fun ByteArray.toHex(): String {
    val result = StringBuffer()
    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }
    return result.toString()
}

fun String.hexStringToByteArray(): ByteArray {
    val hex = this.lowercase().replace("0x", "")
    val result = ByteArray(hex.length / 2)
    for (i in hex.indices step 2) {
        val firstIndex = HEX_CHAR.indexOf(hex[i])
        val secondIndex = HEX_CHAR.indexOf(hex[i + 1])
        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }
    return result
}

fun String.encodeHex(): String {
    return this.toByteArray().toHex()
}
