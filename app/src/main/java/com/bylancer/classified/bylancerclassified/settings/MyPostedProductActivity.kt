package com.bylancer.classified.bylancerclassified.settings

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.dashboard.DashboardItemAdapter
import com.bylancer.classified.bylancerclassified.dashboard.DashboardProductDetailActivity
import com.bylancer.classified.bylancerclassified.dashboard.OnProductItemClickListener
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductInputData
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductsData
import com.gmail.samehadar.iosdialog.IOSDialog
import kotlinx.android.synthetic.main.activity_my_posted_product.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyPostedProductActivity : BylancerBuilderActivity(), OnProductItemClickListener, View.OnClickListener, Callback<List<ProductsData>> {
    val SPAN_COUNT = 2
    var iosDialog:IOSDialog? = null

    override fun setLayoutView() = R.layout.activity_my_posted_product

    override fun initialize(savedInstanceState: Bundle?) {
        iosDialog =  Utility.showProgressView(this@MyPostedProductActivity, LanguagePack.getString("Loading..."))
        my_products_title_text_view.text = LanguagePack.getString(getString(R.string.my_posted_product))
        no_my_products_added.text = LanguagePack.getString(getString(R.string.no_post))

        setUpPullToRefresh()
        initializePostedPropertyRecyclerView()
        fetchProductList()
    }

    private fun initializePostedPropertyRecyclerView() {
        my_products_recycler_view.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        my_products_recycler_view.setHasFixedSize(false)
        my_products_recycler_view.isNestedScrollingEnabled = false
        my_products_recycler_view.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, 10, false))
    }

    private fun setUpPullToRefresh() {
        my_posted_property_pull_to_refresh.setWaveColor(AppConstants.SEARCH_PULL_TO_REFRESH_COLOR.toInt())
        my_posted_property_pull_to_refresh.setColorSchemeColors(AppConstants.PULL_TO_REFRESH_COLOR_SCHEME, AppConstants.PULL_TO_REFRESH_COLOR_SCHEME)
        my_posted_property_pull_to_refresh.setOnRefreshListener {
            my_posted_property_pull_to_refresh?.isRefreshing = true
            fetchProductList(false)
        }
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.my_products_back_image_view) {
            onBackPressed()
        }
    }

    private fun fetchProductList(isLoadingRequired : Boolean = true) {
        if (isLoadingRequired) {
            iosDialog?.show()
        }
        val productInputData = ProductInputData()
        productInputData.limit = "300"
        productInputData.pageNumber = "1"
        productInputData.userId = SessionState.instance.userId

        RetrofitController.fetchProductsForUser(productInputData, this)
    }

    override fun onResponse(call: Call<List<ProductsData>>?, response: Response<List<ProductsData>>?) {
        if(!this.isFinishing) {
            iosDialog?.dismiss()
            if(response != null && response.isSuccessful) {
                val productList: List<ProductsData> = response.body()
                if(!productList.isNullOrEmpty()) {
                    no_my_products_frame.visibility = View.GONE
                    my_products_recycler_view.visibility = View.VISIBLE
                    my_products_recycler_view.adapter = DashboardItemAdapter(productList, this)
                } else {
                    no_my_products_frame.visibility = View.VISIBLE
                    my_products_recycler_view.visibility = View.GONE
                }

                my_posted_property_pull_to_refresh?.isRefreshing = false
            }
        }
    }

    override fun onFailure(call: Call<List<ProductsData>>?, t: Throwable?) {
        iosDialog?.dismiss()
        no_my_products_frame.visibility = View.VISIBLE
        my_products_recycler_view.visibility = View.GONE
        my_posted_property_pull_to_refresh?.isRefreshing = false
        Utility.showSnackBar(my_posted_product_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this)
    }

    override fun onProductItemClicked(productId: String?, productName: String?, userName: String?) {
        val bundle = Bundle()
        bundle.putString(AppConstants.PRODUCT_ID, productId)
        bundle.putString(AppConstants.PRODUCT_NAME, productName)
        bundle.putString(AppConstants.PRODUCT_OWNER_NAME, userName)
        startActivity(DashboardProductDetailActivity::class.java, false, bundle)
    }
}
