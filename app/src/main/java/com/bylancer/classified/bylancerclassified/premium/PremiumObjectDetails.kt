package com.bylancer.classified.bylancerclassified.premium

import android.os.Parcel
import android.os.Parcelable

data class PremiumObjectDetails (val name : String, val description : String, val cost : Int, val canCancelSelection : Boolean = false, var isSelected : Boolean = false)
    : Parcelable {
    constructor(parcel: Parcel) : this (
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeInt(cost)
        parcel.writeByte(if (canCancelSelection) 1 else 0)
        parcel.writeByte(if (isSelected) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PremiumObjectDetails> {
        override fun createFromParcel(parcel: Parcel): PremiumObjectDetails {
            return PremiumObjectDetails(parcel)
        }

        override fun newArray(size: Int): Array<PremiumObjectDetails?> {
            return arrayOfNulls(size)
        }
    }
}