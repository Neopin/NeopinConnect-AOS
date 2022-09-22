package io.neoply.neopinconnect.extension

private const val HEX_CHARS = "0123456789abcdef"

fun String.hexStringToByteArray() : ByteArray {
    val hex = lowercase()
    val result = ByteArray(length / 2)

    for (i in hex.indices step 2) {
        val firstIndex = HEX_CHARS.indexOf(hex[i])
        val secondIndex = HEX_CHARS.indexOf(hex[i + 1])

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }

    return result
}