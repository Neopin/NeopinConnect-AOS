package io.neoply.neopinconnect.exception

import java.lang.Exception

class InvalidJsonRpcParamsException(val requestId: Long) : Exception("Invalid JSON RPC Request")
