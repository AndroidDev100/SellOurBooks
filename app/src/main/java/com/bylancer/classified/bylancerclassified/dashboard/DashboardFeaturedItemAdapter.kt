package com.bylancer.classified.bylancerclassified.dashboard

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


class DashboardFeaturedItemAdapter(private val dashboardItemList : List<ProductsData>, private val onProductItemClickListener: OnProductItemClickListener) : RecyclerView.Adapter<DashboardFeaturedItemAdapter.DashboardListItemLayoutItem>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardListItemLayoutItem {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_featured_item_layout, parent, false)
        var holder = DashboardListItemLayoutItem(itemView)
        return holder!!
    }

    override fun getItemCount(): Int {
        return dashboardItemList.size
    }

    override fun onBindViewHolder(holder : DashboardListItemLayoutItem, position : Int) {
        val dataModel : ProductsData = dashboardItemList.get(position)
        holder.listItemDescription!!.text = dataModel.productName
        holder.listItemDistance!!.text = dataModel.location
        if (!dataModel.price.isNullOrEmpty() && checkIfNumber(dataModel.price!!)) {
            holder.listItemPrice!!.text = dataModel.price
            holder.listItemPrice!!.visibility = View.VISIBLE
        } else {
            holder.listItemPrice!!.visibility = View.GONE
        }

        var symbol = ""
        if (dataModel.currency != null) {
            symbol = Utility.decodeUnicode(dataModel.currency!!)
        }
        if (AppConstants.CURRENCY_IN_LEFT.equals(dataModel.currencyInLeft)) {
            holder.listItemPrice!!.text  = symbol + dataModel.price
        } else {
            holder.listItemPrice!!.text  = dataModel.price + symbol
        }

        holder.dashboardItemUrgentTagTextView?.visibility = View.GONE

        if (AppConstants.IS_ACTIVE.equals(dataModel.featured)) {
            holder.dashboardItemFeaturedTagTextView?.visibility = View.VISIBLE
            holder.dashboardItemFeaturedTagTextView?.text = LanguagePack.getString("Featured")
            holder.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.dashboardItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
        } else {
            holder.dashboardItemFeaturedTagTextView?.visibility = View.GONE
        }

        if (AppConstants.IS_ACTIVE.equals(dataModel.urgent)) {
            if (holder.dashboardItemFeaturedTagTextView?.text!!.isEmpty()) {
                holder.dashboardItemFeaturedTagTextView?.visibility = View.VISIBLE
                holder.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.dashboardItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                holder.dashboardItemFeaturedTagTextView?.text = LanguagePack.getString("Urgent")
                holder.dashboardItemUrgentTagTextView?.visibility = View.GONE
            } else {
                holder.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.dashboardItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
                holder.dashboardItemUrgentTagTextView?.setBackgroundColor(getColor(holder.dashboardItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                holder.dashboardItemUrgentTagTextView?.text = LanguagePack.getString("Urgent")
                holder.dashboardItemUrgentTagTextView?.visibility = View.VISIBLE
            }

        }

        if (holder.dashboardItemFeaturedTagTextView?.text.isNullOrEmpty()) {
            holder.dashboardItemFeaturedTagTextView?.visibility = View.GONE
        }

        if (AppConstants.IS_ACTIVE.equals(dataModel.highlight)) {
            holder.dashboardItemCardView?.background = ContextCompat.getDrawable(holder.dashboardFeaturedItemParentLayout?.context!!, R.drawable.layout_shadow)
            holder.dashboardFeaturedItemParentLayout?.setBackgroundColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.swipe_refresh_orange)!!)
            holder.listItemDescription?.setTextColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.list_drawer_background_pressed)!!)
            holder.listItemPrice?.setTextColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.dark_green)!!)
            holder.listItemDistance?.setTextColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.colorPrimary)!!)
            holder.listItemDescription?.setTypeface(getHighlightedTypeFace(holder.listItemDescription?.context!!), Typeface.BOLD)
            holder.listItemPrice?.setTypeface(getHighlightedTypeFace(holder.listItemPrice?.context!!), Typeface.BOLD)
            holder.listItemDistance?.setTypeface(getHighlightedTypeFace(holder.listItemDistance?.context!!), Typeface.BOLD)
        } else {
            holder.dashboardFeaturedItemParentLayout?.setBackgroundColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
            holder.dashboardItemCardView?.setCardBackgroundColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
            holder.dashboardItemCardView?.background = ContextCompat.getDrawable(holder.dashboardFeaturedItemParentLayout?.context!!, R.drawable.layout_no_shadow)
            holder.dashboardItemCardView?.useCompatPadding = true
            holder.listItemDescription?.setTextColor(getColor(holder.listItemDescription?.resources, R.color.black_color_text)!!)
            holder.listItemPrice?.setTextColor(getColor(holder.listItemPrice?.resources, R.color.denied_red)!!)
            holder.listItemDistance?.setTextColor(getColor(holder.listItemDistance?.resources, R.color.dark_gray)!!)
            holder.listItemDescription?.setTypeface(getNormalTypeFace(holder.listItemDescription?.context!!), Typeface.NORMAL)
            holder.listItemPrice?.setTypeface(getNormalTypeFace(holder.listItemPrice?.context!!), Typeface.NORMAL)
            holder.listItemDistance?.setTypeface(getNormalTypeFace(holder.listItemDistance?.context!!), Typeface.NORMAL)
        }

        Glide.with(holder.listItemImageView!!.context).load(dataModel.picture).apply(RequestOptions().fitCenter()).into(holder.listItemImageView!!)
        holder.dashboardFeaturedItemParentLayout?.setOnClickListener {
            if(onProductItemClickListener != null) {
                onProductItemClickListener.onProductItemClicked(dataModel.id, dataModel.productName, dataModel.username)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dashboardItemList.get(position).itemType
    }

    inner class DashboardListItemLayoutItem(itemView : View) : RecyclerView.ViewHolder(itemView) {
        var listItemImageView : ImageView? = null
        var listItemPrice : AppCompatTextView? = null
        var listItemDescription : AppCompatTextView? = null
        var listItemDistance : AppCompatTextView? = null
        var dashboardItemFeaturedTagTextView : AppCompatTextView? = null
        var dashboardItemUrgentTagTextView : AppCompatTextView? = null
        var dashboardFeaturedItemParentLayout : LinearLayout? = null
        var dashboardItemCardView : CardView? = null
        init {
            listItemImageView = itemView.findViewById(R.id.dashboard_item_image_view)
            listItemPrice = itemView.findViewById(R.id.dashboard_item_price)
            listItemDescription = itemView.findViewById(R.id.dashboard_item_description)
            listItemDistance = itemView.findViewById(R.id.dashboard_item_distance)
            dashboardFeaturedItemParentLayout = itemView.findViewById(R.id.dashboard_featured_item_parent_layout)
            dashboardItemFeaturedTagTextView = itemView.findViewById(R.id.dashboard_item_featured_tag_text_view)
            dashboardItemUrgentTagTextView = itemView.findViewById(R.id.dashboard_item_urgent_tag_text_view)
            dashboardItemCardView = itemView.findViewById(R.id.dashboard_featured_card_view_layout)
        }
    }

    private fun getColor(resources: Resources?, colorCode: Int) = resources?.getColor(colorCode)

    private fun getHighlightedTypeFace(context: Context) : Typeface? = ResourcesCompat.getFont(context, R.font.museo_bold)

    private fun getNormalTypeFace(context: Context) : Typeface? = ResourcesCompat.getFont(context, R.font.roboto_regular)
}