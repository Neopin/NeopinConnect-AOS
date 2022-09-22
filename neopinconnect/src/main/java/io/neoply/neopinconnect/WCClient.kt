package io.neoply.neopinconnect

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import io.neoply.neopinconnect.constants.WCConstants
import io.neoply.neopinconnect.exception.InvalidJsonRpcParamsException
import io.neoply.neopinconnect.extension.hexStringToByteArray
import io.neoply.neopinconnect.jsonrpc.JsonRpcError
import io.neoply.neopinconnect.jsonrpc.JsonRpcErrorResponse
import io.neoply.neopinconnect.jsonrpc.JsonRpcRequest
import io.neoply.neopinconnect.jsonrpc.JsonRpcResponse
import io.neoply.neopinconnect.model.*
import io.neoply.neopinconnect.model.ethereum.WCEthereumSignMessage
import io.neoply.neopinconnect.model.ethereum.WCEthereumTransaction
import io.neoply.neopinconnect.model.method.WCMethod
import io.neoply.neopinconnect.model.session.WCApproveSessionResponse
import io.neoply.neopinconnect.model.session.WCSession
import io.neoply.neopinconnect.model.session.WCSessionRequest
import io.neoply.neopinconnect.model.session.WCSessionUpdate
import okhttp3.*
import okio.ByteString
import timber.log.Timber
import java.util.*

open class WCClient(
    builder: GsonBuilder = GsonBuilder(),
    private val httpClient: OkHttpClient
): WebSocketListener() {

    private val gson = builder
        .serializeNulls()
        .create()

    private var socket: WebSocket? = null

    private val listeners: MutableSet<WebSocketListener> = mutableSetOf()

    var session: WCSession? = null
        private set
    var chainId: String? = null
        private set
    var peerMeta: WCPeerMeta? = null
        private set
    var peerId: String? = null
        private set
    var remotePeerMeta: WCPeerMeta? = null
        private set
    var remotePeerId: String? = null
        private set
    var isConnected: Boolean = false
        private set
    var responseId: Long = -1
    var currentMethod: String = ""

    private var isReconnect: Boolean = false
    private var retryCount: Int = 0

    private var requestSessionId: Long? = null
    private var handshakeId: Long = -1

    var onConnected: (peerId: String, session: WCSession) -> Unit = { _, _ -> Unit }
    var onFailure: (Throwable) -> Unit = { _ -> Unit}
    var onDisconnect: (code: Int, reason: String) -> Unit = { _, _ -> Unit }
    var onSessionRequest: (id: Long, peer: WCPeerMeta) -> Unit = { _, _ -> Unit }
    var onSessionApprove: (id: Long, accounts: List<String>?) -> Unit = { _, _ -> Unit }
    var onSessionReject: (id: Long, payload: String) -> Unit = { _, _ -> Unit }
    var onEthSign: (id: Long, message: WCEthereumSignMessage) -> Unit = { _, _ -> Unit }
    var onEthSignTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit = { _, _ -> Unit }
    var onEthSendTransaction: (id: Long, transaction: WCEthereumTransaction) -> Unit = { _, _ -> Unit }
    var onGetAccounts: (id: Long) -> Unit = { _ -> Unit }
    var onSignTransaction: (id: Long, transaction: WCSignTransaction) -> Unit = { _, _ -> Unit }
    var onCustomMethod: (id: Long, payload: String) -> Unit = { _, _ -> Unit }
    var onResultResponse: (id: Long, payload: String) -> Unit = { _, _ -> Unit }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Timber.d("<< websocket opened >>")
        isConnected = true
        retryCount = 0

        listeners.forEach { it.onOpen(webSocket, response) }

        val session = this.session ?: throw IllegalStateException("session can't be null on connection open")
        val peerId = this.peerId ?: throw IllegalStateException("peerId can't be null on connection open")
        // The Session.topic channel is used to listen session request messages only.
        subscribe(session.topic)
        // The peerId channel is used to listen to all messages sent to this httpClient.
        subscribe(peerId)
        onConnected(peerId, session)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        if (text == "Missing or invalid socket data") {
            // OkHttpClient Ping Interval
            // Missing or invalid socket data
            return
        }
        var decrypted: String? = null
        try {
            Timber.d("<== message $text")
            decrypted = decryptMessage(text)
            Timber.d("<== decrypted $decrypted")
            handleMessage(decrypted)

            retryCount = 0
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            if (retryCount <= 30) {
                listeners.forEach { it.onMessage(webSocket, decrypted ?: text) }
                retryCount++
            } else {
                if (this.session != null) {
                    killSession()
                } else {
                    disconnect()
                }
                retryCount = 0
            }
            if (retryCount > 0) {
                Timber.d("<== retryCount: $retryCount")
            }
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        resetState()
        onFailure(t)

        listeners.forEach { it.onFailure(webSocket, t, response) }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Timber.d("<< websocket closed >>")

        listeners.forEach { it.onClosed(webSocket, code, reason) }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        Timber.d("<== pong")

        listeners.forEach { it.onMessage(webSocket, bytes) }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Timber.d("<< closing socket >>")

        resetState()
        onDisconnect(code, reason)

        listeners.forEach { it.onClosing(webSocket, code, reason) }
    }

    fun connect(
        session: WCSession,
        peerMeta: WCPeerMeta,
        peerId: String = UUID.randomUUID().toString(),
        remotePeerMeta: WCPeerMeta? = null,
        remotePeerId: String? = null,
    ) {
        if (this.session != null && this.session?.topic != session.topic) {
            isReconnect = true
            killSession()
        }

        this.session = session
        this.peerMeta = peerMeta
        this.peerId = peerId
        this.remotePeerMeta = remotePeerMeta
        this.remotePeerId = remotePeerId

        val request = Request.Builder()
            .url(session.bridge)
            .build()

        socket = httpClient.newWebSocket(request, this)
    }

    fun approveSession(accounts: List<String>, chainId: String? = null): Boolean {
        check(handshakeId > 0) { "handshakeId must be greater than 0 on session approve" }
        this.chainId = chainId
        val result = WCApproveSessionResponse(
            chainId = chainId?.toIntOrNull() ?: this.chainId?.toIntOrNull(),
            accounts = accounts,
            peerId = peerId,
            peerMeta = peerMeta
        )
        val response = JsonRpcResponse(
            id = handshakeId,
            result = result
        )
        return encryptAndSend(gson.toJson(response))
    }

    fun updateSession(accounts: List<String>? = null, chainId: String? = null, approved: Boolean = true): Boolean {
        val request = JsonRpcRequest(
            id = System.currentTimeMillis(),
            method = WCMethod.SESSION_UPDATE,
            params = listOf(
                WCSessionUpdate(
                    approved = approved,
                    chainId = chainId?.toIntOrNull() ?: this.chainId?.toIntOrNull(),
                    accounts = accounts
                )
            )
        )
        return encryptAndSend(gson.toJson(request))
    }

    fun requestSession(peerId: String, peerMeta: WCPeerMeta, chainId: String? = null): Boolean {
        requestSessionId = System.currentTimeMillis()
        val request = JsonRpcRequest(
            id = requestSessionId!!,
            method = WCMethod.SESSION_REQUEST,
            params = listOf(
                WCSessionRequest(
                    peerId = peerId,
                    peerMeta = peerMeta,
                    chainId = chainId
                )
            )
        )
        return encryptAndSend(gson.toJson(request))
    }

    fun customMethod(method: WCMethod, data: Any): Boolean {
        val request = JsonRpcRequest(
            id = System.currentTimeMillis(),
            method = method,
            params = listOf(data)
        )
        return encryptAndSend(gson.toJson(request))
    }

    fun customMethodList(method: WCMethod, data: List<Any>): Boolean {
        val request = JsonRpcRequest(
            id = System.currentTimeMillis(),
            method = method,
            params = data
        )
        return encryptAndSend(gson.toJson(request))
    }

    fun rejectSession(message: String = "Session rejected"): Boolean {
        check(handshakeId > 0) { "handshakeId must be greater than 0 on session reject" }

        val response = JsonRpcErrorResponse(
            id = handshakeId,
            error = JsonRpcError.serverError(
                message = message
            )
        )
        return encryptAndSend(gson.toJson(response))
    }

    fun killSession(): Boolean {
        updateSession(approved = false)
        return disconnect()
    }

    fun <T> approveRequest(id: Long, result: T): Boolean {
        val response = JsonRpcResponse(
            id = id,
            result = result
        )
        return encryptAndSend(gson.toJson(response))
    }

    fun rejectRequest(id: Long, message: String = "Reject by the user"): Boolean {
        val response = JsonRpcErrorResponse(
            id = id,
            error = JsonRpcError.serverError(
                message = message
            )
        )
        return encryptAndSend(gson.toJson(response))
    }

    fun errorRequest(id: Long, jsonRpcError: JsonRpcError): Boolean {
        val response = JsonRpcErrorResponse(
            id = id,
            error = jsonRpcError
        )
        return encryptAndSend(gson.toJson(response))
    }

    private fun decryptMessage(text: String): String {
        val message = gson.fromJson(text, WCSocketMessage::class.java)
        val encrypted = gson.fromJson(message.payload, WCEncryptionPayload::class.java)
        val session = this.session ?: throw IllegalStateException("session can't be null on message receive")
        return String(WCCipher.decrypt(encrypted, session.key.hexStringToByteArray()), Charsets.UTF_8)
    }

    private fun invalidParams(id: Long): Boolean {
        val response = JsonRpcErrorResponse(
            id = id,
            error = JsonRpcError.invalidParams(
                message = "Invalid parameters"
            )
        )
        return encryptAndSend(gson.toJson(response))
    }

    private fun handleMessage(payload: String) {
        try {
            val request = gson.fromJson<JsonRpcRequest<JsonArray>>(payload, object : TypeToken<JsonRpcRequest<JsonArray>>() {}.type)
            val method = request.method
            if (method != null) {
                handleRequest(request)
            } else {
                if (requestSessionId == request.id) {
                    requestSessionId = null
                    val param = gson.fromJson<JsonRpcResponse<WCSessionRequest?>>(payload, object : TypeToken<JsonRpcResponse<WCSessionRequest?>>() {}.type)
                    val account = gson.fromJson<JsonRpcResponse<WCSessionUpdate?>>(payload, object : TypeToken<JsonRpcResponse<WCSessionUpdate?>>() {}.type)
                    if (param.result != null) {
                        handshakeId = request.id
                        chainId = param.result.chainId
                        remotePeerId = param.result.peerId
                        remotePeerMeta = param.result.peerMeta
                        onSessionApprove(request.id, account.result?.accounts)
                    } else {
                        onSessionReject(request.id, payload)
                    }
                } else {
                    onResultResponse(request.id, payload)
                }
            }
        } catch (e: InvalidJsonRpcParamsException) {
            invalidParams(e.requestId)
        }
    }

    private fun handleRequest(request: JsonRpcRequest<JsonArray>) {
        responseId = request.id
        currentMethod = request.method?.name ?: ""

        when (request.method) {
            WCMethod.DISCONNECT -> {
                Timber.d("disconnect")
            }
            WCMethod.SESSION_REQUEST -> {
                val param = gson.fromJson<List<WCSessionRequest>>(request.params, object : TypeToken<List<WCSessionRequest>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                handshakeId = request.id
                remotePeerId = param.peerId
                remotePeerMeta = param.peerMeta
                chainId = param.chainId
                onSessionRequest(request.id, param.peerMeta)
            }
            WCMethod.SESSION_UPDATE -> {
                val param = gson.fromJson<List<WCSessionUpdate>>(request.params, object : TypeToken<List<WCSessionUpdate>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                if (!param.approved) {
                    killSession()
                }
            }
            WCMethod.GET_ACCOUNTS -> {
                onGetAccounts(request.id)
            }
            WCMethod.SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCSignTransaction>>(request.params, object : TypeToken<List<WCSignTransaction>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onSignTransaction(request.id, param)
            }
            WCMethod.ETH_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params, object : TypeToken<List<String>>() {}.type)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.MESSAGE))
            }
            WCMethod.ETH_PERSONAL_SIGN -> {
                val params = gson.fromJson<List<String>>(request.params, object : TypeToken<List<String>>() {}.type)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.PERSONAL_MESSAGE))
            }
            WCMethod.ETH_SIGN_TYPE_DATA -> {
                val params = gson.fromJson<List<String>>(request.params, object : TypeToken<List<String>>() {}.type)
                if (params.size < 2)
                    throw InvalidJsonRpcParamsException(request.id)
                onEthSign(request.id, WCEthereumSignMessage(params, WCEthereumSignMessage.WCSignType.TYPED_MESSAGE))
            }
            WCMethod.ETH_SIGN_TRANSACTION -> {
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params, object : TypeToken<List<WCEthereumTransaction>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onEthSignTransaction(request.id, param)
            }
            WCMethod.ETH_SEND_TRANSACTION -> {
                val param = gson.fromJson<List<WCEthereumTransaction>>(request.params, object : TypeToken<List<WCEthereumTransaction>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onEthSendTransaction(request.id, param)
            }
            else -> {
                val param = gson.fromJson<List<String>>(request.params, object : TypeToken<List<String>>() {}.type)
                    .firstOrNull() ?: throw InvalidJsonRpcParamsException(request.id)
                onCustomMethod(request.id, param)
            }
        }
    }

    private fun subscribe(topic: String): Boolean {
        val message = WCSocketMessage(
            topic = topic,
            type = MessageType.SUB,
            payload = ""
        )
        val json = gson.toJson(message)
        Timber.d("==> subscribe $json")
        return socket?.send(gson.toJson(message)) ?: false
    }

    private fun encryptAndSend(result: String): Boolean {
        Timber.d("==> message $result")
        val session = this.session ?: throw IllegalStateException("session can't be null on message send")
        val payload = gson.toJson(WCCipher.encrypt(result.toByteArray(Charsets.UTF_8), session.key.hexStringToByteArray()))
        val message = WCSocketMessage(
            // Once the remotePeerId is defined, all messages must be sent to this channel. The session.topic channel
            // will be used only to respond the session request message.
            topic = remotePeerId ?: session.topic,
            type = MessageType.PUB,
            payload = payload
        )
        val json = gson.toJson(message)
        Timber.d("==> encrypted $json")
        return socket?.send(json) ?: false
    }


    fun disconnect(): Boolean {
        return socket?.close(WCConstants.WS_CLOSE_NORMAL, null) ?: false
    }

    fun addSocketListener(listener: WebSocketListener) {
        listeners.add(listener)
    }

    fun removeSocketListener(listener: WebSocketListener) {
        listeners.remove(listener)
    }

    private fun resetState() {
        if (!isReconnect) {
            handshakeId = -1
            isConnected = false
            session = null
            peerMeta = null
            peerId = null
            remotePeerMeta = null
            remotePeerId = null
        } else {
            isReconnect = false
        }
    }
}
