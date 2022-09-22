package io.neoply.neopinconnect.jsonrpc

data class JsonRpcError(
    val code: Int,
    val message: String
) {
    companion object {
        fun serverError(message: String) = JsonRpcError(-32000, message)
        fun lockClaim(message: String) = JsonRpcError(-32001, message)
        fun invalidParams(message: String) = JsonRpcError(-32602, message)
        fun invalidRequest(message: String) = JsonRpcError(-32600, message)
        fun parseError(message: String) = JsonRpcError(-32700, message)
        fun methodNotFound(message: String) = JsonRpcError(-32601, message)


        // 0 (Generic)
        fun GENERIC_0() = JsonRpcError(0, "message")

        // 1000 (Internal)
        fun INTERNAL_1000(name: String) = JsonRpcError(1000, "Missing or invalid $name")
        fun INTERNAL_1001(context: String) = JsonRpcError(1001, "Response is required for approved $context proposals")
        fun INTERNAL_1002(context: String) = JsonRpcError(1002, "Decrypt params required for $context")
        fun INTERNAL_1003(context: String) = JsonRpcError(1003, "Invalid $context update request")
        fun INTERNAL_1004(context: String) = JsonRpcError(1004, "Invalid $context upgrade request")
        fun INTERNAL_1005(name: String) = JsonRpcError(1005, "Invalid storage key name: $name")
        fun INTERNAL_1100(context: String, id: String) = JsonRpcError(1100, "Record already exists for $context matching id: $id")
        fun INTERNAL_1200(context: String) = JsonRpcError(1200, "Restore will override already set $context")
        fun INTERNAL_1300(context: String, id: String) = JsonRpcError(1300, "No matching $context with id: $id")
        fun INTERNAL_1301(context: String, topic: String) = JsonRpcError(1301, "No matching $context with topic: $topic")
        fun INTERNAL_1302(context: String) = JsonRpcError(1302, "No response found in pending $context proposal")
        fun INTERNAL_1303(tag: String) = JsonRpcError(1303, "No matching key with tag: $tag")
        fun INTERNAL_1400(method: String) = JsonRpcError(1400, "Unknown JSON-RPC Method Requested: $method")
        fun INTERNAL_1500(context: String, id: String) = JsonRpcError(1500, "Mismatched topic for $context with id: $id")
        fun INTERNAL_1501(mismatched_array: String) = JsonRpcError(1501, "Invalid accounts with mismatched chains: $mismatched_array")
        fun INTERNAL_1600(context: String) = JsonRpcError(1600, "$context settled")
        fun INTERNAL_1601(context: String) = JsonRpcError(1601, "$context not approved")
        fun INTERNAL_1602(context: String) = JsonRpcError(1602, "$context proposal responded")
        fun INTERNAL_1603(context: String) = JsonRpcError(1603, "$context response acknowledge")
        fun INTERNAL_1604(context: String) = JsonRpcError(1604, "$context expired")
        fun INTERNAL_1605(context: String) = JsonRpcError(1605, "$context deleted")
        fun INTERNAL_1606(topic: String) = JsonRpcError(1606, "Subscription resubscribed with topic: $topic")

        // 2000 (Timeout)
        fun TIMEOUT_2000(context: String, timeout: String) = JsonRpcError(2000, "$context failed to settle after $timeout seconds")
        fun TIMEOUT_2001(timeout: String, method: String) = JsonRpcError(2001, "JSON-RPC Request timeout after $timeout seconds: $method")

        // 3000 (Unauthorized)
        fun UNAUTHORIZED_3000(chainId: String) = JsonRpcError(3000, "Unauthorized Target ChainId Requested: $chainId")
        fun UNAUTHORIZED_3001(method: String) = JsonRpcError(3001, "Unauthorized JSON-RPC Method Requested: $method")
        fun UNAUTHORIZED_3002(type: String) = JsonRpcError(3002, "Unauthorized Notification Type Requested: $type")
        fun UNAUTHORIZED_3003(context: String) = JsonRpcError(3003, "Unauthorized $context update request")
        fun UNAUTHORIZED_3004(context: String) = JsonRpcError(3004, "Unauthorized $context upgrade request")
        fun UNAUTHORIZED_3005() = JsonRpcError(3005, "Unauthorized: peer is also {'' | 'not'} controller")

        // 4000 (EIP-1193)
        fun EIP_1193_4001() = JsonRpcError(4001, "User rejected the request.")
        fun EIP_1193_4100() = JsonRpcError(4100, "The requested account and/or method has not been authorized by the user.")
        fun EIP_1193_4200(blockhain: String) = JsonRpcError(4200, "The requested method is not supported by this $blockhain provider.")
        fun EIP_1193_4900() = JsonRpcError(4900, "The provider is disconnected from all chains.")
        fun EIP_1193_4901() = JsonRpcError(4901, "The provider is disconnected from the specified chain.")

        // 5000 (CAIP-25)
        fun CAIP_25_5000() = JsonRpcError(5000, "User disapproved requested chains")
        fun CAIP_25_5001() = JsonRpcError(5001, "User disapproved requested json-rpc methods")
        fun CAIP_25_5002() = JsonRpcError(5002, "User disapproved requested notification types")
        fun CAIP_25_5100(chains_array: String) = JsonRpcError(5100, "Requested chains are not supported: $chains_array")
        fun CAIP_25_5101(methods_array: String) = JsonRpcError(5101, "Requested json-rpc methods are not supported: $methods_array")
        fun CAIP_25_5102(types_array: String) = JsonRpcError(5102, "Requested notification types are not supported: $types_array")
        fun CAIP_25_5103(context: String) = JsonRpcError(5103, "Proposed $context signal is unsupported")
        fun CAIP_25_5900(context: String) = JsonRpcError(5900, "User disconnected $context")

        // 9000 (Unknown)
        fun UNKNOWN_9000(error_message: String) = JsonRpcError(9000, "Unknown error{'' || ': $error_message'}")

        // 11000 (USER_ACTION)
        fun permissionDenied(message: String) = JsonRpcError(11000, message)
        fun userCanceled(method: String) = JsonRpcError(11001, "User Cancel $method")
        fun notLoggedIn() = JsonRpcError(11002, "User Not Neopin Logged in")
        fun notCompletedKyc() = JsonRpcError(11003, "User Not Completed KYC")
        fun notCompletedTransaction() = JsonRpcError(11004, "User Not Completed Transaction")
    }
}