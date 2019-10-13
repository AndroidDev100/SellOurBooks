package com.bylancer.classified.bylancerclassified.settings

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.dashboard.OnProductItemClickListener
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.Utility


class MyFavoriteItemAdapter(private val favItemList : List<DashboardDetailModel>, private val onProductItemClickListener: OnProductItemClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var holder : RecyclerView.ViewHolder? = favItemListItemLayoutItem(R.layout.dashboard_top_picks_item_layout, parent)
        return holder!!
    }

    override fun getItemCount(): Int {
        return favItemList.size
    }

    override fun onBindViewHolder(holder : RecyclerView.ViewHolder, position : Int) {
        val dataModel : DashboardDetailModel = favItemList.get(position)
        val favItemListItemLayout : favItemListItemLayoutItem = holder as favItemListItemLayoutItem
        favItemListItemLayout.listItemDescription!!.text = dataModel.title
        favItemListItemLayout.listItemDistance!!.text = dataModel.location
        favItemListItemLayout.listItemPrice!!.text = dataModel.price
        var symbol = ""
        if (dataModel.currency != null) {
            symbol = Utility.decodeUnicode(dataModel.currency!!)
        }
        if (AppConstants.CURRENCY_IN_LEFT.equals(dataModel.currencyInLeft)) {
            favItemListItemLayout.listItemPrice!!.text  = symbol + dataModel.price
        } else {
            favItemListItemLayout.listItemPrice!!.text  = dataModel.price + symbol
        }

        if (!dataModel.productImages.isNullOrEmpty()) {
            Glide.with(favItemListItemLayout.listItemImageView!!.context).load(AppConstants.PRODUCT_IMAGE_URL + dataModel.productImages?.get(0)).apply(RequestOptions().fitCenter()).into(favItemListItemLayout.listItemImageView!!)
        }

        if (AppConstants.IS_ACTIVE.equals(dataModel.featured)) {
            holder.favItemItemFeaturedTagTextView?.text = LanguagePack.getString("Featured")
            holder.favItemItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.favItemItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
        }

        if (AppConstants.IS_ACTIVE.equals(dataModel.urgent)) {
            holder.favItemItemFeaturedTagTextView?.visibility = View.VISIBLE
            if (holder.favItemItemFeaturedTagTextView?.text!!.isEmpty()) {
                holder.favItemItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.favItemItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                holder.favItemItemFeaturedTagTextView?.text = LanguagePack.getString("Urgent")
            } else {
                holder.favItemItemFeaturedTagTextView?.setBackgroundColor(getColor(holder.favItemItemFeaturedTagTextView?.resources, R.color.new_ui_red_background)!!)
                holder.favItemItemUrgentTagTextView?.setBackgroundColor(getColor(holder.favItemItemFeaturedTagTextView?.resources, R.color.denied_red)!!)
                holder.favItemItemUrgentTagTextView?.text = LanguagePack.getString("Urgent")
                holder.favItemItemUrgentTagTextView?.visibility = View.VISIBLE
            }

        }

        if (holder.favItemItemFeaturedTagTextView?.text.isNullOrEmpty()) {
            holder.favItemItemFeaturedTagTextView?.visibility = View.GONE
        }

        if (AppConstants.IS_ACTIVE.equals(dataModel.highlight)) {
            holder.favItemItemCardView?.background = ContextCompat.getDrawable(holder.favItemFeaturedItemParentLayout?.context!!, R.drawable.layout_shadow)
            holder.favItemFeaturedItemParentLayout?.setBackgroundColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.swipe_refresh_orange)!!)
            holder.listItemDescription?.setTextColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.list_drawer_background_pressed)!!)
            holder.listItemPrice?.setTextColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.dark_green)!!)
            holder.listItemDistance?.setTextColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.colorPrimary)!!)
            holder.listItemDescription?.setTypeface(getHighlightedTypeFace(holder.listItemDescription?.context!!), Typeface.BOLD)
            holder.listItemPrice?.setTypeface(getHighlightedTypeFace(holder.listItemPrice?.context!!), Typeface.BOLD)
            holder.listItemDistance?.setTypeface(getHighlightedTypeFace(holder.listItemDistance?.context!!), Typeface.BOLD)
        } else {
            holder.favItemFeaturedItemParentLayout?.setBackgroundColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
            holder.favItemItemCardView?.setCardBackgroundColor(getColor(holder.favItemFeaturedItemParentLayout?.resources, R.color.snow_color_background)!!)
            holder.favItemItemCardView?.background = ContextCompat.getDrawable(holder.favItemFeaturedItemParentLayout?.context!!, R.drawable.layout_no_shadow)
            holder.favItemItemCardView?.useCompatPadding = true
            holder.listItemDescription?.setTextColor(getColor(holder.listItemDescription?.resources, R.color.black_color_text)!!)
            holder.listItemPrice?.setTextColor(getColor(holder.listItemPrice?.resources, R.color.denied_red)!!)
            holder.listItemDistance?.setTextColor(getColor(holder.listItemDistance?.resources, R.color.dark_gray)!!)
            holder.listItemDescription?.setTypeface(getNormalTypeFace(holder.listItemDescription?.context!!), Typeface.NORMAL)
            holder.listItemPrice?.setTypeface(getNormalTypeFace(holder.listItemPrice?.context!!), Typeface.NORMAL)
            holder.listItemDistance?.setTypeface(getNormalTypeFace(holder.listItemDistance?.context!!), Typeface.NORMAL)
        }

        favItemListItemLayout.itemView.setOnClickListener {
            if(onProductItemClickListener != null) {
                onProductItemClickListener.onProductItemClicked(dataModel.productId, dataModel.title, dataModel.sellerUsername)
            }
        }
    }

    private inner class favItemListItemLayoutItem(id : Int, parent: ViewGroup) : BaseVH(id, parent) {
        var listItemImageView : ImageView? = null
        var listItemPrice : AppCompatTextView? = null
        var listItemDescription : AppCompatTextView? = null
        var listItemDistance : AppCompatTextView? = null
        var favItemItemFeaturedTagTextView : AppCompatTextView? = null
        var favItemItemUrgentTagTextView : AppCompatTextView? = null
        var favItemFeaturedItemParentLayout : LinearLayout? = null
        var favItemItemCardView : CardView? = null
        init {
            listItemImageView = findViewById(R.id.dashboard_item_image_view)
            listItemPrice = findViewById(R.id.dashboard_item_price)
            listItemDescription = findViewById(R.id.dashboard_item_description)
            listItemDistance = findViewById(R.id.dashboard_item_distance)
            favItemFeaturedItemParentLayout = findViewById(R.id.dashboard_featured_item_parent_layout)
            favItemItemFeaturedTagTextView = findViewById(R.id.dashboard_item_featured_tag_text_view)
            favItemItemUrgentTagTextView = findViewById(R.id.dashboard_item_urgent_tag_text_view)
            favItemItemCardView = findViewById(R.id.dashboard_item_card_view_layout)
        }
    }

    internal abstract inner class BaseVH(id: Int, parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(id, parent, false)) {
        fun <T : View> findViewById(id: Int) : T {
            return this.itemView.findViewById(id)
        }
    }

    enum class favItemItemTypes private constructor(val type: Int) {
        TOP_LAYOUT(0),
        LIST_ITEM(1) {};

        companion object {
            internal fun getByVal(type : Int) : favItemItemTypes {
                for(item in favItemItemTypes.values()) {
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