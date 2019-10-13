package com.bylancer.classified.bylancerclassified.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import co.paystack.android.Paystack.TransactionCallback
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.exceptions.ExpiredAccessCodeException
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.android.volley.NoConnectionError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.chat.ChatActivity
import com.bylancer.classified.bylancerclassified.login.LoginActivity
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.login.ManualLoginActivity
import com.bylancer.classified.bylancerclassified.login.RegisterUserActivity
import com.bylancer.classified.bylancerclassified.splash.SplashActivity
import com.bylancer.classified.bylancerclassified.submitcreditcardflow.PayStackCard
import com.bylancer.classified.bylancerclassified.submitcreditcardflow.SubmitCreditCardActivity
import com.bylancer.classified.bylancerclassified.uploadproduct.categoryselection.UploadCategorySelectionActivity
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.transaction.TransactionResponseModel
import com.bylancer.classified.bylancerclassified.widgets.CustomAlertDialog
import com.bylancer.classified.bylancerclassified.widgets.ProgressUtils
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.payumoney.core.PayUmoneyConfig
import com.payumoney.core.PayUmoneySdkInitializer
import com.payumoney.core.entity.TransactionResponse
import com.payumoney.sdkui.ui.utils.PayUmoneyFlowManager
import com.payumoney.sdkui.ui.utils.ResultModel
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.util.*

/**
 * Created by Ani on 3/20/18.
 */
abstract class BylancerBuilderActivity : AppCompatActivity() {
    lateinit var mInterstitialAd: InterstitialAd
    private var mPremiumUpgradeType: Int = AppConstants.GO_FOR_PREMIUM_APP
    private val CARD_DETAILS_REQUEST = 1010
    private var mPayStackTransaction : Transaction? = null
    private var mTransactionAmount = "0"
    private var mProductIdForPremium : String? = null
    private var mProductTitleForPremium : String? = null
    private var mPremiumFeatures: Array<String>? = null
    var isPaymentActive = false
    private var mAdTimer : Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        isRTLSupportRequired()
        super.onCreate(savedInstanceState)
        if(this::class.simpleName != LoginRequiredActivity::class.simpleName &&
                this::class.simpleName != LoginActivity::class.simpleName &&
                this::class.simpleName != ChatActivity::class.simpleName &&
                this::class.simpleName != UploadCategorySelectionActivity::class.simpleName) {
            Utility.slideActivityRightToLeft(this)
        } else {
            Utility.slideActivityBottomToTop(this)
        }
        setContentView(setLayoutView())

        setOrientation() // Fixing Android O issue

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = getString(R.string.ad_mob_interstitial_ad_unit)
        loadInterstitialAd()
        addInterstitialAdListener()

        if (SessionState.instance.isGoogleInterstitialSupported
                && this::class.simpleName != LoginRequiredActivity::class.simpleName
                && this::class.simpleName != LoginActivity::class.simpleName
                && this::class.simpleName != RegisterUserActivity::class.simpleName
                && this::class.simpleName != SplashActivity::class.simpleName
                && this::class.simpleName != ManualLoginActivity::class.simpleName) {
            scheduleInterstitialAd()
        }
        initialize(savedInstanceState)
    }

    protected abstract fun setLayoutView(): Int

    protected abstract fun initialize(savedInstanceState: Bundle?)

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean) {
        val intent = Intent(this, clazz)
        if(isNewTask) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun startActivityWithAffinity(clazz: Class<out Activity>, isNewTask:Boolean) {
        val intent = Intent(this, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    fun setOrientation() {
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean, bundle: Bundle) {
        val intent = Intent(this, clazz)
        intent.putExtra(AppConstants.BUNDLE, bundle)
        if(isNewTask) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    fun startActivityForResult(clazz: Class<out Activity>, isNewTask:Boolean, bundle: Bundle?, activityStartCode: Int) {
        val intent = Intent(this, clazz)
        if (bundle != null) {
            intent.putExtra(AppConstants.BUNDLE, bundle)
        }
        if(isNewTask) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivityForResult(intent, activityStartCode)
    }

    fun startActivity(clazz: Class<out Activity>, extra:Bundle, parcelName:String) {
        val intent = Intent(this, clazz)
        intent.putExtra(AppConstants.BUNDLE, extra)
        startActivity(intent)
    }

    override fun onBackPressed() {
        try {
            super.onBackPressed()
            if(this::class.simpleName != LoginRequiredActivity::class.simpleName &&
                    this::class.simpleName != LoginActivity::class.simpleName &&
                    this::class.simpleName != ChatActivity::class.simpleName &&
                    this::class.simpleName != UploadCategorySelectionActivity::class.simpleName) {
                Utility.slideActivityLeftToRight(this)
            } else {
                Utility.slideActivityTopToBottom(this)
            }
        } catch (nullPointerException: NullPointerException) {

        } finally {
            finish()
        }
    }

    private fun loadInterstitialAd() {
        if (!isPaymentActive)
            mInterstitialAd?.loadAd(AdRequest.Builder().build())
    }

    private fun addInterstitialAdListener() {
        mInterstitialAd?.adListener  = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                loadInterstitialAd()
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                loadInterstitialAd()
            }
        }
    }

    private fun scheduleInterstitialAd() {
        val interstitialTask = object : TimerTask() {
            override fun run() {
                this@BylancerBuilderActivity.runOnUiThread() {
                    if (mInterstitialAd?.isLoaded && !this@BylancerBuilderActivity.isFinishing && !isPaymentActive) {
                        mInterstitialAd?.show()
                    }
                }
            }
        }
        val delay = (1000 * 60 * AppConstants.INTERSTITIAL_DELAY)
        mAdTimer?.schedule(interstitialTask, 0L, delay.toLong())
    }

    private fun isRTLSupportRequired() {
        if (AppConstants.DIRECTION_RTL.equals(SessionState.instance.selectedLanguageDirection)) {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        } else {
            window.decorView.layoutDirection = View.LAYOUT_DIRECTION_LTR
        }
    }

    /**
     * Pay U Money Payment
     */
    private fun launchPayUPaymentFlow(title: String, amount: String, upgradeType: Int) {
        this.mPremiumUpgradeType = upgradeType
        val payUmoneyConfig = PayUmoneyConfig.getInstance()
        payUmoneyConfig.payUmoneyActivityTitle = if (SessionState.instance.appName.isNullOrEmpty()) getString(R.string.app_name) else SessionState.instance.appName
        payUmoneyConfig.doneButtonText = "Pay " + SessionState.instance.paymentCurrencySign + amount
        if (SessionState.instance.phoneNumber.isNullOrEmpty()) {
            SessionState.instance.phoneNumber = "9999999999"
        }

        val builder = PayUmoneySdkInitializer.PaymentParam.Builder()
        builder.setAmount(amount)
                .setTxnId(System.currentTimeMillis().toString() + "")
                .setPhone(SessionState.instance.phoneNumber)
                .setProductName(title)
                .setFirstName(SessionState.instance.userName)
                .setEmail(SessionState.instance.email)
                .setsUrl(AppConstants.SURL)
                .setfUrl(AppConstants.FURL)
                .setUdf1("Aa")
                .setUdf2("bb")
                .setUdf3("cc")
                .setUdf4("dd")
                .setUdf5("ee")
                .setIsDebug(AppConstants.DEBUG)
                .setKey(AppConstants.MERCHANT_KEY)
                .setMerchantId(AppConstants.MERCHANT_ID)

        try {
            var mPaymentParams = builder.build()
            calculateHashInServer(mPaymentParams)
        } catch (e: Exception) {
            showToast(e.message)
        }
    }

    private fun calculateHashInServer(mPaymentParams: PayUmoneySdkInitializer.PaymentParam) {
        ProgressUtils.showLoadingDialog(this)
        val url = AppConstants.BASE_URL + AppConstants.PAY_U_HASH_URL
        val request = object : StringRequest(Request.Method.POST, url,

                Response.Listener { response ->
                    var merchantHash = ""

                    try {
                        val jsonObject = JSONObject(response)
                        merchantHash = jsonObject.getString("payment_hash")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                    ProgressUtils.cancelLoading()

                    if (merchantHash.isEmpty() || merchantHash == "") {
                        Toast.makeText(this, "Could not generate hash", Toast.LENGTH_SHORT).show()
                    } else {
                        mPaymentParams.setMerchantHash(merchantHash)
                        if (PayUAppPreferences.selectedTheme != -1) {
                            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, this, PayUAppPreferences.selectedTheme, PayUAppPreferences.isOverrideResultScreen)
                        } else {
                            PayUmoneyFlowManager.startPayUMoneyFlow(mPaymentParams, this, R.style.PayUMoney, PayUAppPreferences.isOverrideResultScreen)
                        }
                    }
                },

                Response.ErrorListener { error ->
                    if (error is NoConnectionError) {
                        Toast.makeText(this, "Connect to internet Volley", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
                    }
                    ProgressUtils.cancelLoading()
                }) {
            override fun getParams(): Map<String, String> {
                return mPaymentParams.params
            }
        }
        request.setShouldCache(false)
        Volley.newRequestQueue(this).add(request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == Activity.RESULT_OK && data != null) {

            val transactionResponse = data.getParcelableExtra<TransactionResponse>(PayUmoneyFlowManager.INTENT_EXTRA_TRANSACTION_RESPONSE)
            val resultModel = data.getParcelableExtra<ResultModel>(PayUmoneyFlowManager.ARG_RESULT)

            if (transactionResponse?.getPayuResponse() != null) {
                when {
                    transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.SUCCESSFUL -> { uploadTransactionDetails(mProductTitleForPremium!!, mTransactionAmount, SessionState.instance.userId,
                            mProductIdForPremium!!, mPremiumFeatures!![0], mPremiumFeatures!![1], mPremiumFeatures!![2], AppConstants.PAY_U_MONEY,
                            AppConstants.PAYMENT_TYPE_PREMIUM, AppConstants.PAYMENT_TRANSACTION_DETAILS) }
                    transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.CANCELLED -> showAlert(false)
                    transactionResponse.transactionStatus == TransactionResponse.TransactionStatus.FAILED -> showAlert(false)
                }

            } else if (resultModel != null && resultModel.error != null) {
                showAlert(false)
            } else {
                showAlert(false)
            }
        } else if (requestCode == PayUmoneyFlowManager.REQUEST_CODE_PAYMENT && resultCode == Activity.RESULT_CANCELED) {
            showAlert(false)
        } else if (requestCode == CARD_DETAILS_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val cardDetail = data.getParcelableExtra<PayStackCard>(AppConstants.PAY_STACK_CARD_DETAILS)
            if (cardDetail != null) {
                startPayStackCharge(cardDetail)
            } else {
                showAlert(false)
            }
        } else if (requestCode == CARD_DETAILS_REQUEST && resultCode == Activity.RESULT_CANCELED) {
            showAlert(false)
        }
    }
    /********************PayUMoney payment ends here ******************/

    private fun showAlert(isSuccess : Boolean) {
        val successOrFailureDialog = CustomAlertDialog(this, R.style.payment_chooser_dialog)
        successOrFailureDialog.setContentView(R.layout.success_dialog)
        successOrFailureDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        successOrFailureDialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        successOrFailureDialog.setCanceledOnTouchOutside(true)
        successOrFailureDialog.show()

        if (!isSuccess) {
            val topBackground = successOrFailureDialog.findViewById<View>(R.id.success_dialog_top_background)
            topBackground?.setBackgroundColor(resources?.getColor(R.color.denied_red)!!)

            val iconImageView = successOrFailureDialog.findViewById<ImageView>(R.id.success_dialog_icon)
            iconImageView.setImageResource(R.drawable.close)
        }

        val dialogTitle = successOrFailureDialog.findViewById(R.id.success_dialog_title) as TextView
        dialogTitle.text = if (isSuccess) LanguagePack.getString("Payment Successful") else LanguagePack.getString("Payment Failure")
        val dialogMessage = successOrFailureDialog.findViewById(R.id.success_dialog_message) as TextView
        dialogMessage.text = if (isSuccess) LanguagePack.getString("Congratulations your payment is successful") else LanguagePack.getString("Unfortunately your payment is failed, please retry")

        val okButton = successOrFailureDialog.findViewById(R.id.success_dialog_ok_button) as Button
        okButton.text = LanguagePack.getString(getString(R.string.btn_ok))
        okButton.setOnClickListener {
            successOrFailureDialog.dismiss()
        }

        successOrFailureDialog.setOnDismissListener {  isPaymentActive = false }
    }

    fun showPaymentGatewayOptions(title: String, amount: String, upgradeType: Int, productId : String?, premiumFeatures: Array<String>) {
        val paymentGatewayChooserDialog = CustomAlertDialog(this, R.style.payment_chooser_dialog)
        paymentGatewayChooserDialog.setContentView(R.layout.payment_gateway_chooser_dialog)
        paymentGatewayChooserDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        paymentGatewayChooserDialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        paymentGatewayChooserDialog.setCanceledOnTouchOutside(true)
        paymentGatewayChooserDialog.show()

        mTransactionAmount = amount
        mProductIdForPremium = productId
        mProductTitleForPremium = title
        mPremiumFeatures = premiumFeatures
        val dialogTitle = paymentGatewayChooserDialog.findViewById(R.id.payment_gateway_chooser_title) as AppCompatTextView
        dialogTitle.text = LanguagePack.getString("Pay Using")

        val payUMoney = paymentGatewayChooserDialog.findViewById(R.id.pay_u_money_gateway) as AppCompatImageView
        if (!AppConstants.PAY_U_MONEY_ACTIVE) {
            payUMoney.visibility = View.GONE
        }
        val payStack = paymentGatewayChooserDialog.findViewById(R.id.pay_stack_gateway) as AppCompatImageView
        if (!AppConstants.PAY_STACK_ACTIVE) {
            payStack.visibility = View.GONE
        }
        payUMoney.setOnClickListener() {
            isPaymentActive = true
            paymentGatewayChooserDialog.dismiss()
            launchPayUPaymentFlow(title, amount, upgradeType)
        }
        payStack.setOnClickListener() {
            isPaymentActive = true
            paymentGatewayChooserDialog.dismiss()
            startActivityForResult(SubmitCreditCardActivity :: class.java, false, null, CARD_DETAILS_REQUEST)
        }
    }

    /**
     * PayStack Payment methods starts here
     */
    private fun startPayStackCharge(payStackcard : PayStackCard) {
        ProgressUtils.showLoadingDialog(this)
        val charge = Charge()
        val cardExpiry = payStackcard.expiredDate?.split("/")
        val cardBuilder = Card.Builder(payStackcard.cardNumber, cardExpiry?.get(0)?.toInt(), cardExpiry?.get(1)?.toInt(), payStackcard.cvvCode)
        cardBuilder.setName(payStackcard.cardHolder)
        charge.amount = mTransactionAmount.toInt()
        charge.currency = SessionState.instance.paymentCurrencyCode
        charge.email = SessionState.instance.email
        charge.reference = "ChargedFromAndroid_${SessionState.instance.appName}_" + Calendar.getInstance().timeInMillis
        try {
            charge.putCustomField("Charged From", "Android SDK");
        } catch (e : JSONException) {
            showAlert(false)
            ProgressUtils.cancelLoading()
        }
        chargePayStackCard(charge)
    }

    private fun chargePayStackCard(charge : Charge) {
        mPayStackTransaction = null
        PaystackSdk.chargeCard(this, charge, object : TransactionCallback {
            // This is called only after transaction is successful
            override fun onSuccess(transaction : Transaction) {
                ProgressUtils.cancelLoading()
                mPayStackTransaction = transaction
                if (!mProductTitleForPremium.isNullOrEmpty() && !mProductIdForPremium.isNullOrEmpty() &&
                        !mPremiumFeatures.isNullOrEmpty()) {
                    uploadTransactionDetails(mProductTitleForPremium!!, mTransactionAmount, SessionState.instance.userId,
                            mProductIdForPremium!!, mPremiumFeatures!![0], mPremiumFeatures!![1], mPremiumFeatures!![2], AppConstants.PAY_STACK,
                            AppConstants.PAYMENT_TYPE_PREMIUM, AppConstants.PAYMENT_TRANSACTION_DETAILS)
                }
                //new verifyOnServer().execute(transaction.getReference());
            }

            // This is called only before requesting OTP
            // Save reference so you may send to server if
            // error occurs with OTP
            // No need to dismiss dialog
            override fun beforeValidate(transaction : Transaction) {
                mPayStackTransaction = transaction
                //Toast.makeText(MainActivity.this, transaction.getReference(), Toast.LENGTH_LONG).show();
            }

            override fun onError(error : Throwable, transaction : Transaction) {
                ProgressUtils.cancelLoading()
                // If an access code has expired, simply ask your server for a new one
                // and restart the charge instead of displaying error
                mPayStackTransaction = transaction;
                if (error is ExpiredAccessCodeException) {
                   // startAFreshCharge(charge.amount.toString())
                    //MainActivity.this.chargeCard();
                    showAlert(false)
                    return;
                }

                if (transaction.reference != null) {
                    showAlert(false)
                    //showToast(transaction.reference + " concluded with error: " + error.message)
                    // new verifyOnServer().execute(transaction.getReference());
                } else {
                    showAlert(false)
                    //showToast(transaction.reference + " concluded with error: " + error.message)
                }
            }
        })
    }

    private fun uploadTransactionDetails(productName: String, amount: String, userId: String, productId: String,
                                         isFeatured: String, isUrgent: String, isHighlighted: String, folder: String,
                                         paymentType: String, transactionDetails: String) {
        RetrofitController.postPremiumAdTransactionDetails(productName, amount, userId, productId, isFeatured, isUrgent,
                isHighlighted, folder, paymentType, transactionDetails, object : Callback<TransactionResponseModel> {
            override fun onFailure(call: Call<TransactionResponseModel>?, t: Throwable?) {
                uploadTransactionDetails(productName, amount, userId, productId, isFeatured, isUrgent,
                        isHighlighted, folder, paymentType, transactionDetails)
            }

            override fun onResponse(call: Call<TransactionResponseModel>?, response: retrofit2.Response<TransactionResponseModel>?) {
                if (response != null && response.isSuccessful) {
                    if (AppConstants.SUCCESS.equals(response.body()?.success)) {
                        if (mProductIdForPremium != null) {
                            onProductBecamePremium(mProductIdForPremium!!)
                        }
                        showAlert(true)
                    } else {
                        uploadTransactionDetails(productName, amount, userId, productId, isFeatured, isUrgent,
                                isHighlighted, folder, paymentType, transactionDetails)
                    }
                } else {
                    uploadTransactionDetails(productName, amount, userId, productId, isFeatured, isUrgent,
                            isHighlighted, folder, paymentType, transactionDetails)
                }
            }
        })
    }

    open fun onProductBecamePremium(productId: String) {}

    override fun onResume() {
        super.onResume()
        mAdTimer = Timer()
    }

    override fun onPause() {
        super.onPause()
        mAdTimer?.purge()
        mAdTimer?.cancel()
        mAdTimer = null
    }
}

