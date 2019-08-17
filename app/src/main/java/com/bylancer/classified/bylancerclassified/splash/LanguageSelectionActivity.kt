package com.bylancer.classified.bylancerclassified.splash

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.LinearLayoutManager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import kotlinx.android.synthetic.main.activity_language_selection.*

class LanguageSelectionActivity : BylancerBuilderActivity(), LanguageSelection {

    override fun setLayoutView() = R.layout.activity_language_selection

    override fun initialize(savedInstanceState: Bundle?) {
        language_selection_app_name_text_view.text = if (SessionState.instance.appName != null) SessionState.instance.appName else getString(R.string.app_name)
        language_selection_sub_title_text_view.text = LanguagePack.getString(getString(R.string.choose_language))

        language_list_recycler_view.layoutManager = LinearLayoutManager(this)
        language_list_recycler_view.setHasFixedSize(false)
        language_list_recycler_view.layoutAnimation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation)
        if (!AppConfigDetail.languageList.isNullOrEmpty()) {
            language_list_recycler_view.adapter = LanguageSelectionAdapter(AppConfigDetail.languageList!!, this@LanguageSelectionActivity)
        }
    }

    override fun onLanguageSelected(languageString: String?, languageCode : String?) {
        SessionState.instance.selectedLanguage = languageString ?: ""
        SessionState.instance.selectedLanguageCode = languageCode ?: ""
        SessionState.instance.saveValuesToPreferences(this@LanguageSelectionActivity, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                SessionState.instance.selectedLanguage)
        SessionState.instance.saveValuesToPreferences(this@LanguageSelectionActivity, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_CODE.toString(),
                SessionState.instance.selectedLanguageCode)
        startActivity(DashboardActivity :: class.java, false)
        finish()
    }

}