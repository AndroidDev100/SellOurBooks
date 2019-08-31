package com.bylancer.classified.bylancerclassified.settings

import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.dashboard.OnProductItemClickListener
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.Utility


class MyFavoriteItemAdapter(private val dashboardItemList : List<DashboardDetailModel>, private val onProductItemClickListener: OnProductItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder : RecyclerView.ViewHolder? = DashboardListItemLayoutItem(R.layout.dashboard_top_picks_item_layout, parent)
        return holder!!
    }

    override fun getItemCount(): Int {
        return dashboardItemList.size
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder, position : Int) {
        val dataModel : DashboardDetailModel = dashboardItemList.get(position)
        val dashboardListItemLayout : DashboardListItemLayoutItem = holder as DashboardListItemLayoutItem
        dashboardListItemLayout.listItemDescription!!.text = dataModel.title
        dashboardListItemLayout.listItemDistance!!.text = dataModel.location
        dashboardListItemLayout.listItemPrice!!.text = dataModel.price
        var symbol = ""
        if (dataModel.currency != null) {
            symbol = Utility.decodeUnicode(dataModel.currency!!)
        }
        if (AppConstants.CURRENCY_IN_LEFT.equals(dataModel.currencyInLeft)) {
            dashboardListItemLayout.listItemPrice!!.text  = symbol + dataModel.price
        } else {
            dashboardListItemLayout.listItemPrice!!.text  = dataModel.price + symbol
        }

        if (!dataModel.productImages.isNullOrEmpty()) {
            Glide.with(dashboardListItemLayout.listItemImageView!!.context).load(AppConstants.PRODUCT_IMAGE_URL + dataModel.productImages?.get(0)).apply(RequestOptions().fitCenter()).into(dashboardListItemLayout.listItemImageView!!)
        }

        dashboardListItemLayout.itemView.setOnClickListener {
            if(onProductItemClickListener != null) {
                onProductItemClickListener.onProductItemClicked(dataModel.productId, dataModel.title, dataModel.sellerUsername)
            }
        }
    }

    private inner class DashboardListItemLayoutItem(id : Int, parent: ViewGroup) : BaseVH(id, parent) {
        var listItemImageView : ImageView? = null
        var listItemPrice : AppCompatTextView? = null
        var listItemDescription : AppCompatTextView? = null
        var listItemDistance : AppCompatTextView? = null
        init {
            listItemImageView = findViewById(R.id.dashboard_item_image_view)
            listItemPrice = findViewById(R.id.dashboard_item_price)
            listItemDescription = findViewById(R.id.dashboard_item_description)
            listItemDistance = findViewById(R.id.dashboard_item_distance)
        }
    }

    internal abstract inner class BaseVH(id: Int, parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(id, parent, false)) {
        fun <T : View> findViewById(id: Int) : T {
            return this.itemView.findViewById(id)
        }
    }

    enum class DashboardItemTypes private constructor(val type: Int) {
        TOP_LAYOUT(0),
        LIST_ITEM(1) {};

        companion object {
            internal fun getByVal(type : Int) : DashboardItemTypes {
                for(item in DashboardItemTypes.values()) {
                    if(type == item.type) {
                        return item
                    }
                }
                return TOP_LAYOUT
            }
        }
    }
}