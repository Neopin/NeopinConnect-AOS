package io.neoply.neopinconnect.model.ethereum

import android.os.Parcel
import android.os.Parcelable

data class WCEthereumTransaction(
    val from: String,
    val to: String?,
    val nonce: String?,
    val gasPrice: String?,
    val maxFeePerGas: String? = null,
    val maxPriorityFeePerGas: String? = null,
    val gas: String?,
    val gasLimit: String? = null,
    val value: String?,
    val data: String
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()!!
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(from)
        parcel.writeString(to)
        parcel.writeString(nonce)
        parcel.writeString(gasPrice)
        parcel.writeString(maxFeePerGas)
        parcel.writeString(maxPriorityFeePerGas)
        parcel.writeString(gas)
        parcel.writeString(gasLimit)
        parcel.writeString(value)
        parcel.writeString(data)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WCEthereumTransaction> {
        override fun createFromParcel(parcel: Parcel): WCEthereumTransaction {
            return WCEthereumTransaction(parcel)
        }

        override fun newArray(size: Int): Array<WCEthereumTransaction?> {
            return arrayOfNulls(size)
        }
    }
}
