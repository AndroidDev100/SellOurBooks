package com.bylancer.classified.bylancerclassified.dashboard

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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


class DashboardItemAdapter(private val dashboardItemList : List<ProductsData>, private val onProductItemClickListener: OnProductItemClickListener, private val isFromMyProduct: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder : RecyclerView.ViewHolder? = null
        when (DashboardItemTypes.getByVal(viewType)) {
            DashboardItemTypes.TOP_LAYOUT -> {
                holder = DashboardTopLayoutItem(R.layout.dashboard_top_item_layout, parent)
            }
            DashboardItemTypes.LIST_ITEM -> {
                holder = DashboardListItemLayoutItem(R.layout.dashboard_top_picks_item_layout, parent)
            }
        }
        return holder!!
    }

    override fun getItemCount(): Int {
        return dashboardItemList.size
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder, position : Int) {
        val dataModel : ProductsData = dashboardItemList.get(position)
        when(DashboardItemTypes.getByVal(dataModel.itemType)) {
            DashboardItemTypes.TOP_LAYOUT -> {
                val dashboardTopLayoutItem : DashboardTopLayoutItem =  holder as DashboardTopLayoutItem
            }
            DashboardItemTypes.LIST_ITEM -> {
                val dashboardListItemLayout : DashboardListItemLayoutItem = holder as DashboardListItemLayoutItem
                dashboardListItemLayout.listItemDescription!!.text = dataModel.productName
                dashboardListItemLayout.listItemDistance!!.text = dataModel.location
                if (!dataModel.price.isNullOrEmpty() && checkIfNumber(dataModel.price!!)) {
                    dashboardListItemLayout.listItemPrice!!.text = dataModel.price
                    dashboardListItemLayout.listItemPrice!!.visibility = View.VISIBLE
                } else {
                    dashboardListItemLayout.listItemPrice!!.visibility = View.GONE
                }

                var symbol = ""
                if (dataModel.currency != null) {
                    symbol = Utility.decodeUnicode(dataModel.currency!!)
                }
                if (AppConstants.CURRENCY_IN_LEFT.equals(dataModel.currencyInLeft)) {
                    dashboardListItemLayout.listItemPrice!!.text  = symbol + dataModel.price
                } else {
                    dashboardListItemLayout.listItemPrice!!.text  = dataModel.price + symbol
                }

                dashboardListItemLayout.dashboardItemUrgentTagTextView?.visibility = View.GONE

                if (AppConstants.IS_ACTIVE.equals(dataModel.featured)) {
                    dashboardListItemLayout.dashboardItemFeaturedTagTextView?.visibility = View.VISIBLE
                    dashboardListItemLayout.dashboardItemFeaturedTagTextView?.text = LanguagePack.getString("Featured")
                    dashboardListItemLayout.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(dashboardListItemLayout.dashboardItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
                } else {
                    dashboardListItemLayout.dashboardItemFeaturedTagTextView?.visibility = View.GONE
                }

                if (AppConstants.IS_ACTIVE.equals(dataModel.urgent)) {
                    if (dashboardListItemLayout.dashboardItemFeaturedTagTextView?.text!!.isEmpty()) {
                        dashboardListItemLayout.dashboardItemFeaturedTagTextView?.visibility = View.VISIBLE
                        dashboardListItemLayout.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(dashboardListItemLayout.dashboardItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                        dashboardListItemLayout.dashboardItemFeaturedTagTextView?.text = LanguagePack.getString("Urgent")
                        dashboardListItemLayout.dashboardItemUrgentTagTextView?.visibility = View.GONE
                    } else {
                        dashboardListItemLayout.dashboardItemFeaturedTagTextView?.setBackgroundColor(getColor(dashboardListItemLayout.dashboardItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
                        dashboardListItemLayout.dashboardItemUrgentTagTextView?.setBackgroundColor(getColor(dashboardListItemLayout.dashboardItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                        dashboardListItemLayout.dashboardItemUrgentTagTextView?.text = LanguagePack.getString("Urgent")
                        dashboardListItemLayout.dashboardItemUrgentTagTextView?.visibility = View.VISIBLE
                    }

                }

                if (dashboardListItemLayout.dashboardItemFeaturedTagTextView?.text.isNullOrEmpty()) {
                    dashboardListItemLayout.dashboardItemFeaturedTagTextView?.visibility = View.GONE
                }

                if (AppConstants.IS_ACTIVE.equals(dataModel.highlight)) {
                    dashboardListItemLayout.dashboardItemCardView?.background = ContextCompat.getDrawable(dashboardListItemLayout.dashboardFeaturedItemParentLayout?.context!!, R.drawable.layout_shadow)
                    dashboardListItemLayout.dashboardFeaturedItemParentLayout?.setBackgroundColor(getColor(dashboardListItemLayout.dashboardFeaturedItemParentLayout?.resources, R.color.swipe_refresh_orange)!!)
                    dashboardListItemLayout.listItemDescription?.setTextColor(getColor(dashboardListItemLayout.dashboardFeaturedItemParentLayout?.resources, R.color.list_drawer_background_pressed)!!)
                    dashboardListItemLayout.listItemPrice?.setTextColor(getColor(dashboardListItemLayout.dashboardFeaturedItemParentLayout?.resources, R.color.dark_green)!!)
                    dashboardListItemLayout.listItemDistance?.setTextColor(getColor(dashboardListItemLayout.dashboardFeaturedItemParentLayout?.resources, R.color.colorPrimary)!!)
                    dashboardListItemLayout.listItemDescription?.setTypeface(getHighlightedTypeFace(dashboardListItemLayout.listItemDescription?.context!!), Typeface.BOLD)
                    dashboardListItemLayout.listItemPrice?.setTypeface(getHighlightedTypeFace(dashboardListItemLayout.listItemPrice?.context!!), Typeface.BOLD)
                    dashboardListItemLayout.listItemDistance?.setTypeface(getHighlightedTypeFace(dashboardListItemLayout.listItemDistance?.context!!), Typeface.BOLD)
                } else {
                    dashboardListItemLayout.dashboardFeaturedItemParentLayout?.setBackgroundColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
                    dashboardListItemLayout.dashboardItemCardView?.setCardBackgroundColor(getColor(holder.dashboardFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
                    dashboardListItemLayout.dashboardItemCardView?.background = ContextCompat.getDrawable(holder.dashboardFeaturedItemParentLayout?.context!!, R.drawable.layout_no_shadow)
                    dashboardListItemLayout.dashboardItemCardView?.useCompatPadding = true
                    dashboardListItemLayout.listItemDescription?.setTextColor(getColor(dashboardListItemLayout.listItemDescription?.resources, R.color.black_color_text)!!)
                    dashboardListItemLayout.listItemPrice?.setTextColor(getColor(dashboardListItemLayout.listItemPrice?.resources, R.color.denied_red)!!)
                    dashboardListItemLayout.listItemDistance?.setTextColor(getColor(dashboardListItemLayout.listItemDistance?.resources, R.color.dark_gray)!!)
                    dashboardListItemLayout.listItemDescription?.setTypeface(getNormalTypeFace(dashboardListItemLayout.listItemDescription?.context!!), Typeface.NORMAL)
                    dashboardListItemLayout.listItemPrice?.setTypeface(getNormalTypeFace(dashboardListItemLayout.listItemPrice?.context!!), Typeface.NORMAL)
                    dashboardListItemLayout.listItemDistance?.setTypeface(getNormalTypeFace(dashboardListItemLayout.listItemDistance?.context!!), Typeface.NORMAL)
                }

                if (isFromMyProduct && !AppConstants.PRODUCT_ACTIVE.equals(dataModel?.status)) {
                    holder.dashboardItemStatusTextView?.visibility = View.VISIBLE
                    holder.dashboardItemStatusTextView?.text = LanguagePack.getString("Pending")
                } else {
                    holder.dashboardItemStatusTextView?.visibility = View.GONE
                }

                Glide.with(dashboardListItemLayout.listItemImageView!!.context).load(dataModel.picture).apply(RequestOptions().fitCenter()).into(dashboardListItemLayout.listItemImageView!!)
                dashboardListItemLayout.itemView.setOnClickListener {
                    if(onProductItemClickListener != null) {
                        onProductItemClickListener.onProductItemClicked(dataModel.id, dataModel.productName, dataModel.username)
                    }
                }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return dashboardItemList.get(position).itemType
    }

    private inner class DashboardTopLayoutItem(id : Int, parent: ViewGroup) : BaseVH(id, parent) {
        init {

        }
    }

    private inner class DashboardListItemLayoutItem(id : Int, parent: ViewGroup) : BaseVH(id, parent) {
        var listItemImageView : ImageView? = null
        var listItemPrice : AppCompatTextView? = null
        var listItemDescription : AppCompatTextView? = null
        var listItemDistance : AppCompatTextView? = null
        var dashboardItemFeaturedTagTextView : AppCompatTextView? = null
        var dashboardItemUrgentTagTextView : AppCompatTextView? = null
        var dashboardItemStatusTextView : AppCompatTextView? = null
        var dashboardFeaturedItemParentLayout : LinearLayout? = null
        var dashboardItemCardView : CardView? = null
        init {
            listItemImageView = findViewById(R.id.dashboard_item_image_view)
            listItemPrice = findViewById(R.id.dashboard_item_price)
            listItemDescription = findViewById(R.id.dashboard_item_description)
            listItemDistance = findViewById(R.id.dashboard_item_distance)
            dashboardFeaturedItemParentLayout = findViewById(R.id.dashboard_featured_item_parent_layout)
            dashboardItemFeaturedTagTextView = findViewById(R.id.dashboard_item_featured_tag_text_view)
            dashboardItemUrgentTagTextView = findViewById(R.id.dashboard_item_urgent_tag_text_view)
            dashboardItemStatusTextView = findViewById(R.id.dashboard_item_status_text_view)
            dashboardItemCardView = findViewById(R.id.dashboard_item_card_view_layout)
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

    private fun getColor(resources: Resources?, colorCode: Int) = resources?.getColor(colorCode)

    private fun getHighlightedTypeFace(context: Context) : Typeface? = ResourcesCompat.getFont(context, R.font.museo_bold)

    private fun getNormalTypeFace(context: Context) : Typeface? = ResourcesCompat.getFont(context, R.font.roboto_regular)
}