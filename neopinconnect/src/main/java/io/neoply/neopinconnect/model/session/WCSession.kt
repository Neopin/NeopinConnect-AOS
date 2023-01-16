package io.neoply.neopinconnect.model.session

import android.net.Uri
import io.neoply.neopinconnect.extension.toHex
import java.security.SecureRandom
import java.util.*

data class WCSession(
    val scheme: String = "nptwc",
    val topic: String = UUID.randomUUID().toString(),
    val version: String = "1.0",
    val bridge: String,
    val key: String = generateRandomKey()
) {
    fun toUri(scheme: String = "nptwc"): String = "${scheme}:${topic}@${version}?bridge=${bridge}&key=${key}"

    companion object {
        fun from(from: String): WCSession? {
            if (!from.startsWith("neopinwc:")
                && !from.startsWith("nptwc:")
                && !from.startsWith("wc:")) {
                return null
            }

            val uriString = from.replace("wc:", "wc://")
            val uri = Uri.parse(uriString)
            val scheme = uri.scheme
            val bridge = uri.getQueryParameter("bridge")
            val key = uri.getQueryParameter("key")
            val topic = uri.userInfo
            val version = uri.host

            if (scheme == null || bridge == null || key == null || topic == null || version == null) {
                return null
            }

            return WCSession(scheme, topic, version, bridge, key)
        }

        fun generateRandomKey(): String {
            val random = SecureRandom()
            val bytes = ByteArray(32)
            random.nextBytes(bytes)
            return bytes.toHex()
        }
    }
}