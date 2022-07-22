package com.bylancer.classified.bylancerclassified.settings


import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.asksira.bsimagepicker.BSImagePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigModel
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.fragments.BylancerBuilderFragment
import com.bylancer.classified.bylancerclassified.login.LoginActivity
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.login.TermsAndConditionWebView
import com.bylancer.classified.bylancerclassified.member.MembershipUpgradeViewActivity
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.membership.CurrentUserMembershipPlan
import com.bylancer.classified.bylancerclassified.webservices.settings.CityListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.CountryListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.ProductUploadProductModel
import com.bylancer.classified.bylancerclassified.webservices.settings.StateListModel
import com.gmail.samehadar.iosdialog.IOSDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_settings.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

/**
 * A settings [Fragment] class.
 *
 */
class SettingsFragment : BylancerBuilderFragment(), View.OnClickListener, BSImagePicker.OnSingleImageSelectedListener {
    private var mProgressDialog: IOSDialog? = null
    val countryList = arrayListOf<CountryListModel>()
    val stateList = arrayListOf<StateListModel>()
    val cityList = arrayListOf<CityListModel>()
    val PROFILE_IMAGE_PICKER = "profile_image_picker"
    var isFromMembershipScreen = false

    override fun setLayoutView() = R.layout.fragment_settings

    override fun initialize(savedInstanceState: Bundle?) {
        initializeTextViewsWithLanguagePack(null)

        settings_login_sign_up_frame.setOnClickListener(this)
        settings_terms_condition_frame.setOnClickListener(this)
        settings_my_fav_frame.setOnClickListener(this)
        settings_my_ads_frame.setOnClickListener(this)
        settings_support_frame.setOnClickListener(this)
        profile_icon_image_view.setOnClickListener(this)
        go_premium_button.setOnClickListener(this)
        if (SessionState.instance.isLoggedIn) {
            settings_login_sign_up_icon.setImageResource(R.drawable.ic_settings_logout)
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.log_out))
            if (!SessionState.instance.profilePicUrl.isNullOrEmpty()) {
                Glide.with(profile_icon_image_view.context).load(SessionState.instance.profilePicUrl).apply(RequestOptions().circleCrop()).into(profile_icon_image_view);
            }
            if (SessionState.instance.isLoggedIn) {
                go_premium_button.visibility = View.VISIBLE
                go_premium_button.text = LanguagePack.getString(getString(R.string.membership))
            }
        } else {
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.login_sign_up))
        }

        settings_country_spinner.setHintTextColor(resources.getColor(R.color.light_gray)) //change title of spinner-dialog programmatically
        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedCountry, true)) {
            settings_country_spinner.setText(SessionState.instance.selectedCountry)
        }
        settings_country_spinner.setExpandTint(R.color.transparent)
        settings_country_spinner.setOnItemClickListener{ position ->
            if (countryList.get(position) != null) {
                SessionState.instance.selectedCountry = countryList[position].name!!
                SessionState.instance.selectedCountryCode = countryList[position].lowercaseCode!!
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY.toString(),
                        countryList[position].name!!)
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY_CODE.toString(),
                        countryList[position].lowercaseCode!!)
                SessionState.instance.selectedState = ""
                SessionState.instance.selectedCity = ""
                SessionState.instance.selectedStateCode = ""
                SessionState.instance.selectedCityId = ""
                settings_state_spinner.setText("")
                settings_city_spinner.setText("")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE_CODE.toString(),
                        "")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY_CODE.toString(),
                        "")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                        "")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                        "")
                stateList?.clear()
                saveStateDetailData()
                fetchStateList(countryList.get(position).lowercaseCode!!)
            }
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedState)) {
            settings_state_spinner.setText(SessionState.instance.selectedState)
        }

        settings_state_spinner.setOnItemClickListener{ position ->
            SessionState.instance.selectedState = if (stateList[position] != null) stateList[position].name!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                    SessionState.instance.selectedState)
            SessionState.instance.selectedStateCode = if (stateList[position] != null) stateList[position].code!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE_CODE.toString(),
                    SessionState.instance.selectedStateCode)
            settings_city_spinner.setText("")
            SessionState.instance.selectedCity = ""
            SessionState.instance.selectedCityId = ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                    "")
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY_CODE.toString(),
                    "")
            cityList?.clear()
            saveCityDetailData()
            fetchCityList(stateList.get(position).code!!)
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedCity)) {
            settings_city_spinner.setText(SessionState.instance.selectedCity)
        }

        settings_city_spinner.setOnItemClickListener{ position ->
            SessionState.instance.selectedCityId = if (cityList[position] != null) cityList[position].id!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY_CODE.toString(),
                    SessionState.instance.selectedCityId )
            SessionState.instance.selectedCity = if (cityList[position] != null) cityList[position].name!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                    SessionState.instance.selectedCity)
        }

        if (isAdded && isVisible) getUserMembershipPlan()
    }

    private fun fetchLanguagePackDetails() {
        context?.let {
            val response = getJsonDataFromAsset(it)
            if (response != null) {
                LanguagePack.instance.saveLanguageData(it, response)
                LanguagePack.instance.setLanguageData(response)
                initializeLanguagePack()
            }
        }
    }

    private fun initializeTextViewsWithLanguagePack(languageCode: String?) {
        if (!languageCode.isNullOrEmpty()) {
            refreshCategoriesWithLanguageCode(languageCode)
        }
        appCompatTextView.text = LanguagePack.getString(getString(R.string.my_account))
        settings_my_ads_text.text = LanguagePack.getString(getString(R.string.my_ads))
        settings_my_fav_text.text = LanguagePack.getString(getString(R.string.my_favorites))
        settings_my_saved_search_text.text = LanguagePack.getString(getString(R.string.my_saved_search))
        settings_caption_text_view.text = LanguagePack.getString(getString(R.string.settings))
        settings_country_text.text = LanguagePack.getString(getString(R.string.country))
        settings_state_text.text = LanguagePack.getString(getString(R.string.state))
        settings_city_text.text = LanguagePack.getString(getString(R.string.city))
        settings_language_text.text = LanguagePack.getString(getString(R.string.language))
        settings_support_text.text = LanguagePack.getString(getString(R.string.support))
        settings_terms_condition_text.text = LanguagePack.getString(getString(R.string.terms_condition))
        settings_language_spinner.hint = LanguagePack.getString(getString(R.string.select_language))
        settings_country_spinner.hint = LanguagePack.getString(getString(R.string.select_country))
        settings_state_spinner.hint = LanguagePack.getString(getString(R.string.select_state))
        settings_city_spinner.hint = LanguagePack.getString(getString(R.string.select_city))
        appCompatTextView.text = LanguagePack.getString(getString(R.string.my_account))
        if (SessionState.instance.isLoggedIn) {
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.log_out))
        } else {
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.login_sign_up))
        }
    }

    private fun initializeLanguagePack() {
        settings_language_spinner?.hint = LanguagePack.getString(getString(R.string.select_language))
        settings_language_spinner?.setOnItemClickListener{ position ->
            if(LanguagePack.instance.languagePackData != null) {
                SessionState.instance.selectedLanguage = if (LanguagePack.instance.languagePackData?.get(position)?.language != null) LanguagePack.instance.languagePackData?.get(position)?.language!! else ""
                SessionState.instance.selectedLanguageCode = if (LanguagePack.instance.languagePackData?.get(position)?.languageCode != null) LanguagePack.instance.languagePackData?.get(position)?.languageCode!! else ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                        SessionState.instance.selectedLanguage)
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_CODE.toString(),
                        SessionState.instance.selectedLanguageCode)
                if (SessionState.instance.selectedLanguageDirection.equals(LanguagePack.instance.languagePackData?.get(position)?.direction, true)) {
                    initializeTextViewsWithLanguagePack(SessionState.instance.selectedLanguageCode)
                } else {
                    SessionState.instance.selectedLanguageDirection = if (LanguagePack.instance.languagePackData?.get(position)?.direction != null) LanguagePack.instance.languagePackData?.get(position)?.direction!! else ""
                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_DIRECTION.toString(),
                            SessionState.instance.selectedLanguageDirection)
                    if (!SessionState.instance.selectedLanguageCode.isNullOrEmpty()) {
                        refreshCategoriesWithLanguageCode(SessionState.instance.selectedLanguageCode, true)
                    }
                }
            } else  {
                SessionState.instance.selectedLanguage = "English"
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                        "English")
            }
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedLanguage, true)) {
            settings_language_spinner?.setText(SessionState.instance.selectedLanguage)
        }

        var languageList = arrayListOf<String?>()
        if (LanguagePack.instance.languagePackData != null) {
            for (x in LanguagePack.instance.languagePackData!!) {
                languageList.add(x.language)
            }
        } else  {
            languageList.add("English")
        }
        settings_language_spinner?.setItems(languageList.toArray(arrayOfNulls<String>(languageList.size))) //this is important, you must set it to set the item list
        settings_language_spinner?.setHintTextColor(resources.getColor(R.color.light_gray))
        settings_language_spinner?.setExpandTint(R.color.transparent)
        settings_language_spinner?.setExpandTint(R.color.transparent)

        if (settings_country_spinner?.text.isNullOrEmpty()) {
            fetchCountryList() // To load only first time
        } else {
            AppConfigDetail.loadLocationDetail(mContext)
            if (!AppConfigDetail.countryList.isNullOrEmpty()) {
                countryList?.clear()
                countryList?.addAll(AppConfigDetail.countryList!!)
                addCountryNameToCountrySpinner()
            }
            if (!AppConfigDetail.stateList.isNullOrEmpty()) {
                stateList?.clear()
                stateList?.addAll(AppConfigDetail.stateList!!)
                addStateNameToStateSpinner()
            }
            if (!AppConfigDetail.cityList.isNullOrEmpty()) {
                cityList?.clear()
                cityList?.addAll(AppConfigDetail.cityList!!)
                addCityNameToCitySpinner()
            }
        }
    }

    private fun fetchCountryList() {
        showProgressDialog(getString(R.string.fetch_country))
        RetrofitController.fetchCountryDetails(object: Callback<List<CountryListModel>> {
            override fun onResponse(call: Call<List<CountryListModel>>?, response: Response<List<CountryListModel>>?) {
                if(isAdded && isVisible) {
                    if (response != null && response.isSuccessful && settings_country_spinner != null && response.body() != null) {
                        countryList.addAll(response.body()!!)
                        saveCountryDetailData()
                    }
                    dismissProgressDialog()
                }
            }

            override fun onFailure(call: Call<List<CountryListModel>>?, t: Throwable?) {
                if(isAdded && isVisible) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun fetchStateList(countryId: String) {
        showProgressDialog(getString(R.string.fetch_state))
        RetrofitController.fetchStateDetails(countryId, object: Callback<List<StateListModel>> {
            override fun onResponse(call: Call<List<StateListModel>>?, response: Response<List<StateListModel>>?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful && settings_state_spinner != null && response.body() != null) {
                        stateList.addAll(response.body()!!)
                        saveStateDetailData()
                    }
                }
            }

            override fun onFailure(call: Call<List<StateListModel>>?, t: Throwable?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun fetchCityList(stateId: String) {
        showProgressDialog(getString(R.string.fetch_city))
        RetrofitController.fetchCityDetails(stateId, object: Callback<List<CityListModel>> {
            override fun onResponse(call: Call<List<CityListModel>>?, response: Response<List<CityListModel>>?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful && settings_city_spinner != null && response.body() != null) {
                        cityList.addAll(response.body()!!)
                        saveCityDetailData()
                    }
                }
            }

            override fun onFailure(call: Call<List<CityListModel>>?, t: Throwable?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.settings_login_sign_up_frame ->
                if (SessionState.instance.isLoggedIn) {
                    showLogoutAlertDialog()
                } else  {
                    startActivity(LoginActivity::class.java, false)
                }
            R.id.settings_terms_condition_frame -> {
                loadTermsAndConditionWebView(R.string.terms_condition, if (SessionState.instance.termsConditionUrl != null) SessionState.instance.termsConditionUrl else "")
            }
            R.id.settings_support_frame -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", AppConstants.SUPPORT_EMAIL, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding ");
                //emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi, \n \n"+ LanguagePack.getString("I am interested in your property") + " " + product_detail_title_text_view.text + ".\n "+ LanguagePack.getString("We can have discussion on") +" \n\n" + LanguagePack.getString("Regards")+",\n"+ SessionState.instance.displayName);
                startActivityForResult(Intent.createChooser(emailIntent,  LanguagePack.getString("Send email...")), 0);
            }
            R.id.settings_my_fav_frame -> {
                if (SessionState.instance.isLoggedIn) {
                    startActivity(MyFavoritesActivity::class.java, false)
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.settings_my_ads_frame -> {
                if (SessionState.instance.isLoggedIn) {
                    startActivity(MyPostedProductActivity::class.java, false)
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.profile_icon_image_view -> {
                if (SessionState.instance.isLoggedIn) {
                    val profileImagePicker = getSingleImagePickerDialog(PROFILE_IMAGE_PICKER)
                    profileImagePicker.show(childFragmentManager, AppConstants.IMAGE_PICKER_FRAGMENT)
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.go_premium_button -> {
                startPremiumFlow()
            }
            else -> { }
        }
    }

    private fun startPremiumFlow() {
        if (mContext != null) {
            isFromMembershipScreen = true
            startActivity(MembershipUpgradeViewActivity::class.java, false)
        }
    }

    private fun logoutUser() {
        SessionState.instance.clearSession()
        SessionState.instance.removePreference(activity!!)
        startActivity(LoginActivity::class.java, true)
        this.activity?.finish()
    }

    private fun showNetworkErrorSnackBar() {
        if (fragment_settings_user_parent_layout != null) Utility.showSnackBar(fragment_settings_user_parent_layout, "Please check your internet connection", context!!)
    }

    private fun showProgressDialog(message: String) {
        mProgressDialog = Utility.showProgressView(context!!, message)
        mProgressDialog?.show()
    }

    fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }

    private fun showLogoutAlertDialog() {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("")
        builder.setMessage(LanguagePack.getString("Are you sure you want to log out"))
        builder.setPositiveButton(LanguagePack.getString("YES")){dialog, _ ->
            dialog.dismiss()
            logoutUser()
        }
        builder.setNegativeButton(LanguagePack.getString("NO")){dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun loadTermsAndConditionWebView(titleId: Int, url: String) {
        var bundle = Bundle()
        bundle.putString(AppConstants.TERMS_CONDITION_TITLE, LanguagePack.getString(getString(titleId)))
        bundle.putString(AppConstants.TERMS_CONDITION_URL, url)
        startActivity(TermsAndConditionWebView ::class.java, false, bundle)
    }

    override fun onSingleImageSelected(uri: Uri?, tag: String?) {
        if (profile_icon_image_view != null && uri != null) {
            showProgressDialog("Uploading...")
            Glide.with(profile_icon_image_view.context).load(uri).apply(RequestOptions().circleCrop()).into(profile_icon_image_view)
            val file = File(uri.path)
            if (file != null) {
                val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
                // MultipartBody.Part is used to send also the actual file name
                val body = MultipartBody.Part.createFormData("fileToUpload", file.getName(), requestFile)
                RetrofitController.updateUserProfilePic(body, object: Callback<ProductUploadProductModel> {
                    override fun onFailure(call: Call<ProductUploadProductModel>?, t: Throwable?) {
                        if (isAdded && isVisible) {
                            dismissProgressDialog()
                            Utility.showSnackBar(fragment_settings_user_parent_layout, getString(R.string.internet_issue), context!!)
                        }
                    }

                    override fun onResponse(call: Call<ProductUploadProductModel>?, response: Response<ProductUploadProductModel>?) {
                        if (isAdded && isVisible) {
                            if (response != null && response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && AppConstants.SUCCESS.equals(responseBody.status) && context != null) {
                                    SessionState.instance.profilePicUrl = responseBody.url!!
                                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.PROFILE_PIC.toString(),
                                            SessionState.instance.profilePicUrl)
                                }
                            } else {
                                Utility.showSnackBar(fragment_settings_user_parent_layout, getString(R.string.some_wrong), context!!)
                            }
                            dismissProgressDialog()
                        }
                    }
                })
            } else {
                dismissProgressDialog()
            }
        }
    }

    private fun refreshCategoriesWithLanguageCode(languageCode: String, isRecreateActivityRequired : Boolean = false) {
        showProgressDialog(getString(R.string.loading))
        RetrofitController.fetchAppConfig(languageCode, object : Callback<AppConfigModel> {
            override fun onFailure(call: Call<AppConfigModel>?, t: Throwable?) {
                if (isAdded && isVisible) {
                    if (context != null && Utility.isNetworkAvailable(context!!)) {
                        refreshCategoriesWithLanguageCode(languageCode)
                    } else {
                        dismissProgressDialog()
                    }
                }
            }

            override fun onResponse(call: Call<AppConfigModel>?, response: Response<AppConfigModel>?) {
                if (isAdded && isVisible) {
                    if (context != null && response != null && response.isSuccessful && response.body() != null) {
                        val appConfigUrl: AppConfigModel = response.body()!!
                        AppConfigDetail.saveAppConfigData(context!!, Gson().toJson(appConfigUrl))
                        AppConfigDetail.initialize(Gson().toJson(appConfigUrl))
                        dismissProgressDialog()
                        if (isRecreateActivityRequired) {
                            //this@SettingsFragment.activity?.recreate()
                            recreateView()
                        }
                    }
                }
            }

        })
    }

    private fun saveCountryDetailData() {
        if (!countryList.isNullOrEmpty() && isAdded && isVisible && mContext != null) {
            AppConfigDetail.countryList = countryList
            AppConfigDetail.saveCountryListData(mContext!!, Gson().toJson(countryList))
            addCountryNameToCountrySpinner()
        }
    }

    private fun addCountryNameToCountrySpinner() {
        if (countryList != null) {
            val listOfCountry = arrayListOf<String>()
            countryList.forEach { listOfCountry.add(it.name!!)}
            settings_country_spinner.setItems(listOfCountry.toArray(arrayOfNulls<String>(countryList.size)))
        }
    }

    private fun saveStateDetailData() {
        if (!stateList.isNullOrEmpty() && isAdded && isVisible && mContext != null) {
            AppConfigDetail.stateList = stateList
            AppConfigDetail.saveSateListData(mContext!!, Gson().toJson(stateList))
            addStateNameToStateSpinner()
        } else {
            AppConfigDetail.stateList = null
            AppConfigDetail.saveSateListData(mContext!!, "")
            addStateNameToStateSpinner()
        }
    }

    private fun addStateNameToStateSpinner() {
        if (stateList != null) {
            val listOfState = arrayListOf<String>()
            stateList.forEach { listOfState.add(it.name!!)}
            settings_state_spinner.setItems(listOfState.toArray(arrayOfNulls<String>(stateList.size)))
        }
    }

    private fun saveCityDetailData() {
        if (!cityList.isNullOrEmpty() && isAdded && isVisible && mContext != null) {
            AppConfigDetail.cityList = cityList
            AppConfigDetail.saveCityListData(mContext!!, Gson().toJson(cityList))
            addCityNameToCitySpinner()
        } else {
            AppConfigDetail.cityList = null
            AppConfigDetail.saveCityListData(mContext!!, "")
            addCityNameToCitySpinner()
        }
    }

    private fun addCityNameToCitySpinner() {
        if (cityList != null) {
            val listOfCity = arrayListOf<String>()
            cityList.forEach { listOfCity.add(it.name!!)}
            settings_city_spinner.setItems(listOfCity.toArray(arrayOfNulls<String>(cityList.size)))
        }
    }

    private fun getUserMembershipPlan(isLanguageFetchRequired: Boolean = true) {
        showProgressDialog(LanguagePack.getString(getString(R.string.loading)))
        RetrofitController.fetchCurrentUserMembershipPlan(object : Callback<CurrentUserMembershipPlan>{
            override fun onFailure(call: Call<CurrentUserMembershipPlan>?, t: Throwable?) {
                dismissProgressDialog()
                if (isLanguageFetchRequired) initLanguagePack()
            }

            override fun onResponse(call: Call<CurrentUserMembershipPlan>?, response: Response<CurrentUserMembershipPlan>?) {
                if (response != null && response.isSuccessful && response.body() != null && !response!!.body()!!.imageUrl.isNullOrEmpty()) {
                    badge_icon_image_view.visibility = View.VISIBLE
                    Glide.with(badge_icon_image_view.context).load(response!!.body()!!.imageUrl).into(badge_icon_image_view)
                }
                dismissProgressDialog()
                if (isLanguageFetchRequired) initLanguagePack()
            }

        })
    }

    private fun initLanguagePack() {
        if (isAdded && isVisible) {
            if (LanguagePack.instance.languagePackData == null) {
                fetchLanguagePackDetails()
            } else {
                initializeLanguagePack()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isFromMembershipScreen) {
            isFromMembershipScreen != isFromMembershipScreen
            getUserMembershipPlan(false)
        }
    }

    private fun recreateView() {
        startActivity(DashboardActivity::class.java, true)
        this@SettingsFragment.activity?.finishAffinity()
    }
}
