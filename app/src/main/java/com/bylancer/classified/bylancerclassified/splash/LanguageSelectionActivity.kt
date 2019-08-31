package com.bylancer.classified.bylancerclassified.splash

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigModel
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.gmail.samehadar.iosdialog.IOSDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_language_selection.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LanguageSelectionActivity : BylancerBuilderActivity(), LanguageSelection {
    var mProgressDialog: IOSDialog? = null

    override fun setLayoutView() = R.layout.activity_language_selection

    override fun initialize(savedInstanceState: Bundle?) {
        language_selection_app_name_text_view.text = if (SessionState.instance.appName != null) SessionState.instance.appName else getString(R.string.app_name)
        language_selection_sub_title_text_view.text = LanguagePack.getString(getString(R.string.choose_language))

        language_list_recycler_view.layoutManager = LinearLayoutManager(this)
        language_list_recycler_view.setHasFixedSize(false)
        language_list_recycler_view.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation)
        if (!LanguagePack.instance.languagePackData.isNullOrEmpty()) {
            language_list_recycler_view.adapter = LanguageSelectionAdapter(LanguagePack.instance.languagePackData!!, this@LanguageSelectionActivity)
        }
    }

    override fun onLanguageSelected(languageString: String?, languageCode : String?, languageDirection : String?) {
        SessionState.instance.selectedLanguage = languageString ?: ""
        SessionState.instance.selectedLanguageCode = languageCode ?: ""
        SessionState.instance.selectedLanguageDirection = languageDirection ?: ""
        SessionState.instance.saveValuesToPreferences(this@LanguageSelectionActivity, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                SessionState.instance.selectedLanguage)
        SessionState.instance.saveValuesToPreferences(this@LanguageSelectionActivity, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_CODE.toString(),
                SessionState.instance.selectedLanguageCode)
        SessionState.instance.saveValuesToPreferences(this@LanguageSelectionActivity, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_DIRECTION.toString(),
                SessionState.instance.selectedLanguageDirection)
        refreshCategoriesWithLanguageCode(SessionState.instance.selectedLanguageCode)
    }

    private fun refreshCategoriesWithLanguageCode(languageCode: String) {
        showProgressDialog(getString(R.string.loading))
        RetrofitController.fetchAppConfig(languageCode, object : Callback<AppConfigModel> {
            override fun onFailure(call: Call<AppConfigModel>?, t: Throwable?) {
                if (!this@LanguageSelectionActivity.isFinishing) {
                    dismissProgressDialog()
                }
            }

            override fun onResponse(call: Call<AppConfigModel>?, response: Response<AppConfigModel>?) {
                if (!this@LanguageSelectionActivity.isFinishing && response != null && response.isSuccessful) {
                    val appConfigUrl: AppConfigModel = response.body()
                    AppConfigDetail.saveAppConfigData(this@LanguageSelectionActivity, Gson().toJson(appConfigUrl))
                    AppConfigDetail.initialize(Gson().toJson(appConfigUrl))
                    dismissProgressDialog()
                }
            }

        })
    }

    private fun showProgressDialog(message: String) {
        mProgressDialog = Utility.showProgressView(this@LanguageSelectionActivity, message)
        mProgressDialog?.show()
    }

    private fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
            startActivity(DashboardActivity :: class.java, false)
            finish()
        }
    }
}



