package io.neoply.dappsample.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import io.neoply.dappsample.R
import io.neoply.dappsample.databinding.ActivityDappMainBinding
import io.neoply.neopinconnect.manager.ConnectManager
import io.neoply.neopinconnect.model.WCPeerMeta
import io.neoply.neopinconnect.model.ethereum.WCEthereumTransaction
import io.neoply.neopinconnect.model.session.WCSession
import kotlinx.coroutines.launch

class DappMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDappMainBinding
    private var qrCodeDialog: QrCodeDialog? = null
    private var isForceQrCode = false
    private var lastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dapp_main)
        binding.apply {
            lifecycleOwner = this@DappMainActivity
            setConnectCheck(false)

            ConnectManager.wcConnect = { peerId, session ->
                viewTextMessage("Open Bridge: ${session.bridge}")
                ConnectManager.requestSession(peerId)
                if (!isForceQrCode && isInstalled("io.neoply.walletsample")) {
                    viewTextMessage(
                        "Call Wallet DeepLink\n" +
                            "wcSession.topic: ${session.topic}\n" +
                            "wcClient.peerId: $peerId"
                    )
                    startActivity(Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(session.toUri())
                    ))
                } else {
                    viewTextMessage(
                        "Call Wallet QR Code Create\n" +
                            "wcSession.topic: ${session.topic}\n" +
                            "wcClient.peerId: $peerId"
                    )
                    QrCodeDialog(session.toUri()).let { dialog ->
                        qrCodeDialog = dialog
                        dialog.show(supportFragmentManager, dialog.tag)
                    }
                }
            }
            ConnectManager.wcDisconnect = { peerId ->
                setConnectCheck(false)
                viewTextMessage("Disconnect: $peerId")
            }
            ConnectManager.wcSessionApprove = { peerId, requestId, accounts ->
                viewTextMessage("Connect: $peerId")
                setConnectCheck(true)
                qrCodeDialog?.dismiss()
                val instance = ConnectManager.getInstance()
                viewTextMessage(
                    "onSessionApprove remotePeerId: ${instance?.remotePeerId}\n" +
                        "remotePeerMeta: ${instance?.remotePeerMeta}\n" +
                        "userAddress: ${instance?.userAddress}\n" +
                        "chainId: ${instance?.network}"
                )
            }
            ConnectManager.wcSessionReject = { peerId, requestId, payload ->
                val instance = ConnectManager.getInstance()
                viewTextMessage(
                    "onSessionReject remotePeerId: ${instance?.remotePeerId}\n" +
                        "remotePeerMeta: ${instance?.remotePeerMeta}\n" +
                        "payload: $payload"
                )
            }
            ConnectManager.wcResultResponse = { peerId, requestId, payload ->
                val instance = ConnectManager.getInstance()
                viewTextMessage(
                    "onResultResponse remotePeerId: ${instance?.remotePeerId}\n" +
                        "remotePeerMeta: ${instance?.remotePeerMeta}\n" +
                        "payload: $payload"
                )
            }

            toolbar.setNavigationOnClickListener {
                onBackPressed()
            }
            btnConnect.setOnClickListener {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                if ((elapsedRealtime - lastClickTime) < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = elapsedRealtime

                isForceQrCode = false
                connect()
            }
            btnDisconnect.setOnClickListener {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                if ((elapsedRealtime - lastClickTime) < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = elapsedRealtime

                setConnectCheck(false)
                viewTextMessage("Disconnect: ${ConnectManager.getInstance()?.id}")
                ConnectManager.disconnect()
            }
            btnQrconnect.setOnClickListener {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                if ((elapsedRealtime - lastClickTime) < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = elapsedRealtime

                isForceQrCode = true
                connect()
            }
            btnAccount.setOnClickListener {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                if ((elapsedRealtime - lastClickTime) < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = elapsedRealtime

                if (ConnectManager.isSocketConnect()) {
                    ConnectManager.getAccounts()
                } else {
                    viewTextMessage("Bridge Server is Not Connected!")
                }
            }
            btnSendTransaction.setOnClickListener {
                val elapsedRealtime = SystemClock.elapsedRealtime()
                if ((elapsedRealtime - lastClickTime) < 1000) {
                    return@setOnClickListener
                }
                lastClickTime = elapsedRealtime

                if (ConnectManager.isSocketConnect()) {
                    val instance = ConnectManager.getInstance()
                    ConnectManager.sendTransaction(
                        WCEthereumTransaction(
                            from = instance?.userAddress ?: "0x0",
                            to = "0x0", // Contract Address
                            nonce = "0x1",
                            gasPrice = "0x9184e72a000",
                            gas = "0x76c0",
                            value = "0x0",
                            data = "0xa9059cbb00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                        )
                    )
                } else {
                    viewTextMessage("Bridge Server is Not Connected!")
                }
            }
        }
    }

    private fun connect() {
        ConnectManager.setClient(
            wcSession = WCSession(
                bridge = "https://bridge.walletconnect.org",
            ),
            peerMeta = WCPeerMeta(
                appId = "e4ec094244",
                name = "DApp Sample",
                url = "https://www.sample.com/",
                description = "DApp Platform",
                icons = listOf("https://raw.githubusercontent.com/Neopin/NeopinConnect-AOS/main/dappsample/src/main/res/mipmap-xxxhdpi/ic_launcher.png"),
            )
        )
    }

    private fun isInstalled(name: String): Boolean = try {
        packageManager.getApplicationInfo(name, 0).enabled
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }

    private fun setConnectCheck(isConnect: Boolean) {
        lifecycleScope.launch {
            binding.apply {
                btnConnect.isGone = isConnect
                btnQrconnect.isGone = isConnect
                btnDisconnect.isVisible = isConnect
            }
        }
    }

    private fun viewTextMessage(message: String) {
        lifecycleScope.launch {
            binding.apply {
                tvTest.text = "${tvTest.text}\n$message\n"
                nsvLog.post {
                    nsvLog.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onDestroy() {
        if (ConnectManager.isSocketConnect()) {
            ConnectManager.disconnect()
        }
        super.onDestroy()
    }
}