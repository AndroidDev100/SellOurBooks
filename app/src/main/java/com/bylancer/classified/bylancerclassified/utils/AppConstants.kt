package com.bylancer.classified.bylancerclassified.utils

import android.graphics.Color

/**
 * Created by Ani on 3/24/18.
 */
class AppConstants {
    companion object {
        const val DETAIL_ACTIVITY_PARCELABLE = "DETAIL_ACTIVITY_PARCELABLE"
        const val BUNDLE = "bundle"
        const val BASE_URL = "https://classified.bylancer.com/"
        const val REGISTER_URL = "api/v1/?action=register"
        const val LOGIN_URL = "api/v1/?action=login"
        const val FORGOT_PASSWORD_URL = "api/v1/?action=forgot_password"
        const val PRODUCT_LIST_URL =  "api/v1/index.php?action=home_latest_ads"
        const val FEATURED_URGENT_LIST_URL =  "api/v1/index.php?action=featured_urgent_ads"
        const val SEARCH_LIST_URL =  "api/v1/index.php?action=search_post"
        const val PRODUCT_DETAIL_URL = "api/v1/index.php?action=ad_detail"
        const val COUNTRY_DETAIL_URL = "api/v1/index.php?action=installed_countries"
        const val STATE_DETAIL_URL = "api/v1/index.php?action=getStateByCountryCode"
        const val CITY_DETAIL_URL = "api/v1/index.php?action=getCityByStateCode"
        const val IMAGE_URL = BASE_URL  + "storage/profile/"
        const val IMAGE_URL_SMALL = BASE_URL + "storage/profile/small_"
        const val PRODUCT_IMAGE_URL = BASE_URL + "storage/products/"
        const val APP_CONFIG_URL = "api/v1/index.php?action=app_config"
        const val FETCH_CHAT_URL = "api/v1/index.php?action=get_all_msg"
        const val MAKE_AN_OFFER = "api/v1/?action=make_offer"
        const val FETCH_GROUP_CHAT_URL = "api/v1/?action=chat_conversation"
        const val FETCH_LANGUAGE_PACK_URL = "api/v1/index.php?action=language_file"
        const val SEND_CHAT_URL = "api/v1/index.php?action=send_message"
        const val GET_NOTIFICATION_MESSAGE_URL = "api/v1/index.php?action=get_notification"
        const val ADD_FIREBASE_DEVICE_TOKEN_URL = "api/v1/index.php?action=add_firebase_device_token"
        const val UPLOAD_PROFILE_PIC_URL = "api/v1/index.php?action=upload_profile_picture"
        const val UPLOAD_PRODUCT_PIC_URL = "api/v1/index.php?action=upload_product_picture"
        const val UPLOAD_PRODUCT_SAVE_POST_URL = "api/v1/index.php?action=save_post"
        const val UPLOAD_PRODUCT_ADDITIONAL_INFO_URL = BASE_URL + "api/v1/?action=getCustomFieldByCatID&catid=%s&subcatid=%s&additionalinfo=%s"
        const val FLAG_IMAGE_URL = "https://www.countryflags.io/%s/flat/64.png"
        const val IS_ADMIN_APP = false
        const val IS_APP_CONFIG_RELOAD_REQUIRED = true // UPDATE IT TO FALSE IF YOU DON'T WANT TO RELOAD YOUR CATEGORIES EVERY LAUNCH
        val CURRENT_VERSION = "1.0"
        val CURRENCY_IN_LEFT= "0"
        val PREF_FILE = "bylancer_appsgeek"
        val SUPPORT_EMAIL = "apps.bylancer@gmail.com"
        val PRODUCT_LOADING_LIMIT = "36"
        val PRODUCT_STATUS = "active"

        const val ERROR = "error"
        const val SUCCESS = "success"
        const val MESSAGE = "message"
        const val PRODUCT_ID = "product_id"
        const val PRODUCT_NAME = "product_name"
        const val PRODUCT_OWNER_NAME = "product_owner_name"
        const val TERMS_CONDITION_TITLE = "terms_condition_title"
        const val TERMS_CONDITION_URL = "terms_condition_url"
        const val HIDE_PHONE = "yes"
        const val YES = "yes"
        const val NO = "no"
        const val IS_ACTIVE = "1"
        const val CHAT_USER_NAME = "chat_user_name"
        const val CHAT_TITLE = "chat_title"
        const val CHAT_USER_IMAGE = "chat_user_image"
        const val CHAT_USER_ID = "chat_user_id"
        const val ASSET_TYPE_TEXT = "text"
        const val EMPTY = ""
        const val ZERO = "0"
        const val CATEGORY = "category"
        const val SUB_CATEGORY = "sub_category"
        const val DATABASE_NAME = "property_app_db"
        const val DATABASE_VERSION = 5
        const val IMAGE_PICKER_FRAGMENT = "picker"
        const val SELECTED_CATEGORY_POSITION = "selected_category_position"
        const val SELECTED_SUB_CATEGORY_ID = "selected_sub_category_id"
        const val SELECTED_CATEGORY_ID = "selected_category_id"
        const val SELECTED_PRODUCT_LONGITUDE = "selected_product_longitude"
        const val SELECTED_PRODUCT_LATITUDE = "selected_product_latitude"
        const val UPLOAD_PRODUCT_SELECTED_TITLE = "UPLOAD_PRODUCT_SELECTED_TITLE"
        const val ADDITIONAL_INFO_ACTIVITY_TITLE = "ADDITIONAL_INFO_ACTIVITY_TITLE"
        const val DIRECTION_RTL = "rtl"

        const val BANNER_DELAY = 2.0
        const val INTERSTITIAL_DELAY = 3.0
        const val FACEBOOK_INTERSTITIAL_PLACEMENT = "2244587745860156_2351931271792469"

        const val DASHBOARD_PULL_TO_REFRESH_COLOR = 0xFFCA0009
        const val SEARCH_PULL_TO_REFRESH_COLOR = 0xFF313C3F
        const val PULL_TO_REFRESH_COLOR_SCHEME = Color.WHITE

        enum class PREFERENCES private constructor(private val value: String) {
            USER_ID("user_id"),
            DEVICE_ID("deviceId"),
            USERNAME("username"),
            LOGIN_STATUS("LoginStatus"),
            EMAIL("email_id"),
            PHONE("phone"),
            ADDRESS("address"),
            DISPLAY_NAME("DisplayName"),
            APP_NAME("app_name"),
            PRIVACY_URL("privacy_url"),
            DEFAULT_COUNTRY("default_country"),
            SELECTED_COUNTRY("selected_country"),
            SELECTED_COUNTRY_CODE("selected_country_code"),
            SELECTED_STATE("selected_state"),
            SELECTED_CITY("selected_city"),
            SELECTED_LANGUAGE("selected_Language"),
            SELECTED_LANGUAGE_DIRECTION("selected_Language_direction"),
            LIVE_LOCATION("live_location"),
            TNC_URL("tnc_url"),
            PROFILE_PIC("profile_pic"),
            LANGUAGE_PACK("language_pack"),
            COUNTRY_LIST("country_list"),
            STATE_LIST("state_list"),
            CITY_LIST("city_list"),
            CONTINUE_BROWSING_TEXT("continue_browsing"),
            CONTINUE_BROWSING_CATEGORY_ID("continue_browsing_category"),
            CONTINUE_BROWSING_IMAGE("continue_browsing_image"),
            SELECTED_LANGUAGE_CODE("selected_language_code_details"),
            CONTINUE_BROWSING_SUB_CATEGORY_ID("continue_browsing_sub_category"),
            APP_CONFIG("app_config_details"),
            APP_VERSION_FROM_SERVER("app_version_server"),
            PREMIUM_APP("premium_app"),
            IS_FIRST_TIME_LOGIN("is_first_time_login"),
            GOOGLE_BANNER("google_banner"),
            GOOGLE_INTERSTITIAL("google_interstitial"),
            FACEBOOK_INTERSTITIAL("facebook_interstitial"),
            DATABASE_DETAIL("database_detail");

            override fun toString(): String {
                return this.value
            }
        }
    }
}