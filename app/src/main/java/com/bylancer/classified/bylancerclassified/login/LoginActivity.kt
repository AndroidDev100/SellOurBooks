package com.bylancer.classified.bylancerclassified.login

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.dashboard.DashboardActivity
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.registration.UserRegistrationData
import com.bylancer.classified.bylancerclassified.webservices.registration.UserRegistrationStatus
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LoginActivity : BylancerBuilderActivity(), View.OnClickListener, Callback<UserRegistrationStatus> {
    private var callbackManager: CallbackManager? = null

    override fun setLayoutView() = R.layout.activity_login

    override fun initialize(savedInstanceState: Bundle?) {
        if (intent != null && intent.hasExtra(AppConstants.BUNDLE)) {
            intent.getBundleExtra(AppConstants.BUNDLE).getString(AppConstants.MESSAGE)?.let { Utility.showSnackBar(login_screen_parent_layout, it, this) }
        }

        app_name_login_text_view.text = if (SessionState.instance.appName != null) SessionState.instance.appName else getString(R.string.app_name)
        sign_up_to_continue_text_view.text = LanguagePack.getString(getString(R.string.sign_up_to_continue))
        login_with_email_text_view.text = LanguagePack.getString(getString(R.string.login_with_email))
        sign_up_with_email_text_view.text = LanguagePack.getString(getString(R.string.sign_up_with_email))
        setTermsAndCondition()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.login_close_button_view -> onBackPressed()
            R.id.login_with_email_text_view -> startActivity(ManualLoginActivity::class.java, false)
            R.id.sign_up_with_email_text_view -> startActivity(RegisterUserActivity::class.java, false)
            R.id.login_with_facebook -> loginWithFacebook()
        }
    }

    private fun loginWithFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"))
        LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        val request = GraphRequest.newMeRequest(
                                loginResult.accessToken
                        ) { jsonObject, response ->
                            if (jsonObject != null && response != null) {
                                SessionState.instance.profilePicUrl = "https://graph.facebook.com/" + jsonObject.get("id") + "/picture?type=large"
                                SessionState.instance.saveValuesToPreferences(applicationContext, AppConstants.Companion.PREFERENCES.PROFILE_PIC.toString(), SessionState.instance.profilePicUrl)
                                val email = if (jsonObject.has("email")) jsonObject.getString("email") else ""
                                val userData = UserRegistrationData()
                                 userData.email = email
                                 userData.username = if (email.length > 0) email.split("@")[0] else ""
                                 userData.name = if (jsonObject.has("name")) jsonObject.getString("name") else ""
                                 userData.password = UUID.randomUUID().toString()
                                 userData.fbLogin = "1"
                                if (userData.email!!.length > 0) {
                                    logoutFromFacebook()
                                    registerUser(userData)
                                }
                            }
                        }
                        val parameters = Bundle()
                        parameters.putString("fields", "id,name,email")
                        request.parameters = parameters
                        request.executeAsync()
                    }

                    override fun onCancel() {
                        Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString("You have cancelled facebook login, please login to continue"), this@LoginActivity)
                    }

                    override fun onError(error: FacebookException) {
                        Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString(getString(R.string.some_wrong)), this@LoginActivity)
                    }
                })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager?.onActivityResult(requestCode, resultCode, data)
    }

    private fun logoutFromFacebook() {
        Utility.hideKeyboard(this)
        register_user_sliding_progress_indicator.visibility = View.VISIBLE
        if (AccessToken.getCurrentAccessToken() != null) {
            GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, GraphRequest.Callback {
                AccessToken.setCurrentAccessToken(null)
                LoginManager.getInstance().logOut()
            }).executeAsync()
        }
    }

    private fun registerUser(userData: UserRegistrationData) {
        RetrofitController.registerUser(userData, this)
    }

    override fun onFailure(call: Call<UserRegistrationStatus>?, t: Throwable?) {
        Utility.removeProgressBar(register_user_sliding_progress_indicator)
        Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this)
    }

    override fun onResponse(call: Call<UserRegistrationStatus>?, response: Response<UserRegistrationStatus>?) {
        Utility.removeProgressBar(register_user_sliding_progress_indicator)

        if (response != null && response.isSuccessful) {
            val responseBody: UserRegistrationStatus = response.body()
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
                Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString("Username or email is already occupied"), this)
            }
        } else {
            Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString(getString(R.string.some_wrong)), this)
        }
    }

    private fun fetchLanguagePackDetails() {
        var mRequestQueue = Volley.newRequestQueue(this)

        //String Request initialized
        var mStringRequest = JsonArrayRequest(Request.Method.GET, AppConstants.BASE_URL + AppConstants.FETCH_LANGUAGE_PACK_URL, null, com.android.volley.Response.Listener<JSONArray> { response ->
            if (response != null) {
                LanguagePack.instance.saveLanguageData(this@LoginActivity, response.toString())
                LanguagePack.instance.setLanguageData(response.toString())
                SessionState.instance.isLogin = true
                SessionState.instance.saveBooleanToPreferences(this, AppConstants.Companion.PREFERENCES.LOGIN_STATUS.toString(), true)
                moveToDashboard()
            } else {
                Utility.showSnackBar(login_screen_parent_layout, getString(R.string.internet_issue), this)
            }
        }, com.android.volley.Response.ErrorListener {
            if (Utility.isNetworkAvailable(this@LoginActivity)) {
                fetchLanguagePackDetails()
            } else {
                Utility.showSnackBar(login_screen_parent_layout, getString(R.string.internet_issue), this)
            }
        })

        mRequestQueue.add(mStringRequest)
    }

    private fun moveToDashboard() {
        sendTokenToServer()
        Utility.removeProgressBar(register_user_sliding_progress_indicator)
        Utility.showSnackBar(login_screen_parent_layout, LanguagePack.getString("You have successfully logged in"), this)
        startActivity(DashboardActivity::class.java, true)
        finish()
    }
    private fun setTermsAndCondition() {
        val span = Spannable.Factory.getInstance().newSpannable(LanguagePack.getString(getString(R.string.privacy_policy)))
        var minLength = 31
        var maxLength = 49
        if (!span.isNullOrEmpty() && span.length >= 54) {
            span.setSpan(object : ClickableSpan() {
                override fun onClick(v: View) {
                    loadTermsAndConditionWebView(R.string.terms_condition, if (SessionState.instance.termsConditionUrl != null) SessionState.instance.termsConditionUrl else "")
                }
            }, minLength, maxLength, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            val cs = object : ClickableSpan() {
                override fun onClick(v: View) {
                    loadTermsAndConditionWebView(R.string.privacy_text, if (SessionState.instance.privacyPolicyUrl != null) SessionState.instance.privacyPolicyUrl else "")
                }
            }

            span.setSpan(cs, 54, span.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(ForegroundColorSpan(resources.getColor(R.color.denied_red)), minLength, maxLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            span.setSpan(ForegroundColorSpan(resources.getColor(R.color.denied_red)), 54, span.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            login_terms_condition.setText(span)
            login_terms_condition.setMovementMethod(LinkMovementMethod.getInstance())
        }
    }

    private fun loadTermsAndConditionWebView(titleId: Int, url: String) {
        var bundle = Bundle()
        bundle.putString(AppConstants.TERMS_CONDITION_TITLE, LanguagePack.getString(getString(titleId)))
        bundle.putString(AppConstants.TERMS_CONDITION_URL, url)
        startActivity(TermsAndConditionWebView ::class.java, false, bundle)
    }
}
