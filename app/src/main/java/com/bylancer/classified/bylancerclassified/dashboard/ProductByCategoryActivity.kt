package com.bylancer.classified.bylancerclassified.dashboard

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.widget.*
import android.text.InputType
import android.view.View
import android.view.WindowManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.uploadproduct.postdetails.ProductAdditionalInfoWebview
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductInputData
import com.bylancer.classified.bylancerclassified.webservices.productlist.ProductsData
import com.bylancer.classified.bylancerclassified.widgets.CustomAlertDialog
import com.gmail.samehadar.iosdialog.IOSDialog
import kotlinx.android.synthetic.main.activity_product_by_category.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.text.SimpleDateFormat

class ProductByCategoryActivity : BylancerBuilderActivity(), OnProductItemClickListener, View.OnClickListener, Callback<List<ProductsData>> {
    private val SPAN_COUNT = 2
    var iosDialog:IOSDialog? = null
    var productPageNumber = 1
    var lastProductPageNumber = 1
    var isProductDataLoading = false
    var productCategoryId = "1"
    var productSubCategoryId = "1"
    private var ADDITIONAL_SEARCH_INFO_REQUEST = 6
    var filterByAdditionInfoSelected = true
    private val SORT_BY_NEW_TO_OLD_DATE  = 1
    private val SORT_BY_OLD_TO_NEW_DATE  = 2
    private val SORT_BY_PRICE_HIGH_TO_LOW = 3
    private val SORT_BY_PRICE_LOW_TO_HIGH  = 4
    var sortBySelectedType = 1
    var keywords = ""
    val productDataList: ArrayList<ProductsData> = arrayListOf()

    override fun setLayoutView() = R.layout.activity_product_by_category

    override fun initialize(savedInstanceState: Bundle?) {
        productDataList.clear()
        iosDialog =  Utility.showProgressView(this@ProductByCategoryActivity, LanguagePack.getString("Searching..."))
        product_by_category_title_text_view.text = LanguagePack.getString(getString(R.string.search_result))
        no_product_by_category_added.text = LanguagePack.getString(getString(R.string.no_search_match))
        SessionState.instance.uploadedProductAdditionalInfo = ""

        setUpPullToRefresh()
        initializeProductRecyclerView()

        if (intent != null && intent.getBundleExtra(AppConstants.BUNDLE) != null) {
            var receivedBundle = intent.getBundleExtra(AppConstants.BUNDLE)
            productCategoryId = receivedBundle.getString(AppConstants.SELECTED_CATEGORY_ID, "")
            productSubCategoryId = receivedBundle.getString(AppConstants.SELECTED_SUB_CATEGORY_ID, "")
            keywords = receivedBundle.getString(AppConstants.SELECTED_KEYWORD, "")
        }

        fetchProductList(true)
    }

    private fun setUpPullToRefresh() {
        search_pull_to_refresh?.apply {
            setWaveColor(AppConstants.SEARCH_PULL_TO_REFRESH_COLOR.toInt())
            setColorSchemeColors(AppConstants.PULL_TO_REFRESH_COLOR_SCHEME, AppConstants.PULL_TO_REFRESH_COLOR_SCHEME)
            setOnRefreshListener {
                isRefreshing = true
                fetchProductList(false)
            }
        }
    }

    private fun initializeProductRecyclerView() {
        product_by_category_recycler_view.layoutManager = GridLayoutManager(this, SPAN_COUNT)
        product_by_category_recycler_view.setHasFixedSize(false)
        product_by_category_recycler_view.itemAnimator = DefaultItemAnimator()
        product_by_category_recycler_view.isNestedScrollingEnabled = false
        product_by_category_recycler_view.addItemDecoration(GridSpacingItemDecoration(SPAN_COUNT, 0, true))
        initializingRecyclerViewScrollListener()
    }

    override fun onClick(view: View?) {
        if (view?.id == R.id.product_by_category_back_image_view) {
            onBackPressed()
        } else if (view?.id == R.id.product_by_category_add_additional_info_layout) {
            showFilterAlertDialog()
        } else if (view?.id == R.id.product_by_category_sorting_layout) {
            showSortingAlertDialog()
        }
    }

    private fun fetchProductList(isProgressViewRequired: Boolean) {
        if (isProgressViewRequired) iosDialog?.show()
        isProductDataLoading = true
        val productInputData = ProductInputData()
        productInputData.limit = AppConstants.PRODUCT_LOADING_LIMIT

        if (search_pull_to_refresh != null && search_pull_to_refresh.isRefreshing) {
            lastProductPageNumber = productPageNumber
            productPageNumber = 1
        }
        productInputData.pageNumber = productPageNumber.toString()

        productInputData.status = AppConstants.PRODUCT_STATUS
        productInputData.userId = SessionState.instance.userId
        productInputData.additionalSearchInfo = SessionState.instance.uploadedProductAdditionalInfo
        productInputData.categoryId = productCategoryId
        productInputData.subCategoryId = productSubCategoryId
        productInputData.keywords = keywords
        if ("0".equals(productInputData.subCategoryId)) {
            productInputData.subCategoryId = ""
        }
        productInputData.countryCode = SessionState.instance.selectedCountryCode
        if (!SessionState.instance.selectedStateCode.isNullOrEmpty()) {
            productInputData.stateCode = SessionState.instance.selectedStateCode
        }
        if (!SessionState.instance.selectedCityId.isNullOrEmpty()) {
            productInputData.cityId = SessionState.instance.selectedCityId
        }

        RetrofitController.fetchProductDetailsByCategory(productInputData, this)
    }

    override fun onResponse(call: Call<List<ProductsData>>?, response: Response<List<ProductsData>>?) {
        if (!this.isFinishing) {
            iosDialog?.dismiss()
            if(response != null && response.isSuccessful) {
                if (search_pull_to_refresh != null && search_pull_to_refresh.isRefreshing) {
                    productDataList.clear()
                    search_pull_to_refresh.isRefreshing = false
                }
                productDataList.addAll(response.body())
                if(!productDataList.isNullOrEmpty()) {
                    no_product_by_category_frame.visibility = View.GONE
                    product_by_category_recycler_view.visibility = View.VISIBLE
                    if(product_by_category_recycler_view.adapter == null) {
                        sortByDescendingDate(false)
                        product_by_category_recycler_view.adapter = DashboardItemAdapter(productDataList, this)
                    } else {
                       // product_by_category_recycler_view.adapter?.notifyDataSetChanged()
                        sortByType(sortBySelectedType)
                    }
                } else {
                    no_product_by_category_frame.visibility = View.VISIBLE
                    product_by_category_recycler_view.visibility = View.GONE
                }
            }
        }
        isProductDataLoading = false
    }

    override fun onFailure(call: Call<List<ProductsData>>?, t: Throwable?) {
        if (!this.isFinishing) {
            iosDialog?.dismiss()
            no_product_by_category_frame.visibility = View.VISIBLE
            product_by_category_recycler_view.visibility = View.GONE
            if (search_pull_to_refresh != null && search_pull_to_refresh.isRefreshing) {
                search_pull_to_refresh.isRefreshing = false
                productPageNumber = lastProductPageNumber
            }
            isProductDataLoading = false
            Utility.showSnackBar(product_by_category_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this)
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
        product_by_category_recycler_view.initScrollListener(object : LazyProductLoading {
            override fun onProductLoadRequired(currentVisibleItem: Int) {
                val itemSizeForLazyLoading = productDataList.size - AppConstants.PRODUCT_LOADING_OFFSET
                if (!isProductDataLoading && productDataList != null && currentVisibleItem >= itemSizeForLazyLoading) {
                    productPageNumber += 1
                    fetchProductList(false)
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDITIONAL_SEARCH_INFO_REQUEST && !isFinishing) {
            if (!SessionState.instance.uploadedProductAdditionalInfo.isNullOrEmpty()) {
                productDataList.clear()
                fetchProductList(true)
            }
        }
    }

    private fun showSortingAlertDialog() {
        val sortingDialog = CustomAlertDialog(this, R.style.custom_filter_dialog)
        sortingDialog.setContentView(R.layout.sorting_options_dialog)
        sortingDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        sortingDialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        sortingDialog.setCanceledOnTouchOutside(true)
        sortingDialog.show()

        val dialogTitle = sortingDialog.findViewById(R.id.sorting_title_text_view) as AppCompatTextView

        val sortingDateNewToOldTextView = sortingDialog.findViewById(R.id.sorting_new_to_old_text_view) as AppCompatTextView
        val sortingDateOldToNewTextView = sortingDialog.findViewById(R.id.sorting_old_to_to_text_view) as AppCompatTextView
        val sortingPriceHighToLowTextView = sortingDialog.findViewById(R.id.sorting_price_high_to_low_text_view) as AppCompatTextView
        val sortingPriceLowToHighTextView = sortingDialog.findViewById(R.id.sorting_price_low_to_high_text_view) as AppCompatTextView

        val sortingDateNewToOldLayout = sortingDialog.findViewById(R.id.sorting_new_to_old_layout) as ConstraintLayout
        val sortingDateOldToNewLayout = sortingDialog.findViewById(R.id.sorting_old_to_new_layout) as ConstraintLayout
        val sortingPriceHighToLowLayout = sortingDialog.findViewById(R.id.sorting_price_high_to_low_layout) as ConstraintLayout
        val sortingPriceLowToHighLayout = sortingDialog.findViewById(R.id.sorting_price_low_to_high_layout) as ConstraintLayout

        dialogTitle.text = LanguagePack.getString(getString(R.string.sort_by))
        sortingDateNewToOldTextView.text = LanguagePack.getString(getString(R.string.date_new_to_old))
        sortingDateOldToNewTextView.text = LanguagePack.getString(getString(R.string.date_old_to_new))
        sortingPriceHighToLowTextView.text = LanguagePack.getString(getString(R.string.price_high_low))
        sortingPriceLowToHighTextView.text = LanguagePack.getString(getString(R.string.price_low_high))

        if (sortBySelectedType == 1) {
            sortingDateNewToOldTextView.setTypeface(sortingDateNewToOldTextView.typeface, Typeface.BOLD)
            sortingDateOldToNewTextView.setTypeface(sortingDateOldToNewTextView.typeface, Typeface.NORMAL)
            sortingPriceHighToLowTextView.setTypeface(sortingPriceHighToLowTextView.typeface, Typeface.NORMAL)
            sortingPriceLowToHighTextView.setTypeface(sortingPriceLowToHighTextView.typeface, Typeface.NORMAL)

            sortingDateNewToOldLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
            sortingDateOldToNewLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceHighToLowLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceLowToHighLayout.setBackgroundColor(resources.getColor(R.color.white_background))
        } else if (sortBySelectedType == 2){
            sortingDateNewToOldTextView.setTypeface(sortingDateNewToOldTextView.typeface, Typeface.NORMAL)
            sortingDateOldToNewTextView.setTypeface(sortingDateOldToNewTextView.typeface, Typeface.BOLD)
            sortingPriceHighToLowTextView.setTypeface(sortingPriceHighToLowTextView.typeface, Typeface.NORMAL)
            sortingPriceLowToHighTextView.setTypeface(sortingPriceLowToHighTextView.typeface, Typeface.NORMAL)

            sortingDateNewToOldLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingDateOldToNewLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
            sortingPriceHighToLowLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceLowToHighLayout.setBackgroundColor(resources.getColor(R.color.white_background))
        } else if (sortBySelectedType == 3) {
            sortingDateNewToOldTextView.setTypeface(sortingDateNewToOldTextView.typeface, Typeface.NORMAL)
            sortingDateOldToNewTextView.setTypeface(sortingDateOldToNewTextView.typeface, Typeface.NORMAL)
            sortingPriceHighToLowTextView.setTypeface(sortingPriceHighToLowTextView.typeface, Typeface.BOLD)
            sortingPriceLowToHighTextView.setTypeface(sortingPriceLowToHighTextView.typeface, Typeface.NORMAL)

            sortingDateNewToOldLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingDateOldToNewLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceHighToLowLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
            sortingPriceLowToHighLayout.setBackgroundColor(resources.getColor(R.color.white_background))
        } else {
            sortingDateNewToOldTextView.setTypeface(sortingDateNewToOldTextView.typeface, Typeface.NORMAL)
            sortingDateOldToNewTextView.setTypeface(sortingDateOldToNewTextView.typeface, Typeface.NORMAL)
            sortingPriceHighToLowTextView.setTypeface(sortingPriceHighToLowTextView.typeface, Typeface.NORMAL)
            sortingPriceLowToHighTextView.setTypeface(sortingPriceLowToHighTextView.typeface, Typeface.BOLD)

            sortingDateNewToOldLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingDateOldToNewLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceHighToLowLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            sortingPriceLowToHighLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
        }

        sortingDateNewToOldLayout.setOnClickListener() {
            if (sortBySelectedType != 1) {
                sortBySelectedType = 1
                sortByType(sortBySelectedType)
            }
            sortingDialog.dismiss()
        }

        sortingDateOldToNewLayout.setOnClickListener() {
            if (sortBySelectedType != 2) {
                sortBySelectedType = 2
                sortByType(sortBySelectedType)
            }
            sortingDialog.dismiss()
        }

        sortingPriceHighToLowLayout.setOnClickListener() {
            if (sortBySelectedType != 3) {
                sortBySelectedType = 3
                sortByType(sortBySelectedType)
            }
            sortingDialog.dismiss()
        }

        sortingPriceLowToHighLayout.setOnClickListener() {
            if (sortBySelectedType != 4) {
                sortBySelectedType = 4
                sortByType(sortBySelectedType)
            }
            sortingDialog.dismiss()
        }
    }

    private fun showFilterAlertDialog() {
        val filterDialog = CustomAlertDialog(this, R.style.custom_filter_dialog)
        filterDialog.setContentView(R.layout.filter_options_dialog)
        filterDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        filterDialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        filterDialog.setCanceledOnTouchOutside(true)
        filterDialog.show()

        val dialogTitle = filterDialog.findViewById(R.id.filter_title_text_view) as AppCompatTextView
        val filterByKeywordTextView = filterDialog.findViewById(R.id.filter_keyword_text_view) as AppCompatTextView
        val filterByAdditionalInfoTextView = filterDialog.findViewById(R.id.filter_additional_info_text_view) as AppCompatTextView
        val filterByAdditionalInfoLayout = filterDialog.findViewById(R.id.filter_additional_info_layout) as ConstraintLayout
        val filterByKeywordLayout = filterDialog.findViewById(R.id.filter_keyword_layout) as ConstraintLayout

        dialogTitle.text = LanguagePack.getString(getString(R.string.filter_by))
        filterByKeywordTextView.text = LanguagePack.getString(getString(R.string.filter_by_keyword))
        filterByAdditionalInfoTextView.text = LanguagePack.getString(getString(R.string.additional_info))

        if (filterByAdditionInfoSelected) {
            filterByAdditionalInfoTextView.setTypeface(filterByAdditionalInfoTextView.typeface, Typeface.BOLD)
            filterByKeywordTextView.setTypeface(filterByAdditionalInfoTextView.typeface, Typeface.NORMAL)
            filterByAdditionalInfoLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
            filterByKeywordLayout.setBackgroundColor(resources.getColor(R.color.white_background))
        } else {
            filterByAdditionalInfoTextView.setTypeface(filterByAdditionalInfoTextView.typeface, Typeface.NORMAL)
            filterByKeywordTextView.setTypeface(filterByAdditionalInfoTextView.typeface, Typeface.BOLD)
            filterByAdditionalInfoLayout.setBackgroundColor(resources.getColor(R.color.white_background))
            filterByKeywordLayout.setBackgroundColor(resources.getColor(R.color.light_gray))
        }

        filterByAdditionalInfoLayout.setOnClickListener() {
            filterByAdditionInfoSelected = true
            filterDialog.dismiss()
            val bundle = Bundle()
            bundle.putString(AppConstants.SELECTED_CATEGORY_ID, productCategoryId)
            bundle.putString(AppConstants.SELECTED_SUB_CATEGORY_ID, productSubCategoryId)
            bundle.putString(AppConstants.ADDITIONAL_INFO_ACTIVITY_TITLE, getString(R.string.add_filter))
            startActivityForResult(ProductAdditionalInfoWebview::class.java, false, bundle, ADDITIONAL_SEARCH_INFO_REQUEST)
        }

        filterByKeywordLayout.setOnClickListener() {
            filterByAdditionInfoSelected = false
            filterDialog.dismiss()
            showAlertForFilterByKeyword()
        }
    }

    private fun showAlertForFilterByKeyword() {
        val enterKeywordToSearchDialog = CustomAlertDialog(this, R.style.custom_filter_dialog)
        enterKeywordToSearchDialog.setContentView(R.layout.make_an_offer)
        enterKeywordToSearchDialog.window?.setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        enterKeywordToSearchDialog.window?.setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        enterKeywordToSearchDialog.setCanceledOnTouchOutside(true)
        enterKeywordToSearchDialog.show()

        val dialogTitle = enterKeywordToSearchDialog.findViewById(R.id.make_an_offer_title_text_view) as AppCompatTextView
        val dialogSubTitle = enterKeywordToSearchDialog.findViewById(R.id.make_an_offer_title_sub_text_view) as AppCompatTextView
        val dialogEditText = enterKeywordToSearchDialog.findViewById(R.id.user_bid_edit_text) as AppCompatEditText
        dialogEditText.inputType = InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        val dialogConfirmButton = enterKeywordToSearchDialog.findViewById(R.id.bid_confirm_button) as AppCompatButton
        val dialogCancelButton = enterKeywordToSearchDialog.findViewById(R.id.bid_cancel_button) as AppCompatButton

        dialogTitle.text = LanguagePack.getString("Enter Keyword")
        dialogSubTitle.text = LanguagePack.getString("Enter your keyword to search")
        dialogConfirmButton.text = LanguagePack.getString("Search")
        dialogCancelButton.text = LanguagePack.getString("Cancel")

        dialogConfirmButton.setOnClickListener() {
            //Utility.hideKeyboardFromDialogs(this@DashboardProductDetailActivity)
            if (dialogEditText.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            this.keywords = dialogEditText.text.toString()
            enterKeywordToSearchDialog.dismiss()
            productDataList.clear()
            notifyAfterSorting()
            fetchProductList(true)
            iosDialog?.show()
        }
        dialogCancelButton.setOnClickListener() {
            // Utility.hideKeyboardFromDialogs(this@DashboardProductDetailActivity)
            enterKeywordToSearchDialog.dismiss()
        }
    }

    private fun sortByType (type : Int) {
        when (type) {
            SORT_BY_NEW_TO_OLD_DATE -> sortByDescendingDate(true)
            SORT_BY_OLD_TO_NEW_DATE -> sortByAscendingDate()
            SORT_BY_PRICE_HIGH_TO_LOW -> sortByDescendingPrice()
            SORT_BY_PRICE_LOW_TO_HIGH -> sortByAscendingPrice()
        }
    }
    private fun sortByDescendingDate(isNotifyToAdapterRequired : Boolean) {
       /* if (!productDataList.isNullOrEmpty()) {
            Collections.sort(productDataList, object : Comparator<ProductsData> {
                var f: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
                override fun compare(o1: ProductsData, o2: ProductsData): Int {
                    try {
                        if (o1 == null || o2 == null || o1.createdAt.isNullOrEmpty() || o2.createdAt.isNullOrEmpty()) {
                            return -1
                        }
                        return f.parse(o1.createdAt).compareTo(f.parse(o2.createdAt))
                    } catch (e: Exception) {
                        return 0
                    }

                }
            })

            if (isNotifyToAdapterRequired) notifyAfterSorting()
        } */

        try {
            if (!productDataList.isNullOrEmpty()) {
                var f: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
                productDataList.sortWith(Comparator { p0, p1 -> f.parse(p0.createdAt).compareTo(f.parse(p1.createdAt)) })
            }
        } catch (e: Exception) { }

        if (isNotifyToAdapterRequired) notifyAfterSorting()
    }

    private fun sortByAscendingDate() {
        /*if (!productDataList.isNullOrEmpty()) {
            Collections.sort(productDataList, object : Comparator<ProductsData> {
                var f: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
                override fun compare(o1: ProductsData, o2: ProductsData): Int {
                    try {
                        if (o1 == null || o2 == null || o1.createdAt.isNullOrEmpty() || o2.createdAt.isNullOrEmpty()) {
                            return 0
                        }
                        return f.parse(o1.createdAt).compareTo(f.parse(o2.createdAt))
                    } catch (e: Exception) {
                        return 0
                    }

                }
            })*/

            try {
                if (!productDataList.isNullOrEmpty()) {
                    var f: DateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm")
                    productDataList.sortWith(Comparator { p0, p1 -> f.parse(p0.createdAt).compareTo(f.parse(p1.createdAt)) })
                }
            } catch (e: Exception) { }

            productDataList.reverse()
            notifyAfterSorting()
       // }
    }

    private fun sortByAscendingPrice() {
        try {
            if (!productDataList.isNullOrEmpty()) {
                productDataList.sortWith(Comparator { p0, p1 -> Integer.compare(p0!!.price!!.toInt(), p1!!.price!!.toInt()) })
            }
        } catch (e: Exception) { }
        productDataList.reverse()
        notifyAfterSorting()
    }

    private fun sortByDescendingPrice() {
        try {
            if (!productDataList.isNullOrEmpty()) {
                productDataList.sortWith(Comparator { p0, p1 -> Integer.compare(p0!!.price!!.toInt(), p1!!.price!!.toInt()) })
            }
        } catch (e: Exception) { }
        notifyAfterSorting()
    }

    private fun notifyAfterSorting() {
        if (product_by_category_recycler_view != null && product_by_category_recycler_view.adapter != null) {
            product_by_category_recycler_view.adapter!!.notifyDataSetChanged()
        }
    }
}
