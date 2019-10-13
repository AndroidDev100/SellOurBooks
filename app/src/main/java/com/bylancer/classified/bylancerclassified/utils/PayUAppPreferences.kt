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

        enum class AppEnvironment {

            SANDBOX {
                override fun merchant_Key(): String {
                    return "gukapaRa"
                }

                override fun merchant_ID(): String {
                    return "6808616"
                }

                override fun furl(): String {
                    return "https://www.payumoney.com/mobileapp/payumoney/failure.php"
                }

                override fun surl(): String {
                    return "https://www.payumoney.com/mobileapp/payumoney/success.php"
                }

                override fun salt(): String {
                    return "nYB3IRFB35"
                }

                override fun debug(): Boolean {
                    return true
                }
            },
            PRODUCTION {
                override fun merchant_Key(): String {
                    return "QylhKRVd"
                }

                override fun merchant_ID(): String {
                    return "5960507"
                }

                override fun furl(): String {
                    return "https://www.payumoney.com/mobileapp/payumoney/failure.php"
                }

                override fun surl(): String {
                    return "https://www.payumoney.com/mobileapp/payumoney/success.php"
                }

                override fun salt(): String {
                    return "seVTUgzrgE"
                }

                override fun debug(): Boolean {
                    return false
                }
            };

            abstract fun merchant_Key(): String

            abstract fun merchant_ID(): String

            abstract fun furl(): String

            abstract fun surl(): String

            abstract fun salt(): String

            abstract fun debug(): Boolean
        }
    }
}
