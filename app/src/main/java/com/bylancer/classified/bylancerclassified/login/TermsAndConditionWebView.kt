package com.bylancer.classified.bylancerclassified.login

import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import kotlinx.android.synthetic.main.activity_terms_condition.*

class TermsAndConditionWebView: BylancerBuilderActivity(), View.OnClickListener {

    override fun setLayoutView() = R.layout.activity_terms_condition

    override fun initialize(savedInstanceState: Bundle?) {
        terms_condition_title_text_view.text = ""
        terms_condition_web_view.webViewClient = MyBrowser()
        terms_condition_web_view.getSettings().setLoadsImagesAutomatically(true)
        terms_condition_web_view.getSettings().setJavaScriptEnabled(true)
        terms_condition_web_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        tc_web_view_progress_indicator.visibility = View.VISIBLE
        val bundle = intent.getBundleExtra(AppConstants.BUNDLE)
        if (bundle != null) {
            terms_condition_web_view.loadUrl(bundle!!.getString(AppConstants.TERMS_CONDITION_URL).toString())
            terms_condition_title_text_view.text = (bundle!!.getString(AppConstants.TERMS_CONDITION_TITLE))
        }
    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.terms_condition_back_image_view -> onBackPressed()
        }
    }

    private inner class MyBrowser : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            view?.loadUrl(request?.url.toString())
            return super.shouldOverrideUrlLoading(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            tc_web_view_progress_indicator.visibility = View.GONE
            super.onPageFinished(view, url)
        }
    }
}