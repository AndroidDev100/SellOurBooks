package com.bylancer.classified.bylancerclassified.dashboard

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import androidx.appcompat.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.utils.Utility

class ProductDetailViewPagerAdapter(private val mContext: Context, private val images: List<String>, private val imagePath:String) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val layout = inflater.inflate(R.layout.product_detail_view_pager_adapter, collection, false) as ViewGroup
        val productImage = layout.findViewById(R.id.product_image_view_pager) as ImageView
        val productImageCounter = layout.findViewById(R.id.product_image_counter_text_view) as AppCompatTextView
        productImageCounter.text = ((position + 1).toString() + "/" + (images.size).toString())
        Glide.with(mContext).load(imagePath + images[position]).apply(RequestOptions().placeholder(Utility.getCircularProgressDrawable(mContext))).into(productImage)
        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return ""
    }

    override fun isViewFromObject(view: View, p1: Any): Boolean {
        return view === p1
    }

}