package com.bylancer.classified.bylancerclassified.uploadproduct.categoryselection

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.appconfig.Category
import com.bylancer.classified.bylancerclassified.uploadproduct.OnUploadCategorySelection
import kotlinx.android.synthetic.main.category_adapter.view.*

class UploadCategoryAdapter(val items : List<Category>, val onCategorySelection: OnUploadCategorySelection) : RecyclerView.Adapter<UploadCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): UploadCategoryViewHolder {
        return UploadCategoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.upload_category_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: UploadCategoryViewHolder, position: Int) {
        val category: Category = items.get(position)
        holder?.categoryName?.text = category.name
        Glide.with(holder?.categoryIcon.context).load(category.picture).into(holder?.categoryIcon)
        holder?.categoryParentLayout.setOnClickListener() {
            if (onCategorySelection != null) {
                onCategorySelection.onCategorySelected(category.id!!, position)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class UploadCategoryViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val categoryIcon = view.top_layout_category_icon
    val categoryName = view.top_layout_category_text
    val categoryParentLayout = view.category_parent_layout
}