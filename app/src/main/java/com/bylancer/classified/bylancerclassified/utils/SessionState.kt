package com.bylancer.classified.bylancerclassified.utils

import android.content.Context
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail

class SessionState private constructor() {
    var isLogin: Boolean = false
    var isGoogleBannerSupported: Boolean = false
    var isGoogleInterstitialSupported: Boolean = false
    var isFacebookInterstitialSupported: Boolean = false
    var isPremiumAppSupported: Boolean = false
    var isLoginFirstTime: Boolean = false
    var displayName: String = ""
    var email: String = ""
    var phoneNumber: String = ""
    var address: String = ""
    var userId: String = ""
    var token: String = ""
    var userName: String = ""
    var profilePicUrl: String = ""
    var appName: String = ""
    var termsConditionUrl: String = ""
    var privacyPolicyUrl: String = ""
    var detectLiveLocation: String = ""
    var defaultCountry: String = ""
    var selectedCountry: String = ""
    var selectedCountryCode: String = ""
    var selectedStateCode: String = ""
    var selectedLanguageCode: String = ""
    var selectedLanguageDirection: String = ""
    var selectedState: String = ""
    var selectedCity: String = ""
    var selectedLanguage: String = ""
    var continueBrowsingText: String = ""
    var continueBrowsingCategoryId: String = ""
    var continueBrowsingSubCategoryId: String = ""
    var continueBrowsingImage: String = ""
    var uploadedProductLatitude: String = ""
    var uploadedProductLongitude: String = ""
    var uploadedProductAdditionalInfo: String = ""
    var appVersionFromServer : String = ""

    val isLoggedIn: Boolean
        get() = isLogin

    fun readValuesFromPreferences(context: Context?) {
        if (context != null) {
            val prefs = context.getSharedPreferences(AppConstants.PREF_FILE, Context.MODE_PRIVATE)
            this.isLogin = prefs.getBoolean(AppConstants.Companion.PREFERENCES.LOGIN_STATUS.toString(), false)
            this.isFacebookInterstitialSupported = prefs.getBoolean(AppConstants.Companion.PREFERENCES.FACEBOOK_INTERSTITIAL.toString(), false)
            this.isGoogleBannerSupported = prefs.getBoolean(AppConstants.Companion.PREFERENCES.GOOGLE_BANNER.toString(), false)
            this.isGoogleInterstitialSupported = prefs.getBoolean(AppConstants.Companion.PREFERENCES.GOOGLE_INTERSTITIAL.toString(), false)
            this.isPremiumAppSupported = prefs.getBoolean(AppConstants.Companion.PREFERENCES.PREMIUM_APP.toString(), false)
            this.isLoginFirstTime = prefs.getBoolean(AppConstants.Companion.PREFERENCES.IS_FIRST_TIME_LOGIN.toString(), true)
            this.email = prefs.getString(AppConstants.Companion.PREFERENCES.EMAIL.toString(), "")!!
            this.phoneNumber = prefs.getString(AppConstants.Companion.PREFERENCES.PHONE.toString(), "")!!
            this.displayName = prefs.getString(AppConstants.Companion.PREFERENCES.DISPLAY_NAME.toString(), "")!!
            this.userId = prefs.getString(AppConstants.Companion.PREFERENCES.USER_ID.toString(), "")!!
            this.address = prefs.getString(AppConstants.Companion.PREFERENCES.ADDRESS.toString(), "")!!
            this.token = prefs.getString(AppConstants.Companion.PREFERENCES.DEVICE_ID.toString(), "")!!
            this.userName = prefs.getString(AppConstants.Companion.PREFERENCES.USERNAME.toString(), "")!!
            this.profilePicUrl = prefs.getString(AppConstants.Companion.PREFERENCES.PROFILE_PIC.toString(), "")!!
            this.appName = prefs.getString(AppConstants.Companion.PREFERENCES.APP_NAME.toString(), "")!!
            this.termsConditionUrl = prefs.getString(AppConstants.Companion.PREFERENCES.TNC_URL.toString(), "")!!
            this.privacyPolicyUrl = prefs.getString(AppConstants.Companion.PREFERENCES.PRIVACY_URL.toString(), "")!!
            this.detectLiveLocation = prefs.getString(AppConstants.Companion.PREFERENCES.LIVE_LOCATION.toString(), "")!!
            this.defaultCountry = prefs.getString(AppConstants.Companion.PREFERENCES.DEFAULT_COUNTRY.toString(), "")!!
            this.selectedCountry = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY.toString(), "")!!
            this.selectedCountryCode = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY_CODE.toString(), "")!!
            this.selectedStateCode = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_STATE_CODE.toString(), "")!!
            this.selectedState = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(), "")!!
            this.selectedCity = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(), "")!!
            this.selectedLanguage = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(), "")!!
            this.selectedLanguageDirection = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_DIRECTION.toString(), "")!!
            this.continueBrowsingText = prefs.getString(AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_TEXT.toString(), "")!!
            this.continueBrowsingCategoryId = prefs.getString(AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_CATEGORY_ID.toString(), "")!!
            this.continueBrowsingSubCategoryId = prefs.getString(AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_SUB_CATEGORY_ID.toString(), "")!!
            this.continueBrowsingImage = prefs.getString(AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_IMAGE.toString(), "")!!
            this.selectedLanguageCode = prefs.getString(AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_CODE.toString(), "")!!
            this.appVersionFromServer = prefs.getString(AppConstants.Companion.PREFERENCES.APP_VERSION_FROM_SERVER.toString(), "")!!
            LanguagePack.instance.setLanguageData(prefs.getString(AppConstants.Companion.PREFERENCES.LANGUAGE_PACK.toString(), "")!!)
            AppConfigDetail.initialize(prefs.getString(AppConstants.Companion.PREFERENCES.APP_CONFIG.toString(), "")!!)
        }
    }

    fun saveValuesToPreferences(context: Context, prefName: String, prefValue: String) {
        if (context != null) {
            val editor = context.getSharedPreferences(AppConstants.PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putString(prefName, prefValue)
            editor.commit()
        }
    }

    fun saveBooleanToPreferences(context: Context, prefName: String, prefValue: Boolean) {
        if (context != null) {
            val editor = context.getSharedPreferences(AppConstants.PREF_FILE, Context.MODE_PRIVATE).edit()
            editor.putBoolean(prefName, prefValue)
            editor.commit()
        }
    }

    fun removePreference(context: Context) {
        if (context != null) {
            context.getSharedPreferences(AppConstants.PREF_FILE, Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    fun clearSession() {
        this.userName = ""
        this.isLogin = false
        this.email = ""
        this.displayName = ""
        this.phoneNumber = ""
        this.address = ""
        this.userId = ""
        this.token = ""
        this.profilePicUrl = ""
        this.continueBrowsingSubCategoryId = ""
        this.continueBrowsingCategoryId = ""
        this.continueBrowsingText = ""
        this.uploadedProductLatitude = ""
        this.uploadedProductLongitude = ""
        this.uploadedProductAdditionalInfo = ""
        this.appVersionFromServer = ""
    }

    companion object {

        private val TAG = SessionState::class.java.name
        private var sessionState: SessionState? = null

        val instance: SessionState
            get() {
                if (sessionState == null) {
                    sessionState = SessionState()
                }
                return sessionState!!
            }
    }


}
