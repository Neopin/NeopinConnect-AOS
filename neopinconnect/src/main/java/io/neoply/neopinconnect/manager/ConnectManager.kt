package io.neoply.neopinconnect.manager

import android.os.SystemClock
import io.neoply.neopinconnect.WCClient
import io.neoply.neopinconnect.model.WCPeerMeta
import io.neoply.neopinconnect.model.ethereum.WCEthereumTransaction
import io.neoply.neopinconnect.model.session.WCSession
import io.neoply.neopinconnect.model.session.ConnectSession
import io.neoply.neopinconnect.model.method.WCMethod
import okhttp3.OkHttpClient
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

object ConnectManager {

    var wcConnect: (peerId: String, session: WCSession) -> Unit = { _, _ -> Unit }
    var wcDisconnect: (peerId: String) -> Unit = { _ -> Unit }
    var wcSessionApprove: (peerId: String, requestId: Long, accounts: List<String>?) -> Unit = { _, _, _ -> Unit }
    var wcSessionReject: (peerId: String, requestId: Long, payload: String) -> Unit = { _, _, _ -> Unit }
    var wcResultResponse: (peerId: String, requestId: Long, payload: String) -> Unit = { _, _, _ -> Unit }

    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.MINUTES)
        .writeTimeout(30, TimeUnit.MINUTES)
        .build()

    private var instance: ConnectSession? = null
    fun getInstance(): ConnectSession? {
        return instance
    }

    fun setClient(wcSession: WCSession, peerMeta: WCPeerMeta): String {
        if (getInstance() != null) {
            disconnect()
            SystemClock.sleep(500)
        }

        val peerId = UUID.randomUUID().toString()
        val client = WCClient(httpClient = okHttpClient)
        instance = ConnectSession(
            id = peerId,
            session = wcSession,
            client = client,
            regDate = System.currentTimeMillis(),
        )
        client.let {
            it.onConnected = { peerId, session ->
                wcConnect.invoke(peerId, session)
            }
            it.onDisconnect = { _, _ ->
                disconnect()
                wcDisconnect.invoke(peerId)
            }
            it.onFailure = { t ->
                Timber.e("onFailure: ${t.message}")
                if (t.javaClass.simpleName == "SocketException") {
                    disconnect()
                    wcDisconnect.invoke(peerId)
                }
            }
            it.onSessionApprove = { id: Long, accounts: List<String>? ->
                instance?.apply {
                    regDate = System.currentTimeMillis()
                    userAddress = accounts?.get(0).toString()
                    network = client.chainId
                    remotePeerMeta = client.remotePeerMeta
                    remotePeerId = client.remotePeerId
                }
                wcSessionApprove.invoke(peerId, id, accounts)
            }
            it.onSessionReject = { id: Long, payload: String ->
                wcSessionReject.invoke(peerId, id, payload)
            }
            it.onCustomMethod = { id, payload -> }
            it.onResultResponse = { id, payload ->
                wcResultResponse.invoke(peerId, id, payload)
            }
        }
        client.connect(session = wcSession, peerMeta = peerMeta, peerId = peerId)
        return peerId
    }

    fun requestSession(id: String) {
        val instance = getInstance()!!
        instance.apply {
            client.requestSession(id, client.peerMeta!!)
            regDate = System.currentTimeMillis()
        }
    }

    fun disconnect(): Boolean {
        val client = getInstance()!!.client
        val result = if (client.session != null) {
            client.killSession()
        } else {
            client.disconnect()
        }
        instance = null
        return result
    }

    fun isSocketConnect(): Boolean {
        return if (getInstance() != null) {
            (getInstance()!!.client.isConnected || getInstance()!!.client.remotePeerMeta != null)
        } else {
            false
        }
    }

    fun getAccounts(): Boolean {
        val client = getInstance()?.client
        return if (client?.session == null) {
            false
        } else {
            client.customMethod(WCMethod.GET_ACCOUNTS, "")
        }
    }

    fun personalSign(message: String): Boolean {
        val client = getInstance()?.client
        return if (client?.session == null) {
            false
        } else {
            client.customMethodList(WCMethod.ETH_PERSONAL_SIGN, listOf(message, getInstance()?.userAddress ?: ""))
        }
    }

    fun signTransaction(transaction: WCEthereumTransaction): Boolean {
        val client = getInstance()?.client
        val result = if (client?.session == null) {
            false
        } else {
            client.customMethod(WCMethod.ETH_SIGN_TRANSACTION, transaction)
        }
        return result
    }

    fun sendTransaction(transaction: WCEthereumTransaction): Boolean {
        val client = getInstance()?.client
        val result = if (client?.session == null) {
            false
        } else {
            client.customMethod(WCMethod.ETH_SEND_TRANSACTION, transaction)
        }
        return result
    }
}