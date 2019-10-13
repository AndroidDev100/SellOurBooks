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
    private var iosDialog:IOSDialog? = null
    val productDataList: ArrayList<ProductsData> = arrayListOf()
    var productPageNumber = 1
    var lastProductPageNumber = 1
    var isProductDataLoading = false

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
        my_products_recycler_view.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, 20, true))
        initializingRecyclerViewScrollListener()
    }

    private fun setUpPullToRefresh() {
        my_posted_property_pull_to_refresh?.apply {
            setWaveColor(AppConstants.SEARCH_PULL_TO_REFRESH_COLOR.toInt())
            setColorSchemeColors(AppConstants.PULL_TO_REFRESH_COLOR_SCHEME, AppConstants.PULL_TO_REFRESH_COLOR_SCHEME)
            setOnRefreshListener {
                isRefreshing = true
                fetchProductList(false)
            }
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
        isProductDataLoading = true
        val productInputData = ProductInputData()
        productInputData.limit = AppConstants.PRODUCT_LOADING_LIMIT
        if (my_posted_property_pull_to_refresh != null && my_posted_property_pull_to_refresh.isRefreshing) {
            lastProductPageNumber = productPageNumber
            productPageNumber = 1
        }
        productInputData.pageNumber = productPageNumber.toString()
        productInputData.userId = SessionState.instance.userId

        RetrofitController.fetchProductsForUser(productInputData, this)
    }

    override fun onResponse(call: Call<List<ProductsData>>?, response: Response<List<ProductsData>>?) {
        if(!this.isFinishing) {
            iosDialog?.dismiss()
            if(response != null && response.isSuccessful) {
                if (my_posted_property_pull_to_refresh != null && my_posted_property_pull_to_refresh.isRefreshing) {
                    productDataList.clear()
                }
                productDataList.addAll(response.body())
                if(!productDataList.isNullOrEmpty()) {
                    no_my_products_frame.visibility = View.GONE
                    my_products_recycler_view.visibility = View.VISIBLE
                    if (my_products_recycler_view.adapter != null) {
                        my_products_recycler_view.adapter?.notifyDataSetChanged()
                    } else  {
                        my_products_recycler_view.adapter = DashboardItemAdapter(productDataList, this, true)
                    }
                } else {
                    no_my_products_frame.visibility = View.VISIBLE
                    my_products_recycler_view.visibility = View.GONE
                }

                my_posted_property_pull_to_refresh?.isRefreshing = false
            }
        }
        isProductDataLoading = false
    }

    override fun onFailure(call: Call<List<ProductsData>>?, t: Throwable?) {
        if (!this.isFinishing) {
            iosDialog?.dismiss()
            isProductDataLoading = false
            if (productDataList.isNullOrEmpty()) {
                no_my_products_frame.visibility = View.VISIBLE
                my_products_recycler_view.visibility = View.GONE
                Utility.showSnackBar(my_posted_product_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this)
            }
            if (my_posted_property_pull_to_refresh != null && my_posted_property_pull_to_refresh.isRefreshing) {
                productPageNumber = lastProductPageNumber
                my_posted_property_pull_to_refresh?.isRefreshing = false
            }
        }
    }

    override fun onProductItemClicked(productId: String?, productName: String?, userName: String?) {
        val bundle = Bundle()
        bundle.putString(AppConstants.PRODUCT_ID, productId)
        bundle.putString(AppConstants.PRODUCT_NAME, productName)
        bundle.putString(AppConstants.PRODUCT_OWNER_NAME, userName)
        startActivity(DashboardProductDetailActivity::class.java, false, bundle)
    }

    private fun initializingRecyclerViewScrollListener() {
        my_products_recycler_view.initScrollListener(object : LazyProductLoading {
            override fun onProductLoadRequired(currentVisibleItem: Int) {
                val itemSizeForLazyLoading = productDataList.size - AppConstants.PRODUCT_LOADING_OFFSET
                if (!isProductDataLoading && productDataList != null && currentVisibleItem >= itemSizeForLazyLoading) {
                    isProductDataLoading = true
                    productPageNumber += 1
                    fetchProductList(false)
                }
            }
        })
    }
}
