package com.bylancer.classified.bylancerclassified.uploadproduct.categoryselection

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.appconfig.SubCategory
import com.bylancer.classified.bylancerclassified.uploadproduct.OnUploadCategorySelection
import kotlinx.android.synthetic.main.category_adapter.view.*

class UploadSubCategoryAdapter(val items : List<SubCategory>, val onCategorySelection: OnUploadCategorySelection) : RecyclerView.Adapter<UploadSubCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): UploadSubCategoryViewHolder {
        return UploadSubCategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.upload_category_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: UploadSubCategoryViewHolder, position: Int) {
        val subCategory: SubCategory = items.get(position)
        holder?.categoryName?.text = subCategory.name
        if (subCategory.picture != null) {
            Glide.with(holder?.categoryIcon.context).load(subCategory.picture).into(holder?.categoryIcon)
        } else {
            holder?.categoryIcon.setImageResource(R.drawable.sub_category_icon)
        }
        holder?.categoryParentLayout.setOnClickListener() {
            if (onCategorySelection != null) {
                onCategorySelection.onCategorySelected(subCategory.id!!, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class UploadSubCategoryViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val categoryIcon = view.top_layout_category_icon
    val categoryName = view.top_layout_category_text
    val categoryParentLayout = view.category_parent_layout
}