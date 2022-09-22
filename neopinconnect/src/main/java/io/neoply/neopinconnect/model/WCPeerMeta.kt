package io.neoply.neopinconnect.model

data class WCPeerMeta(
    val appId: String? = null,
    val name: String,
    val url: String,
    val description: String? = null,
    val icons: List<String> = listOf(""),
    val deepLink: String? = null
)