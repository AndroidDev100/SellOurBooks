package com.bylancer.classified.bylancerclassified

import android.app.Application
import android.os.StrictMode
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.crashlytics.android.Crashlytics
import com.facebook.FacebookSdk
import com.facebook.ads.AudienceNetworkAds
import com.google.android.gms.ads.MobileAds
//import com.google.android.gms.ads.MobileAds
import io.fabric.sdk.android.Fabric

/**
 * Created by Ani on 1/13/19.
 */

class BylancerClassified : Application() {
    private val TAG = BylancerClassified::class.java!!.getSimpleName()

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        Fabric.with(this, Crashlytics())
        logUserToCrashlytics()
        FacebookSdk.sdkInitialize(applicationContext)
        MobileAds.initialize(this)
        // for exposed beyond app through ClipData.Item.getUri() issues
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        // Initialize the Audience Network SDK
        AudienceNetworkAds.initialize(this);
    }

    companion object {
        var mInstance: BylancerClassified? = null

        @Synchronized
        fun getInstance(): BylancerClassified {
            return mInstance!!
        }
    }

    private fun logUserToCrashlytics() {
        // TODO: Use the current user's information
        // You can call any combination of these three methods
        if (SessionState.instance.isLoggedIn) {
            Crashlytics.setUserIdentifier(SessionState.instance.userId)
            Crashlytics.setUserEmail(SessionState.instance.email)
            Crashlytics.setUserName(SessionState.instance.userName)
        }
    }

}
