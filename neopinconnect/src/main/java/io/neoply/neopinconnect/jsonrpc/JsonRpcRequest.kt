package io.neoply.neopinconnect.jsonrpc

import io.neoply.neopinconnect.constants.WCConstants
import io.neoply.neopinconnect.model.method.WCMethod

data class JsonRpcRequest<T>(
    val id: Long,
    val jsonrpc: String = WCConstants.JSONRPC_VERSION,
    val method: WCMethod?,
    val params: T
)