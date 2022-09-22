package io.neoply.neopinconnect.jsonrpc

import io.neoply.neopinconnect.constants.WCConstants

data class JsonRpcResponse<T>(
    val jsonrpc: String = WCConstants.JSONRPC_VERSION,
    val id: Long,
    val result: T
)

data class JsonRpcErrorResponse(
    val jsonrpc: String = WCConstants.JSONRPC_VERSION,
    val id: Long,
    val error: JsonRpcError
)