package io.neoply.neopinconnect.exception

import java.lang.Exception

class InvalidHmacException : Exception("Received and computed HMAC doesn't mach")