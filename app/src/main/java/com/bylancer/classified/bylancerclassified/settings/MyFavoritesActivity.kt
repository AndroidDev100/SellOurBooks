package com.bylancer.classified.bylancerclassified.settings

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.dashboard.DashboardProductDetailActivity
import com.bylancer.classified.bylancerclassified.dashboard.OnProductItemClickListener
import com.bylancer.classified.bylancerclassified.database.MyFavouriteProductsGetterAsycTask
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.GridSpacingItemDecoration
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import kotlinx.android.synthetic.main.activity_my_favorites.*

class MyFavoritesActivity : BylancerBuilderActivity(), OnProductItemClickListener, View.OnClickListener {
    val SPAN_COUNT = 2

    override fun setLayoutView() = R.layout.activity_my_favorites

    override fun initialize(savedInstanceState: Bundle?) {
        my_fav_title_text_view.text = LanguagePack.getString(getString(R.string.my_favorites))
        no_fav_added.text = LanguagePack.getString(getString(R.string.no_favorites))

        my_fav_recycler_view.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        my_fav_recycler_view.setHasFixedSize(false)
        my_fav_recycler_view.isNestedScrollingEnabled = false
        my_fav_recycler_view.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, 10, false))
    }

    override fun onResume() {
        super.onResume()
        MyFavouriteProductsGetterAsycTask(this, object: FetchAllSavedProduct {
            override fun onAllMyFavoriteProductsFetched(savedProductList: List<DashboardDetailModel>) {
                if (!savedProductList.isNullOrEmpty()) {
                    no_fav_frame.visibility = View.GONE
                    my_fav_recycler_view.visibility = View.VISIBLE
                    my_fav_recycler_view.adapter = MyFavoriteItemAdapter(savedProductList, this@MyFavoritesActivity)
                } else {
                    no_fav_frame.visibility = View.VISIBLE
                    my_fav_recycler_view.visibility = View.GONE
                }
            }
        }).execute()
    }

    override fun onProductItemClicked(productId: String?, productName: String?, userName: String?) {
        val bundle = Bundle()
        bundle.putString(AppConstants.PRODUCT_ID, productId)
        bundle.putString(AppConstants.PRODUCT_NAME, productName)
        bundle.putString(AppConstants.PRODUCT_OWNER_NAME, userName)
        startActivity(DashboardProductDetailActivity::class.java, false, bundle)
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.my_favorite_back_image_view) {
            onBackPressed()
        }
    }
}
