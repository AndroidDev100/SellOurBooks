package com.bylancer.classified.bylancerclassified.uploadproduct.postdetails

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bylancer.classified.bylancerclassified.R
import kotlinx.android.synthetic.main.upload_selected_image_adapter.view.*

class UploadSelectedImageAdapter(val items : MutableList<Uri>) : RecyclerView.Adapter<UploadSelectedImageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): UploadSelectedImageViewHolder {
        return UploadSelectedImageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.upload_selected_image_adapter, parent, false))
    }

    override fun onBindViewHolder(holder: UploadSelectedImageViewHolder, position: Int) {
        val imageUrl = items.get(position)
        Glide.with(holder?.selectedImage.context).load(imageUrl).into(holder?.selectedImage)
        holder?.cancelSelectedImage.setOnClickListener() {
            items.removeAt(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class UploadSelectedImageViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val selectedImage = view.selected_picture_image_view
    val cancelSelectedImage = view.cancel_selected_picture_image_view
}