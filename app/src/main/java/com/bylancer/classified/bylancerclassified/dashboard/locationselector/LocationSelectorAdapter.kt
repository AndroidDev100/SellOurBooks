package com.bylancer.classified.bylancerclassified.dashboard.locationselector

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.utils.checkIfNumber
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductsData


class LocationSelectorAdapter(private val locationItemList : List<String>, private val onItemClickListener: LocationSelectedListener, private val isCityList: Boolean = false) : RecyclerView.Adapter<LocationSelectorAdapter.LocationSelectorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationSelectorViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.adapter_location_selector, parent, false)
        var holder = LocationSelectorViewHolder(itemView)
        return holder!!
    }

    override fun getItemCount(): Int {
        return locationItemList.size
    }

    override fun onBindViewHolder(holder : LocationSelectorViewHolder, position : Int) {
        val location : String = locationItemList[position]
        holder.locationNameTextView?.text = location
        if (isCityList) {
            holder.listItemImageView?.visibility = View.GONE
        } else {
            holder.listItemImageView?.visibility = View.VISIBLE
        }
        holder.itemCardView?.setOnClickListener {
            onItemClickListener?.onLocationSelected(position)
        }
    }

    inner class LocationSelectorViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var listItemImageView : ImageView? = null
        var locationNameTextView : AppCompatTextView? = null
        var itemCardView : CardView? = null
        init {
            listItemImageView = itemView.findViewById(R.id.location_next_image_view)
            locationNameTextView = itemView.findViewById(R.id.location_name_text_view)
            itemCardView = itemView.findViewById(R.id.location_selector_card_view)
        }
    }
}