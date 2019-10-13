package com.bylancer.classified.bylancerclassified.webservices.transaction

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class TransactionResponseModel {
    @SerializedName("status")
    @Expose
    var success : String? = null
}
