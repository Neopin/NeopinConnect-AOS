package io.neoply.neopinconnect.model.session

import io.neoply.neopinconnect.WCClient
import io.neoply.neopinconnect.model.WCPeerMeta

data class ConnectSession(
    var id: String,
    var session: WCSession,
    var client: WCClient,
    var regDate: Long = 0,
    var userAddress: String? = null,
    var network: String? = null,
    var remotePeerMeta: WCPeerMeta? = null,
    var remotePeerId: String? = null,
)