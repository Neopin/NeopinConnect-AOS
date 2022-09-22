package io.neoply.neopinconnect.model

data class WCEncryptionPayload(
    val data: String,
    val hmac: String,
    val iv: String
)