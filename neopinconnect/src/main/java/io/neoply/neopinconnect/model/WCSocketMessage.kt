package io.neoply.neopinconnect.model

data class WCSocketMessage(
    val topic: String,
    val type: MessageType,
    val payload: String
)