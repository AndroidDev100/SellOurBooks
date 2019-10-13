package com.bylancer.classified.bylancerclassified.submitcreditcardflow

import android.os.Parcel
import android.os.Parcelable

data class PayStackCard(var cardNumber: String?, var expiredDate: String?, var cardHolder: String?, var cvvCode: String?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cardNumber)
        parcel.writeString(expiredDate)
        parcel.writeString(cardHolder)
        parcel.writeString(cvvCode)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayStackCard> {
        override fun createFromParcel(parcel: Parcel): PayStackCard {
            return PayStackCard(parcel)
        }

        override fun newArray(size: Int): Array<PayStackCard?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "Card info:\r\n" +
                "Card number = " + cardNumber + "\r\n" +
                "Expired date = " + expiredDate + "\r\n" +
                "Card holder = " + cardHolder + "\r\n" +
                "CVV code = " + cvvCode
    }
}
