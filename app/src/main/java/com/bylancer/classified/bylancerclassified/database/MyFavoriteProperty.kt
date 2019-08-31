package com.bylancer.classified.bylancerclassified.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MyFavoriteProperty {
    @PrimaryKey
    var productId: String? = null
    var productName: String? = null
    var catId: String? = null
    var subCatId: String? = null
    var featured: String? = null
    var urgent: String? = null
    var highlight: String? = null
    var highlightBgClr: String? = null
    var location: String? = null
    var city: String? = null
    var status: String? = null
    var hide: String? = null
    var createdAt: String? = null
    var expireDate: String? = null
    var category: String? = null
    var subCategory: String? = null
    var favorite: Boolean? = null
    var showtag: String? = null
    var tag: String? = null
    var picCount: Int? = null
    var picture: String? = null
    var price: String? = null
    var currency: String? = null
    var currencyInLeft: String? = null
    var username: String? = null
    var subcriptionTitle: String? = null
    var subcriptionImage: String? = null
}