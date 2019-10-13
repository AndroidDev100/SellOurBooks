package com.bylancer.classified.bylancerclassified.uploadproduct.categoryselection

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.uploadproduct.OnUploadCategorySelection
import com.bylancer.classified.bylancerclassified.uploadproduct.subcategoryselection.UploadSubCategorySelectionActivity
import com.bylancer.classified.bylancerclassified.utils.*
import kotlinx.android.synthetic.main.activity_upload_category_selection.*

class UploadCategorySelectionActivity : BylancerBuilderActivity(), OnUploadCategorySelection, View.OnClickListener {
    val SPAN_COUNT = 2
    val SUB_CATEGORY_REQUEST = 101

    override fun setLayoutView() = R.layout.activity_upload_category_selection

    override fun initialize(savedInstanceState: Bundle?) {
        category_selection_title_text_view.text = LanguagePack.getString(getString(R.string.what_listing))
        category_selection_sub_title_text_view.text = LanguagePack.getString(getString(R.string.choose_category_in_upload))
        category_selection_recycler_view.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        category_selection_recycler_view.setHasFixedSize(false)
        category_selection_recycler_view.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, 10, false))

        if (AppConfigDetail.category != null) {
            category_selection_recycler_view.adapter = UploadCategoryAdapter(AppConfigDetail.category!!, this)
        } else {
            Utility.showSnackBar(upload_category_parent_layout, getString(R.string.some_wrong), this)
        }

        if (!isLocationEnabled()) showLocationAlertDialog()
    }

    override fun onCategorySelected(categoryId: String, position: Int) {
        var bundle = Bundle()
        bundle.putString(AppConstants.SELECTED_CATEGORY_ID, categoryId)
        bundle.putInt(AppConstants.SELECTED_CATEGORY_POSITION, position)
        startActivityForResult(UploadSubCategorySelectionActivity::class.java, false,  bundle, SUB_CATEGORY_REQUEST)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_category_selection_image_view -> onBackPressed()
        }
    }

    private fun showLocationAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setCancelable(false)
        builder.setMessage(LanguagePack.getString("Please enable location service to continue. We require it to get your current Ad location"))
        builder.setPositiveButton(LanguagePack.getString("OK")){dialog, which ->
            dialog.dismiss()
            var myIntent = Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent)
        }
        builder.create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SUB_CATEGORY_REQUEST && resultCode == Activity.RESULT_OK) {
            finish()
        }
    }
}
