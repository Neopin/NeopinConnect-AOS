package io.neoply.neopinconnect.model.session

import io.neoply.neopinconnect.model.WCPeerMeta

data class WCApproveSessionResponse(
    val approved: Boolean = true,
    val chainId: Int?,
    val accounts: List<String>,
    val peerId: String?,
    val peerMeta: WCPeerMeta?
)