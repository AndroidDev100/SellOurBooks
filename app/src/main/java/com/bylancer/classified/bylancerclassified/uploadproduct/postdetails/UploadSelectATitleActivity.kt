package com.bylancer.classified.bylancerclassified.uploadproduct.postdetails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.Utility
import kotlinx.android.synthetic.main.activity_upload_select_atitle.*

class UploadSelectATitleActivity : BylancerBuilderActivity(), View.OnClickListener {
    override fun setLayoutView() = R.layout.activity_upload_select_atitle
    var categoryPosition = 0
    var subCategoryId = ""
    val UPLOAD_PRODUCT_REQUEST = 103

    override fun initialize(savedInstanceState: Bundle?) {
        upload_enter_a_title_edit_text.hint = LanguagePack.getString(getString(R.string.enter_upload_title))
        upload_tile_selection_text_view.text = LanguagePack.getString(getString(R.string.enter_short_title))
        upload_title_selection_continue_button.text = LanguagePack.getString(getString(R.string.continue_title_selection))

        if(intent != null && intent.getBundleExtra(AppConstants.BUNDLE) != null) {
            var bundle = intent.getBundleExtra(AppConstants.BUNDLE)
            categoryPosition = bundle.getInt(AppConstants.SELECTED_CATEGORY_POSITION)
            subCategoryId = bundle.getString(AppConstants.SELECTED_SUB_CATEGORY_ID, "1")
        }
        addEnableListnerForButton()
    }

    private fun addEnableListnerForButton() {
        upload_enter_a_title_edit_text.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                upload_title_selection_continue_button.isEnabled = !upload_enter_a_title_edit_text.text.isNullOrEmpty()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_upload_title_selection_image_view -> onBackPressed()

            R.id.upload_title_selection_continue_button -> {
                upload_title_selection_continue_button.isEnabled = false
                Utility.hideKeyboard(this)
                if (!subCategoryId.isNullOrEmpty()) {
                    val bundle = Bundle()
                    bundle.putString(AppConstants.UPLOAD_PRODUCT_SELECTED_TITLE, upload_enter_a_title_edit_text.text.toString())
                    bundle.putInt(AppConstants.SELECTED_CATEGORY_POSITION, categoryPosition)
                    bundle.putString(AppConstants.SELECTED_SUB_CATEGORY_ID, subCategoryId)
                    startActivityForResult(UploadProductDetail::class.java, false, bundle, UPLOAD_PRODUCT_REQUEST)
                } else {
                    Utility.showSnackBar(upload_title_text_parent_layout, getString(R.string.some_wrong_retry), this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (upload_title_selection_continue_button != null) upload_title_selection_continue_button.isEnabled = true
        if (requestCode == UPLOAD_PRODUCT_REQUEST && resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}
