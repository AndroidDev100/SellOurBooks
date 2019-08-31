package com.bylancer.classified.bylancerclassified.appconfig

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AppConfigModel {

    @Expose
    var status: String? = null
    @SerializedName("app_name")
    @Expose
    var appName: String? = null
    @SerializedName("default_country")
    @Expose
    var defaultCountry: String? = null
    @SerializedName("default_lang_code")
    @Expose
    var defaultLangCode: String? = null
    @SerializedName("default_lang")
    @Expose
    var defaultLang: String? = null
    @SerializedName("detect_live_location")
    @Expose
    var detectLiveLocation: String? = null
    @SerializedName("terms_page_link")
    @Expose
    var termsPageLink: String? = null
    @SerializedName("policy_page_link")
    @Expose
    var policyPageLink: String? = null
    @SerializedName("app_version")
    @Expose
    var appVersion: String? = null
    @SerializedName("facebook_interstitial")
    @Expose
    var facebookInterstitial: Boolean? = null
    @SerializedName("google_banner")
    @Expose
    var googleBanner: Boolean? = null
    @SerializedName("google_interstitial")
    @Expose
    var googleInterstitial: Boolean? = null
    @SerializedName("premium_app")
    @Expose
    var premiumApp: Boolean? = null
    @SerializedName("categories")
    @Expose
    var categories: List<Category>? = null
    @SerializedName("languages")
    @Expose
    var languages: List<Language>? = null

}