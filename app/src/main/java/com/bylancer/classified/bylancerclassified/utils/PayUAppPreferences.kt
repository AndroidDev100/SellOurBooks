package com.bylancer.classified.bylancerclassified.utils

class PayUAppPreferences {

    companion object {
        var dummyAmount = "10"//"10";
        var dummyEmail = "xyz@gmail.com"//"";//d.basak.db@gmail.com
        var productInfo = "product_info"// "product_info";
        var firstName = "firstname" //"firstname";
        var isOverrideResultScreen = true

        var isDisableWallet: Boolean = false
        var isDisableSavedCards: Boolean = false
        var isDisableNetBanking: Boolean = false
        var isDisableThirdPartyWallets: Boolean = false
        var isDisableExitConfirmation: Boolean = false

        val USER_EMAIL = "user_email"
        val USER_MOBILE = "user_mobile"
        val PHONE_PATTERN = "^[987]\\d{9}$"
        val MENU_DELAY: Long = 300
        var USER_DETAILS = "user_details"
        var selectedTheme = -1
    }
}
