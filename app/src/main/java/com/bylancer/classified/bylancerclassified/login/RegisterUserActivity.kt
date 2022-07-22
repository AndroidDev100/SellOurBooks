package com.bylancer.classified.bylancerclassified.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.registration.UserRegistrationData
import com.bylancer.classified.bylancerclassified.webservices.registration.UserRegistrationStatus
import kotlinx.android.synthetic.main.activity_register_user.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterUserActivity : BylancerBuilderActivity(), View.OnFocusChangeListener , TextWatcher, View.OnClickListener, Callback<UserRegistrationStatus> {
    var isPasswordShown = false

    override fun setLayoutView() = R.layout.activity_register_user

    override fun initialize(savedInstanceState: Bundle?) {
        register_user_title_text_view.text = String.format(LanguagePack.getString(getString(R.string.sign_up_app)), SessionState.instance.appName)
        register_name_edit_text.hint = LanguagePack.getString(getString(R.string.name))
        register_username_edit_text.hint = LanguagePack.getString(getString(R.string.username))
        register_email_id_edit_text.hint = LanguagePack.getString(getString(R.string.email_id))
        register_user_password_edit_text.hint = LanguagePack.getString(getString(R.string.password))
        register_user_show_hide_password_text_view.text = LanguagePack.getString(getString(R.string.show))
        register_user_button.text = LanguagePack.getString(getString(R.string.sign_up))
        register_name_edit_text.addTextChangedListener(this)
        register_email_id_edit_text.addTextChangedListener(this)
        register_username_edit_text.addTextChangedListener(this)
        register_user_password_edit_text.addTextChangedListener(this)
        register_user_password_edit_text.onFocusChangeListener = this
        //register_ccp.registerPhoneNumberTextView(register_phone_edit_text)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun afterTextChanged(p0: Editable?) {
        register_user_button.isEnabled = Utility.hasText(register_name_edit_text) && Utility.hasText(register_email_id_edit_text)
                && Utility.hasText(register_user_password_edit_text) && Utility.hasText(register_username_edit_text)

    }

    override fun onFocusChange(view: View?, hasFocus: Boolean) {
        if(hasFocus) {
            register_user_password_frame.setBackgroundResource(R.drawable.rounded_edittext_focussed)
        } else {
            register_user_password_frame.setBackgroundResource(R.drawable.rounded_edittext_normal)
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.register_user_show_hide_password_text_view -> {
                if (!isPasswordShown) {
                    register_user_show_hide_password_text_view.text = LanguagePack.getString(getString(R.string.hide))
                    register_user_password_edit_text.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                } else {
                    register_user_show_hide_password_text_view.text = LanguagePack.getString(getString(R.string.show))
                    register_user_password_edit_text.setTransformationMethod(PasswordTransformationMethod.getInstance())
                }
                register_user_password_edit_text.setSelection(register_user_password_edit_text.text.length)
                isPasswordShown = !isPasswordShown
            }

            R.id.register_user_screen_back -> onBackPressed()

            R.id.register_user_button -> {
                if (register_name_edit_text.text.toString().length < 2) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please enter your full name"), this)
                } else if (register_username_edit_text.text.toString().length < 6) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please enter minimum 6 characters of Username"), this)
                } else if (!Utility.isValidAlphaNumeric(register_username_edit_text.text.toString())) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Username can contain only Alphanumeric characters"), this)
                } else if (!Utility.isValidEmail(register_email_id_edit_text.text.toString())) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please enter correct Email id"), this)
                } else if (register_user_password_edit_text.text.toString().length < 6) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please enter minimum 6 characters of Password"), this)
                }/* else if (register_phone_edit_text.text.toString().length < 6) {
                    Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please enter correct phone number"), this)
                } */else {
                    registerUser()
                    /*PhoneNumberAuthHelper.startPhoneNumberVerification("+919962909777", object : OnOTPResponse {
                        override fun onOTPResponse(code: Int) {

                        }

                    }, this@RegisterUserActivity)*/
                }
            }
        }
    }

    private fun registerUser() {
        Utility.hideKeyboard(this)
        register_user_sliding_progress_indicator.visibility = View.VISIBLE

        val userData = UserRegistrationData()
        userData.email = register_email_id_edit_text.text.toString()
        userData.username = register_username_edit_text.text.toString()
        userData.name = register_name_edit_text.text.toString()
        userData.password = register_user_password_edit_text.text.toString()
        userData.fbLogin = ""

        RetrofitController.registerUser(userData, this)
    }

    override fun onFailure(call: Call<UserRegistrationStatus>?, t: Throwable?) {
        register_user_sliding_progress_indicator.visibility = View.GONE
        Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Please check your internet connection"), this)
    }

    override fun onResponse(call: Call<UserRegistrationStatus>?, response: Response<UserRegistrationStatus>?) {
        if (response != null && response.isSuccessful && response.body() != null) {
            val responseBody: UserRegistrationStatus = response.body()!!
            if (responseBody != null && AppConstants.SUCCESS.equals(responseBody.status!!)) {
                 SessionState.instance.displayName = responseBody.name!!
                SessionState.instance.userName = responseBody.username!!
                SessionState.instance.email = responseBody.email!!
                SessionState.instance.userId = responseBody.userId!!
                SessionState.instance.profilePicUrl = if (responseBody.profilePicture != null) responseBody.profilePicture!! else ""

                SessionState.instance.saveValuesToPreferences(this, AppConstants.Companion.PREFERENCES.PROFILE_PIC.toString(), SessionState.instance.profilePicUrl)
                SessionState.instance.saveValuesToPreferences(this, AppConstants.Companion.PREFERENCES.USERNAME.toString(), responseBody.username!!)
                SessionState.instance.saveValuesToPreferences(this, AppConstants.Companion.PREFERENCES.EMAIL.toString(), responseBody.email!!)
                SessionState.instance.saveValuesToPreferences(this, AppConstants.Companion.PREFERENCES.DISPLAY_NAME.toString(), responseBody.name!!)
                SessionState.instance.saveValuesToPreferences(this, AppConstants.Companion.PREFERENCES.USER_ID.toString(), responseBody.userId!!)

                fetchLanguagePackDetails()
            } else {
                register_user_sliding_progress_indicator.visibility = View.GONE
                Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Username or email is already occupied"), this)
            }
        } else {
            register_user_sliding_progress_indicator.visibility = View.GONE
            Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("Something went wrong, we are working on it to fix it as soon as possible"), this)
        }
    }

    private fun fetchLanguagePackDetails() {
        val response = getJsonDataFromAsset(this)
        if (response != null) {
            LanguagePack.instance.saveLanguageData(this@RegisterUserActivity, response.toString())
            LanguagePack.instance.setLanguageData(response.toString())
        }
        SessionState.instance.isLogin = true
        SessionState.instance.saveBooleanToPreferences(this, AppConstants.Companion.PREFERENCES.LOGIN_STATUS.toString(), true)
        moveToDashboard()
    }

    private fun moveToDashboard() {
        sendTokenToServer()
        Utility.removeProgressBar(register_user_sliding_progress_indicator)
        Utility.showSnackBar(activity_register_user_parent_layout, LanguagePack.getString("You have successfully logged in"), this)
        startActivity(DashboardActivity::class.java, true)
        finish()
    }
}
