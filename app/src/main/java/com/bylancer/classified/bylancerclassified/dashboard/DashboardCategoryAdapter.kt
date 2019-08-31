package com.bylancer.classified.bylancerclassified.dashboard

import android.graphics.Typeface
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.appconfig.Category
import com.bylancer.classified.bylancerclassified.appconfig.SubCategory
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat
import ir.mirrajabi.searchdialog.core.SearchResultListener
import kotlinx.android.synthetic.main.category_adapter.view.*

class DashboardCategoryAdapter(val items : List<Category>, val parentActivity: DashboardActivity) : RecyclerView.Adapter<ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parentActivity).inflate(R.layout.category_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category: Category = items.get(position)
        holder?.categoryName?.text = category.name
        Glide.with(holder?.categoryIcon.context).load(category.picture).into(holder?.categoryIcon)
        holder?.categoryParentLayout.setOnClickListener() {
            showSubCategoryInSearchBar(parentActivity, category.subCategory!!, category.id!!, category.name!!, category.picture!!)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private fun showSubCategoryInSearchBar(context: BylancerBuilderActivity, subCategory: List<SubCategory>, categoryId:String, categoryName: String, iconUrl: String) {
        var searchItemList = arrayListOf<DashboardSearchItemModel>(DashboardSearchItemModel(LanguagePack.getString("Motors")), DashboardSearchItemModel(LanguagePack.getString("Classifieds")),
                DashboardSearchItemModel(LanguagePack.getString("Property for Sale")), DashboardSearchItemModel(LanguagePack.getString("Property for Rent")), DashboardSearchItemModel(LanguagePack.getString("Jobs")),
                DashboardSearchItemModel(LanguagePack.getString("Community")))

        if (subCategory != null) {
            searchItemList.clear()
            searchItemList.add(DashboardSearchItemModel(LanguagePack.getString("All")))
            for (subCategoryName in subCategory!!) {
                searchItemList.add(DashboardSearchItemModel(LanguagePack.getString(subCategoryName.name)))
            }
        }

        val simpleSearchDialogCompat = SimpleSearchDialogCompat(context, LanguagePack.getString("Search..."),
                LanguagePack.getString("What are you looking for?"), null, searchItemList,
                SearchResultListener<DashboardSearchItemModel> { dialog, item, position ->
                    SessionState.instance.continueBrowsingText = categoryName + " | " + item.title!!
                    SessionState.instance.continueBrowsingCategoryId = categoryId
                    SessionState.instance.continueBrowsingImage = iconUrl
                    SessionState.instance.continueBrowsingSubCategoryId = if (position == 0) "0" else subCategory.get(position - 1).id!!
                    SessionState.instance.saveValuesToPreferences(parentActivity, AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_TEXT.toString(), SessionState.instance.continueBrowsingText)
                    SessionState.instance.saveValuesToPreferences(parentActivity, AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_CATEGORY_ID.toString(), SessionState.instance.continueBrowsingCategoryId)
                    SessionState.instance.saveValuesToPreferences(parentActivity, AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_SUB_CATEGORY_ID.toString(), SessionState.instance.continueBrowsingSubCategoryId)
                    SessionState.instance.saveValuesToPreferences(parentActivity, AppConstants.Companion.PREFERENCES.CONTINUE_BROWSING_IMAGE.toString(), SessionState.instance.continueBrowsingImage)
                    if (!subCategory.isNullOrEmpty()) {
                        val bundle = Bundle()
                        bundle.putString(AppConstants.SELECTED_CATEGORY_ID, categoryId)
                        val subCategoryId: String = if (position == 0) "0" else subCategory.get(position - 1).id!!
                        bundle.putString(AppConstants.SELECTED_SUB_CATEGORY_ID, subCategoryId)
                        parentActivity.startActivity(ProductByCategoryActivity::class.java, false, bundle)
                    }

                    dialog.dismiss()
                })
        simpleSearchDialogCompat.show()
        //simpleSearchDialogCompat.setOnDismissListener { Utility.hideKeyboardFromDialogs(activity!!) }
        val typeface = Typeface.createFromAsset(context?.assets,
                "fonts/roboto_italic.ttf")
        if (simpleSearchDialogCompat.titleTextView != null) {
            simpleSearchDialogCompat.titleTextView.setTextColor(context.resources.getColor(R.color.shadow_black))
        }

        if (simpleSearchDialogCompat.searchBox != null) {
            simpleSearchDialogCompat.searchBox.setTextColor(context.resources.getColor(R.color.shadow_black))
            simpleSearchDialogCompat.searchBox.typeface = typeface
            simpleSearchDialogCompat.searchBox.setHintTextColor(context.resources.getColor(R.color.light_gray))
        }
        if (simpleSearchDialogCompat.recyclerView != null && simpleSearchDialogCompat.recyclerView.findViewHolderForAdapterPosition(0) != null
                && simpleSearchDialogCompat.recyclerView.findViewHolderForAdapterPosition(0)!!.itemView  != null){
            if ((simpleSearchDialogCompat.recyclerView.findViewHolderForAdapterPosition(0)!!.itemView) is TextView) {
                val recyclerTextView = simpleSearchDialogCompat.recyclerView.findViewHolderForAdapterPosition(0)!!.itemView as TextView
                recyclerTextView.setTextColor(context.resources.getColor(R.color.shadow_black))
                recyclerTextView.typeface = typeface
            }
        }
    }
}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val categoryIcon = view.top_layout_category_icon
    val categoryName = view.top_layout_category_text
    val categoryParentLayout = view.category_parent_layout
}