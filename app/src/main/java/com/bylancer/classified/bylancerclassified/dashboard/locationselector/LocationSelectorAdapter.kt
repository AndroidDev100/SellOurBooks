package com.bylancer.classified.bylancerclassified.dashboard.locationselector

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bylancer.classified.bylancerclassified.R


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