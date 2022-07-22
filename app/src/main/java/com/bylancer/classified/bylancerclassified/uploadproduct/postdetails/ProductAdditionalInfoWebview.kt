package com.bylancer.classified.bylancerclassified.uploadproduct.postdetails

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.webkit.*
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import kotlinx.android.synthetic.main.activity_product_additional_info_webview.*

class ProductAdditionalInfoWebview : BylancerBuilderActivity(), View.OnClickListener {
    var title = ""

    override fun setLayoutView() = R.layout.activity_product_additional_info_webview

    override fun initialize(savedInstanceState: Bundle?) {
        products_additional_info_title_text_view.text = LanguagePack.getString(getString(R.string.add_more_info))
        showProgressDialog(getString(R.string.loading))
        initializeWebView()
    }

    private fun initializeWebView() {
        val webView = findViewById<WebView>(R.id.products_additional_info_webview)
        var catId = "1"
        var subCatId = "1"
        if (intent != null && intent.getBundleExtra(AppConstants.BUNDLE) != null) {
            val bundle = intent.getBundleExtra(AppConstants.BUNDLE)
            catId = bundle!!.getString(AppConstants.SELECTED_CATEGORY_ID, "1")
            subCatId = bundle!!.getString(AppConstants.SELECTED_SUB_CATEGORY_ID, "1")
            title = bundle!!.getString(AppConstants.ADDITIONAL_INFO_ACTIVITY_TITLE, "")
            if (!title.isNullOrEmpty()) {
                products_additional_info_title_text_view.text = LanguagePack.getString(title)
            }
        }

        val url = String.format(AppConstants.UPLOAD_PRODUCT_ADDITIONAL_INFO_URL, catId, subCatId, SessionState.instance.uploadedProductAdditionalInfo)
        webView.loadUrl(url)

        webView.addJavascriptInterface(this, "AndroidInterface") // To call methods in Android from using js in the html, AndroidInterface.showToast, AndroidInterface.getAndroidVersion etc
        val webSettings = webView.settings
        webSettings.setJavaScriptEnabled(true)
        webView.webViewClient = MyWebViewClient()
        webView.webChromeClient = MyWebChromeClient()
    }

    private inner class MyWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            dismissProgressDialog()
            //Calling a javascript function in html page
           // view.loadUrl("javascript:alert(showVersion('called by Android'))")
        }
    }

    private inner class MyWebChromeClient : WebChromeClient() {
        override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
            result.confirm()
            return true
        }
    }

    @JavascriptInterface
    fun additionalInfo(additionalData: String) {
        SessionState.instance.uploadedProductAdditionalInfo = additionalData
        this@ProductAdditionalInfoWebview.runOnUiThread {
            if (!title.isNullOrEmpty() && getString(R.string.add_filter).equals(title)) {
                setResult(Activity.RESULT_OK)
            }
            onBackPressed()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_products_additional_info_image_view -> onBackPressed()
        }
    }

}
