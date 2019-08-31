package com.bylancer.classified.bylancerclassified.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.alarm.NotificationMessagesFragment
import com.bylancer.classified.bylancerclassified.chat.GroupChatFragment
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.settings.SettingsFragment
import com.bylancer.classified.bylancerclassified.uploadproduct.categoryselection.UploadCategorySelectionActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.utils.getCurrentCountry
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.util.*


class DashboardActivity : BylancerBuilderActivity() {
    private val dashboardFragment = DashboardFragment()
    private val notificationMessageFragment = NotificationMessagesFragment()
    private val groupChatFragment = GroupChatFragment()
    private val settingsFragment = SettingsFragment()
    val MY_PERMISSIONS_REQUEST_LOCATION = 88

    override fun setLayoutView() = R.layout.activity_dashboard

    override fun initialize(savedInstanceState: Bundle?) {
        disableShiftMode(bottom_navigation_menu)
        if (SessionState.instance.selectedCountryCode.isNullOrEmpty()) {
            SessionState.instance.selectedCountryCode = getCurrentCountry()
        }

        commitFragment(0)

        Utility.checkLocationAndPhonePermission(MY_PERMISSIONS_REQUEST_LOCATION, this)

        initializeNotification()

        setUpListeners()

        if (SessionState.instance.isGoogleBannerSupported) {
            setBannerAdListener()
            scheduleBannerAd()
        }
    }

    private fun setUpListeners() {
        google_banner_ad_close.setOnClickListener() {
            bylancer_ad_view_layout.visibility = View.GONE
        }

        bottom_navigation_menu.setOnNavigationItemSelectedListener {
            menuItem ->
                if (menuItem.itemId != R.id.action_upload_product) {
                    menuItem.isChecked = true
                }
                when(menuItem.itemId) {
                    R.id.action_home -> {
                        commitFragment(0)
                        true
                    }
                    R.id.action_alarm -> {
                        if (SessionState.instance.isLoggedIn) {
                            commitFragment(1)
                        } else  {
                            startActivity(LoginRequiredActivity::class.java, false)
                        }
                        true
                    }
                    R.id.action_upload_product -> {
                        if (SessionState.instance.isLoggedIn) {
                            startActivity(UploadCategorySelectionActivity::class.java, false)
                        } else  {
                            startActivity(LoginRequiredActivity::class.java, false)
                        }
                        true
                    }
                    R.id.action_chat -> {
                        if (SessionState.instance.isLoggedIn) {
                            commitFragment(3)
                        } else  {
                            startActivity(LoginRequiredActivity::class.java, false)
                        }
                        true
                    }
                    R.id.action_settings -> {
                        commitFragment(4)
                        true
                    }
                    else -> {
                        if (SessionState.instance.isLoggedIn) {
                            Utility.showSnackBar(dashboard_screen_parent_layout, "Work In Progress", this)
                        } else  {
                            startActivity(LoginRequiredActivity::class.java, false)
                        }
                        true
                    }
        }}
    }

    private fun initializeNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            val channelId = getString(R.string.default_notification_channel_id)
            val channelName = getString(R.string.default_notification_channel_name)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW))


            // Handle possible data accompanying notification message.
            // [START handle_data_extras]
            intent.extras?.let {
                for (key in it.keySet()) {
                    val value = intent.extras?.get(key)
                }
            }
            // [END handle_data_extras]


            FirebaseInstanceId.getInstance().instanceId
                    .addOnCompleteListener(OnCompleteListener { task ->
                        if (!task.isSuccessful) {
                            return@OnCompleteListener
                        }

                        subscribeToTopic()
                        // Get new Instance ID token
                        val token = task.result?.token
                    })

        }
    }

    private fun subscribeToTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic(getString(R.string.default_notification_topic))
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {

                    }
                }
    }

    @SuppressLint("RestrictedApi")
    fun disableShiftMode(view: BottomNavigationView) {
        val menuView = view.getChildAt(0) as BottomNavigationMenuView
        try {
            val shiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            shiftingMode.isAccessible = true
            shiftingMode.setBoolean(menuView, false)
            shiftingMode.isAccessible = false
            for (i in 0 until menuView.childCount) {
                val item : BottomNavigationItemView = menuView.getChildAt(i) as BottomNavigationItemView

                item.setShifting(false)
                // set once again checked value, so view will be updated

                item.setChecked(item.itemData.isChecked)
            }
        } catch (e: NoSuchFieldException) {
            Log.e("BNVHelper", "Unable to get shift mode field", e)
        } catch (e: IllegalAccessException) {
            Log.e("BNVHelper", "Unable to change value of shift mode", e)
        }

    }

    fun commitFragment(tabPosition : Int) {
        val fragmentTransaction = supportFragmentManager.beginTransaction();
        when(tabPosition) {
            0 -> fragmentTransaction.replace(R.id.fragment_container, dashboardFragment, dashboardFragment::class.simpleName)
            1 -> fragmentTransaction.replace(R.id.fragment_container, notificationMessageFragment, notificationMessageFragment::class.simpleName)
            3 -> fragmentTransaction.replace(R.id.fragment_container, groupChatFragment, groupChatFragment::class.simpleName)
            4 -> fragmentTransaction.replace(R.id.fragment_container, settingsFragment, settingsFragment::class.simpleName)
            else -> {}
        }
        fragmentTransaction.commit()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun scheduleBannerAd() {
        val timer = Timer()
        val bannerTask = object : TimerTask() {
            override fun run() {
                this@DashboardActivity.runOnUiThread() {
                    if (!this@DashboardActivity.isFinishing) {
                        loadBannerAd()
                    }
                }
            }
        }
        val delay = (1000 * 60 * AppConstants.BANNER_DELAY)
        timer.schedule(bannerTask, 0L, delay.toLong())
    }

    private fun loadBannerAd() {
        bylancer_ad_view?.loadAd(AdRequest.Builder().build())
    }

    private fun setBannerAdListener() {
        bylancer_ad_view?.adListener = object: AdListener() {
            override fun onAdLoaded() {
                bylancer_ad_view_layout.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                bylancer_ad_view_layout.visibility = View.GONE
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                bylancer_ad_view_layout.visibility = View.GONE
            }

            override fun onAdLeftApplication() {
                bylancer_ad_view_layout.visibility = View.GONE
            }

            override fun onAdClosed() {
                bylancer_ad_view_layout.visibility = View.GONE
            }
        }
    }
}