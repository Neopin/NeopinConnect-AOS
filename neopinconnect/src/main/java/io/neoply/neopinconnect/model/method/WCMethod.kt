package io.neoply.neopinconnect.model.method

import com.google.gson.annotations.SerializedName

enum class WCMethod {
    @SerializedName("wc_sessionRequest")
    SESSION_REQUEST,

    @SerializedName("wc_sessionUpdate")
    SESSION_UPDATE,

    @SerializedName("get_accounts")
    GET_ACCOUNTS,

    @SerializedName("trust_signTransaction")
    SIGN_TRANSACTION,

    @SerializedName("eth_sign")
    ETH_SIGN,

    @SerializedName("personal_sign")
    ETH_PERSONAL_SIGN,

    @SerializedName("eth_signTypedData")
    ETH_SIGN_TYPE_DATA,

    @SerializedName("eth_signTransaction")
    ETH_SIGN_TRANSACTION,

    @SerializedName("eth_sendTransaction")
    ETH_SEND_TRANSACTION,

    @SerializedName("disconnect")
    DISCONNECT,

    @SerializedName("wc_customMethod")
    CUSTOM_METHOD;
}