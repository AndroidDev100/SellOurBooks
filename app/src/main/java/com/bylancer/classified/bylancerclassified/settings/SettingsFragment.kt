package com.bylancer.classified.bylancerclassified.settings


import android.app.Fragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.asksira.bsimagepicker.BSImagePicker
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigModel
import com.bylancer.classified.bylancerclassified.fragments.BylancerBuilderFragment
import com.bylancer.classified.bylancerclassified.login.LoginActivity
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.login.TermsAndConditionWebView
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.settings.CityListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.CountryListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.ProductUploadProductModel
import com.bylancer.classified.bylancerclassified.webservices.settings.StateListModel
import com.gmail.samehadar.iosdialog.IOSDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_settings.*
import okhttp3.MediaType
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
    var mProgressDialog: IOSDialog? = null
    val countryList = arrayListOf<CountryListModel>()
    val stateList = arrayListOf<StateListModel>()
    val cityList = arrayListOf<CityListModel>()
    var isKeyboardOpen: Boolean = false
    val PROFILE_IMAGE_PICKER = "profile_image_picker"

    override fun setLayoutView() = R.layout.fragment_settings

    override fun initialize(savedInstanceState: Bundle?) {
        initializeTextViewsWithLanguagePack(null)

        settings_login_sign_up_frame.setOnClickListener(this)
        settings_terms_condition_frame.setOnClickListener(this)
        settings_my_fav_frame.setOnClickListener(this)
        settings_my_ads_frame.setOnClickListener(this)
        settings_support_frame.setOnClickListener(this)
        profile_icon_image_view.setOnClickListener(this)
        if (SessionState.instance.isLoggedIn) {
            settings_login_sign_up_icon.setImageResource(R.drawable.ic_settings_logout)
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.log_out))
            if (!SessionState.instance.profilePicUrl.isNullOrEmpty()) {
                Glide.with(profile_icon_image_view.context).load(SessionState.instance.profilePicUrl).apply(RequestOptions().circleCrop()).into(profile_icon_image_view);
            }
        } else {
            settings_login_sign_up_text.text = LanguagePack.getString(getString(R.string.login_sign_up))
        }

        settings_country_spinner.setHintTextColor(resources.getColor(R.color.light_gray)) //change title of spinner-dialog programmatically
        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedCountry)) {
            settings_country_spinner.setText(SessionState.instance.selectedCountry)
        }
        settings_country_spinner.setExpandTint(R.color.transparent)
        settings_country_spinner.setOnItemClickListener{ position ->
            if (countryList.get(position) != null) {
                SessionState.instance.selectedCountry = countryList.get(position).name!!
                SessionState.instance.selectedCountryCode = countryList.get(position).lowercaseCode!!
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY.toString(),
                        countryList.get(position).name!!)
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY_CODE.toString(),
                        countryList.get(position).lowercaseCode!!)
                SessionState.instance.selectedState = ""
                SessionState.instance.selectedCity = ""
                settings_state_spinner.setText("")
                settings_city_spinner.setText("")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                        "")
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                        "")
                fetchStateList(countryList.get(position).lowercaseCode!!)
            }
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedState)) {
            settings_state_spinner.setText(SessionState.instance.selectedState)
        }

        settings_state_spinner.setOnItemClickListener{ position ->
            SessionState.instance.selectedState = if (stateList.get(position) != null) stateList.get(position).name!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                    if (stateList.get(position) != null) stateList.get(position).name!! else "")
            settings_city_spinner.setText("")
            SessionState.instance.selectedCity = ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                    "")
            fetchCityList(stateList.get(position).code!!)
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedCity)) {
            settings_city_spinner.setText(SessionState.instance.selectedCity)
        }

        settings_city_spinner.setOnItemClickListener{ position ->
            SessionState.instance.selectedCity = if (cityList.get(position) != null) cityList.get(position).name!! else ""
            SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                    if (cityList.get(position) != null) cityList.get(position).name!! else "")
        }

        if (LanguagePack.instance.languagePackData == null) {
            fetchLanguagePackDetails()
        } else {
            initializeLanguagePack()
        }
    }

    private fun fetchLanguagePackDetails() {
        var mRequestQueue = Volley.newRequestQueue(context)

        //String Request initialized
        var mStringRequest = StringRequest(Request.Method.GET, AppConstants.BASE_URL + AppConstants.FETCH_LANGUAGE_PACK_URL, com.android.volley.Response.Listener<String> { response ->
            if (context != null) {
                LanguagePack.instance.saveLanguageData(context!!, response)
                LanguagePack.instance.setLanguageData(response)
                initializeLanguagePack()
            }
        }, com.android.volley.Response.ErrorListener {
            initializeLanguagePack()
        })

        mRequestQueue.add(mStringRequest)
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
        settings_language_spinner.setHint(LanguagePack.getString(getString(R.string.select_language)))
        settings_language_spinner.setOnItemClickListener{ position ->
            if(AppConfigDetail.languageList != null) {
                SessionState.instance.selectedLanguage = if (AppConfigDetail.languageList?.get(position)?.name != null) AppConfigDetail.languageList?.get(position)?.name!! else ""
                SessionState.instance.selectedLanguageCode = if (AppConfigDetail.languageList?.get(position)?.code != null) AppConfigDetail.languageList?.get(position)?.code!! else ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                        SessionState.instance.selectedLanguage)
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE_CODE.toString(),
                        SessionState.instance.selectedLanguageCode)
                initializeTextViewsWithLanguagePack(SessionState.instance.selectedLanguageCode)
            } else  {
                SessionState.instance.selectedLanguage = "English"
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_LANGUAGE.toString(),
                        "English")
            }
        }

        if (!AppConstants.EMPTY.equals(SessionState.instance.selectedLanguage)) {
            settings_language_spinner.setText(SessionState.instance.selectedLanguage)
        }

        var languageList = arrayListOf<String>()
        if (AppConfigDetail.languageList != null) {
            for (x in AppConfigDetail.languageList !!) {
                languageList.add(x.name!!)
            }
        } else  {
            languageList.add("English")
        }
        settings_language_spinner.setItems(languageList.toArray(arrayOfNulls<String>(languageList.size))) //this is important, you must set it to set the item list
        settings_language_spinner.setHintTextColor(resources.getColor(R.color.light_gray))
        settings_language_spinner.setExpandTint(R.color.transparent)
        settings_language_spinner.setExpandTint(R.color.transparent)

        if (settings_country_spinner.text.isNullOrEmpty()) {
            fetchCountryList() // To load only first time
        }
    }

    private fun fetchCountryList() {
        showProgressDialog(getString(R.string.fetch_country))
        RetrofitController.fetchCountryDetails(object: Callback<List<CountryListModel>> {
            override fun onResponse(call: Call<List<CountryListModel>>?, response: Response<List<CountryListModel>>?) {
                if (response != null && response.isSuccessful && settings_country_spinner != null) {
                    countryList.addAll(response.body())
                    val listOfCountry = arrayListOf<String>()
                    countryList.forEach { listOfCountry.add(it.name!!)}
                    settings_country_spinner.setItems(listOfCountry.toArray(arrayOfNulls<String>(countryList.size)))
                }
                dismissProgressDialog()
            }

            override fun onFailure(call: Call<List<CountryListModel>>?, t: Throwable?) {
                dismissProgressDialog()
                showNetworkErrorSnackBar()
            }
        })
    }

    private fun fetchStateList(countryId: String) {
        showProgressDialog(getString(R.string.fetch_state))
        RetrofitController.fetchStateDetails(countryId, object: Callback<List<StateListModel>> {
            override fun onResponse(call: Call<List<StateListModel>>?, response: Response<List<StateListModel>>?) {
                dismissProgressDialog()
                if (response != null && response.isSuccessful && settings_state_spinner != null) {
                    stateList.addAll(response.body())
                    val listOfState = arrayListOf<String>()
                    stateList.forEach { listOfState.add(it.name!!)}
                    settings_state_spinner.setItems(listOfState.toArray(arrayOfNulls<String>(stateList.size)))
                }
            }

            override fun onFailure(call: Call<List<StateListModel>>?, t: Throwable?) {
                dismissProgressDialog()
                showNetworkErrorSnackBar()
            }
        })
    }

    private fun fetchCityList(stateId: String) {
        showProgressDialog(getString(R.string.fetch_city))
        RetrofitController.fetchCityDetails(stateId, object: Callback<List<CityListModel>> {
            override fun onResponse(call: Call<List<CityListModel>>?, response: Response<List<CityListModel>>?) {
                dismissProgressDialog()
                if (response != null && response.isSuccessful && settings_city_spinner != null) {
                    cityList.addAll(response.body())
                    val listOfCity = arrayListOf<String>()
                    cityList.forEach { listOfCity.add(it.name!!)}
                    settings_city_spinner.setItems(listOfCity.toArray(arrayOfNulls<String>(cityList.size)))
                }
            }

            override fun onFailure(call: Call<List<CityListModel>>?, t: Throwable?) {
                dismissProgressDialog()
                showNetworkErrorSnackBar()
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

    fun showProgressDialog(message: String) {
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
        builder.setPositiveButton(LanguagePack.getString("YES")){dialog, which ->
            dialog.dismiss()
            logoutUser()
        }
        builder.setNegativeButton(LanguagePack.getString("No")){dialog, which ->
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
                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                // MultipartBody.Part is used to send also the actual file name
                val body = MultipartBody.Part.createFormData("fileToUpload", file.getName(), requestFile)
                RetrofitController.updateUserProfilePic(body, object: Callback<ProductUploadProductModel> {
                    override fun onFailure(call: Call<ProductUploadProductModel>?, t: Throwable?) {
                        dismissProgressDialog()
                        Utility.showSnackBar(fragment_settings_user_parent_layout, getString(R.string.internet_issue), context!!)
                    }

                    override fun onResponse(call: Call<ProductUploadProductModel>?, response: Response<ProductUploadProductModel>?) {
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
                })
            } else {
                dismissProgressDialog()
            }
        }
    }

    private fun refreshCategoriesWithLanguageCode(languageCode: String) {
        showProgressDialog(getString(R.string.loading))
        RetrofitController.fetchAppConfig(languageCode, object : Callback<AppConfigModel> {
            override fun onFailure(call: Call<AppConfigModel>?, t: Throwable?) {
                if (context != null && Utility.isNetworkAvailable(context!!)) {
                    refreshCategoriesWithLanguageCode(languageCode)
                } else {
                    dismissProgressDialog()
                }
            }

            override fun onResponse(call: Call<AppConfigModel>?, response: Response<AppConfigModel>?) {
                if (context != null && response != null && response.isSuccessful) {
                    val appConfigUrl: AppConfigModel = response.body()
                    AppConfigDetail.saveAppConfigData(context!!, Gson().toJson(appConfigUrl))
                    AppConfigDetail.initialize(Gson().toJson(appConfigUrl))
                    dismissProgressDialog()
                }
            }

        })
    }
}
