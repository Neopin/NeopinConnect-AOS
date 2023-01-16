package io.neoply.neopinconnect.model.method

import com.google.gson.annotations.SerializedName

enum class WCMethod(val message: String) {
    @SerializedName("wc_sessionRequest")
    SESSION_REQUEST("wc_sessionRequest"),

    @SerializedName("wc_sessionUpdate")
    SESSION_UPDATE("wc_sessionUpdate"),

    @SerializedName("get_accounts")
    GET_ACCOUNTS("get_accounts"),

    @SerializedName("trust_signTransaction")
    SIGN_TRANSACTION("trust_signTransaction"),

    @SerializedName("eth_sign")
    ETH_SIGN("eth_sign"),

    @SerializedName("personal_sign")
    ETH_PERSONAL_SIGN("personal_sign"),

    @SerializedName("eth_signTypedData")
    ETH_SIGN_TYPE_DATA("eth_signTypedData"),

    @SerializedName("eth_signTransaction")
    ETH_SIGN_TRANSACTION("eth_signTransaction"),

    @SerializedName("eth_sendTransaction")
    ETH_SEND_TRANSACTION("eth_sendTransaction"),

    @SerializedName("disconnect")
    DISCONNECT("disconnect"),

    @SerializedName("wc_customMethod")
    CUSTOM_METHOD("wc_customMethod");
}