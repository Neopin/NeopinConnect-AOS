package io.neoply.neopinconnect.model.session

import io.neoply.neopinconnect.model.WCPeerMeta

data class WCSessionRequest(
    val peerId: String,
    val peerMeta: WCPeerMeta,
    val chainId: String?
)