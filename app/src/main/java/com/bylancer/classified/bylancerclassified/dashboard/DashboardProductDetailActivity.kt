package com.bylancer.classified.bylancerclassified.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.login.LoginActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import kotlinx.android.synthetic.main.activity_dashboard_product_detail.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Build
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.model.Marker
import android.widget.Toast
import android.support.v4.app.ActivityCompat
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.constraint.ConstraintSet
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.AppCompatTextView
import android.view.Gravity
import android.widget.TextView
import android.view.ViewGroup
import android.view.WindowManager
import com.bylancer.classified.bylancerclassified.chat.ChatActivity
import com.bylancer.classified.bylancerclassified.database.DatabaseTaskAsyc
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.webservices.makeanoffer.MakeAnOfferData
import com.bylancer.classified.bylancerclassified.webservices.makeanoffer.MakeAnOfferStatus
import com.bylancer.classified.bylancerclassified.widgets.CustomAlertDialog
import com.gmail.samehadar.iosdialog.IOSDialog

class DashboardProductDetailActivity: BylancerBuilderActivity(), Callback<DashboardDetailModel>, View.OnClickListener,
        OnMapReadyCallback {

    var mCurrLocationMarker: Marker? = null
    private var mMap: GoogleMap? = null
    var animUpDown: Animation? = null
    val MY_PERMISSIONS_REQUEST_LOCATION = 87
    var phoneNumber: String? = null
    var ownerEmail: String? = null
    var mDashboardDetailModel:DashboardDetailModel? = null
    var iosDialog: IOSDialog? = null

    override fun setLayoutView() = R.layout.activity_dashboard_product_detail

    override fun initialize(savedInstanceState: Bundle?) {
        if (SessionState.instance.isLoggedIn) {
            login_required_product_detail_card_view.visibility = View.GONE
        } else {
            login_required_product_detail_card_view.visibility = View.VISIBLE
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission()
        }

        iosDialog = Utility.showProgressView(this@DashboardProductDetailActivity, LanguagePack.getString("Sending..."))

        product_detail_screen_sms_text_view.text = LanguagePack.getString(getString(R.string.sms))
        product_detail_screen_email_text_view.text = LanguagePack.getString(getString(R.string.email_id))
        product_detail_screen_call_text_view.text = LanguagePack.getString(getString(R.string.call))
        product_detail_screen_chat_text_view.text = LanguagePack.getString(getString(R.string.chat))
        make_an_offer_text_view.text = LanguagePack.getString(getString(R.string.make_an_offer))
        login_to_know_more_text_View.text = LanguagePack.getString(getString(R.string.login_to_know_more))
        product_detail_age.text = LanguagePack.getString(getString(R.string.age))
        start_login_screen_button.text = LanguagePack.getString(getString(R.string.login))
        product_detail_posted_by.text = LanguagePack.getString(getString(R.string.posted_by))
        product_detail_phone_number.text = LanguagePack.getString(getString(R.string.phone_number))
        product_detail_product_status.text = LanguagePack.getString(getString(R.string.status))
        product_detail_description.text = LanguagePack.getString(getString(R.string.description))
        product_detail_location.text = LanguagePack.getString(getString(R.string.location))

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.product_detail_product_map) as SupportMapFragment?

        mapFragment!!.getMapAsync(this)

        val bundle = intent.getBundleExtra(AppConstants.BUNDLE)
        if (bundle != null) {
            product_detail_title_text_view.text = bundle.getString(AppConstants.PRODUCT_NAME, "")
            getProductDetails(bundle.getString(AppConstants.PRODUCT_ID, ""))
        }
    }

    private fun getProductDetails(productId: String) {
        parent_scroll_view.visibility = View.GONE
        progress_view_dashboard_detail_frame.visibility = View.VISIBLE
        animUpDown = Utility.animateProgressView(this, progress_view_dashboard_detail)
        RetrofitController.fetchProductDetails(productId, this)
    }

    override fun onResponse(call: Call<DashboardDetailModel>?, response: Response<DashboardDetailModel>?) {
        progress_view_dashboard_detail_frame.visibility = View.GONE
        progress_view_dashboard_detail.clearAnimation()
        animUpDown = null

        if(response != null && response.isSuccessful) {
            mDashboardDetailModel =  response.body()
            if (mDashboardDetailModel != null) {
                initializeUI(mDashboardDetailModel!!)
            }
        }
    }

    override fun onFailure(call: Call<DashboardDetailModel>?, t: Throwable?) {
        progress_view_dashboard_detail_frame.visibility = View.GONE
        progress_view_dashboard_detail.visibility = View.GONE
        progress_view_dashboard_detail.clearAnimation()
        animUpDown = null
        Utility.showSnackBar(dashboard_product_detail_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this@DashboardProductDetailActivity)
    }

    private fun initializeUI(dashboardDetailModel: DashboardDetailModel) {
        parent_scroll_view.visibility = View.VISIBLE
        if(dashboardDetailModel.images != null && dashboardDetailModel.images!!.size > 0 && dashboardDetailModel.originalImagesPath != null) {
            product_detail_image_view_pager.adapter = ProductDetailViewPagerAdapter(this, dashboardDetailModel.images!!, dashboardDetailModel.originalImagesPath!!)
        }

        if(SessionState.instance.isLoggedIn) {
            post_login_product_detail_layout.visibility = View.VISIBLE
        }

        product_name_in_detail.text = dashboardDetailModel.title
        var symbol = Utility.decodeUnicode(dashboardDetailModel.currency!!)
        if (AppConstants.CURRENCY_IN_LEFT.equals(dashboardDetailModel.currencyInLeft)) {
            product_detail_price_text_view.text = symbol + dashboardDetailModel.price
        } else {
            product_detail_price_text_view.text = dashboardDetailModel.price + symbol
        }

        product_detail_timeline_text_view.text = dashboardDetailModel.createdAt
        product_details_category_text_view.text = dashboardDetailModel.tag
        product_detail_age_desc.text = dashboardDetailModel.updatedAt
        product_detail_description_detail.text = dashboardDetailModel.description
        product_detail_product_status_desc.text = dashboardDetailModel.status
        product_detail_phone_number_desc.text = if(AppConstants.HIDE_PHONE.equals(dashboardDetailModel.hidePhone)) dashboardDetailModel.phone else getString(R.string.hidden)
        product_detail_posted_by_desc.text = dashboardDetailModel.sellerName
        phoneNumber = dashboardDetailModel.phone
        ownerEmail = dashboardDetailModel.sellerEmail
        product_detail_location_detail.text = dashboardDetailModel.country + " > " + dashboardDetailModel.state + " > " + dashboardDetailModel.city

        DatabaseTaskAsyc(this@DashboardProductDetailActivity,
                mDashboardDetailModel!!, product_detail_favorite_image_view, dashboard_product_detail_parent_layout, true).execute()

        var parentLayoutIdToMatchConstraint = product_detail_product_status_separator.id
        if(dashboardDetailModel.customData != null) {
            for(i in 0..(dashboardDetailModel.customData!!.size - 1)) {
                val customDataElement = dashboardDetailModel.customData!![i]
                if (customDataElement != null && customDataElement.title != null && customDataElement.value != null) {
                    parentLayoutIdToMatchConstraint = addCustomDataDynamically(parentLayoutIdToMatchConstraint, customDataElement.title!!, customDataElement.value!!)
                }
            }
        }

        if(mMap != null) {
            val latLng = LatLng((dashboardDetailModel.mapLatitude)!!.toDouble(), (dashboardDetailModel.mapLongitude)!!.toDouble())
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            mCurrLocationMarker = mMap?.addMarker(markerOptions)
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            mMap?.animateCamera(CameraUpdateFactory.zoomTo(15.0F))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap?.setMapType(GoogleMap.MAP_TYPE_NORMAL)
        mMap?.getUiSettings()?.setZoomControlsEnabled(true)
        mMap?.getUiSettings()?.setZoomGesturesEnabled(true)
        mMap?.getUiSettings()?.setCompassEnabled(true)
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap?.setMyLocationEnabled(true)
            }
        } else {
            mMap?.setMyLocationEnabled(true)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.product_detail_back_image_view -> onBackPressed()
            R.id.make_an_offer_text_view -> {
                if (SessionState.instance.isLoggedIn) {
                    if(mDashboardDetailModel != null && product_detail_price_text_view != null) {
                        if (!SessionState.instance.email.equals(mDashboardDetailModel?.sellerEmail)) {
                            makeAnOffer(product_detail_price_text_view.text.toString())
                        } else {
                            Utility.showSnackBar(dashboard_product_detail_parent_layout, getString(R.string.posted_by_you), this@DashboardProductDetailActivity)
                        }
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.product_detail_warning_image_view -> {
                if (SessionState.instance.isLoggedIn) {

                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.product_detail_share_image_view -> {
                Utility.shareProduct("https://appsgeek.in", getString(R.string.app_name), this)
            }
            R.id.product_detail_favorite_image_view -> {
                if (SessionState.instance.isLoggedIn) {
                    if (mDashboardDetailModel != null) {
                        DatabaseTaskAsyc(this@DashboardProductDetailActivity,
                                mDashboardDetailModel!!, product_detail_favorite_image_view, dashboard_product_detail_parent_layout, false).execute()
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.start_login_screen_button -> {
                if (SessionState.instance.isLoggedIn) {

                } else {
                    startActivity(LoginActivity::class.java, false)
                }
            }
            R.id.product_detail_screen_chat -> {
                if (SessionState.instance.isLoggedIn) {
                    if (mDashboardDetailModel != null && mDashboardDetailModel!!.sellerUsername != null) {
                        if (!SessionState.instance.email.equals(mDashboardDetailModel!!.sellerEmail)) {
                            val bundle = Bundle()
                            bundle.putString(AppConstants.CHAT_TITLE, mDashboardDetailModel!!.sellerName)
                            bundle.putString(AppConstants.CHAT_USER_NAME, mDashboardDetailModel!!.sellerUsername)
                            bundle.putString(AppConstants.CHAT_USER_IMAGE, mDashboardDetailModel!!.sellerImage)
                            startActivity(ChatActivity::class.java, false, bundle)
                        } else {
                            Utility.showSnackBar(dashboard_product_detail_parent_layout, getString(R.string.posted_by_you), this@DashboardProductDetailActivity)
                        }
                    } else {
                        Utility.showSnackBar(dashboard_product_detail_parent_layout, getString(R.string.some_wrong), this@DashboardProductDetailActivity)
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.product_detail_screen_call -> {
                if (SessionState.instance.isLoggedIn) {
                    if(phoneNumber != null && checkLocationPermission()) {
                        val intent = Intent(Intent.ACTION_CALL)
                        intent.data = Uri.parse("tel:" + phoneNumber)
                        startActivity(intent)
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.product_detail_screen_sms -> {
                if (SessionState.instance.isLoggedIn) {
                    if(checkLocationPermission()) {
                        startActivityForResult(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null)), 0);
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
            R.id.product_detail_screen_email -> {
                if (SessionState.instance.isLoggedIn) {
                    if (ownerEmail != null) {
                        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                "mailto", ownerEmail, null));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Interested in "+ product_detail_title_text_view.text);
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi, \n \n"+ LanguagePack.getString("I am interested in your property") + " " + product_detail_title_text_view.text + ".\n "+ LanguagePack.getString("We can have discussion on") +" \n\n" + LanguagePack.getString("Regards")+",\n"+ SessionState.instance.displayName);
                        startActivityForResult(Intent.createChooser(emailIntent,  LanguagePack.getString("Send email...")), 0);
                    } else {
                        Utility.showSnackBar(dashboard_product_detail_parent_layout, LanguagePack.getString("Looks like owner has not shared his email id"), this)
                    }
                } else {
                    startActivity(LoginRequiredActivity::class.java, false)
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mMap?.setMyLocationEnabled(true)
                    }
                } else {
                    Toast.makeText(this, "permission denied",
                            Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.SEND_SMS),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        } else {
            return true
        }
    }

    private fun addCustomDataDynamically(parentLayoutId: Int, captionText: String, detailText: String): Int {
        val set =  ConstraintSet()

        val captionTextView = TextView(this)
        captionTextView.id = View.generateViewId()
        captionTextView.text = LanguagePack.getString(captionText)
        captionTextView.setTextColor(resources.getColor(R.color.disable_icon_color))
        captionTextView.setPadding(Utility.valueInDp(10, this), Utility.valueInDp(10, this), Utility.valueInDp(10, this), Utility.valueInDp(10, this));
        post_login_product_detail_layout.addView(captionTextView, 0)
        set.clone(post_login_product_detail_layout)
        set.connect(captionTextView.getId(), ConstraintSet.LEFT,
                post_login_product_detail_layout.id, ConstraintSet.LEFT, 15)
        set.connect(captionTextView.getId(), ConstraintSet.TOP,
                parentLayoutId, ConstraintSet.BOTTOM, 10)
        set.applyTo(post_login_product_detail_layout)

        val detailTextView = TextView(this)
        detailTextView.id = View.generateViewId()
        detailTextView.layoutParams = ViewGroup.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT)
        detailTextView.text = detailText
        detailTextView.gravity = Gravity.RIGHT
        detailTextView.setTextColor(resources.getColor(R.color.shadow_black))
        detailTextView.setPadding(Utility.valueInDp(15, this), Utility.valueInDp(10, this), Utility.valueInDp(10, this), Utility.valueInDp(10, this));
        post_login_product_detail_layout.addView(detailTextView, 0)
        set.clone(post_login_product_detail_layout)
        set.connect(detailTextView.getId(), ConstraintSet.RIGHT,
                post_login_product_detail_layout.id, ConstraintSet.RIGHT, 15)
        set.connect(detailTextView.getId(), ConstraintSet.TOP,
                parentLayoutId, ConstraintSet.BOTTOM, 10)
        set.connect(detailTextView.getId(), ConstraintSet.LEFT,
                captionTextView.id, ConstraintSet.RIGHT, 10)
        set.setHorizontalBias(detailTextView.getId(), 1f);
        set.applyTo(post_login_product_detail_layout)

        val separatorView = View(this)
        separatorView.id = View.generateViewId()
        separatorView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                Utility.valueInDp(1, this))
        separatorView.setBackgroundColor(resources.getColor(R.color.button_disabled_color))
        post_login_product_detail_layout.addView(separatorView, 0)
        set.clone(post_login_product_detail_layout)
        set.connect(separatorView.getId(), ConstraintSet.LEFT,
                post_login_product_detail_layout.id, ConstraintSet.LEFT, 0)
        set.connect(separatorView.getId(), ConstraintSet.RIGHT,
                post_login_product_detail_layout.id, ConstraintSet.RIGHT, 0)
        set.connect(separatorView.getId(), ConstraintSet.TOP,
                detailTextView.id, ConstraintSet.BOTTOM, 10)
        set.applyTo(post_login_product_detail_layout)

        return separatorView.id
    }

    private fun makeAnOffer(askingPrice: String) {
        val makeAnOfferDialog = CustomAlertDialog(this, R.style.custom_login_dialog)
        makeAnOfferDialog.setContentView(R.layout.make_an_offer)
        makeAnOfferDialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        makeAnOfferDialog.getWindow().setBackgroundDrawable(ColorDrawable(resources.getColor(android.R.color.transparent)))
        makeAnOfferDialog.setCanceledOnTouchOutside(true)
        makeAnOfferDialog.show()

        val dialogTitle = makeAnOfferDialog.findViewById(R.id.make_an_offer_title_text_view) as AppCompatTextView
        val dialogSubTitle = makeAnOfferDialog.findViewById(R.id.make_an_offer_title_sub_text_view) as AppCompatTextView
        val dialogEditText = makeAnOfferDialog.findViewById(R.id.user_bid_edit_text) as AppCompatEditText
        val dialogConfirmButton = makeAnOfferDialog.findViewById(R.id.bid_confirm_button) as AppCompatButton
        val dialogCancelButton = makeAnOfferDialog.findViewById(R.id.bid_cancel_button) as AppCompatButton

        dialogTitle.text = LanguagePack.getString("Make an Offer")
        dialogSubTitle.text = LanguagePack.getString("Seller asking price is") + " \""+ askingPrice +"\""
       // dialogSubTitle.text = String.format(LanguagePack.getString("Seller asking price is \" %s \""), askingPrice)
        dialogConfirmButton.text = LanguagePack.getString("Confirm")
        dialogCancelButton.text = LanguagePack.getString("Cancel")

        dialogConfirmButton.setOnClickListener() {
            //Utility.hideKeyboardFromDialogs(this@DashboardProductDetailActivity)
            if (dialogEditText.text.isNullOrEmpty()) {
                return@setOnClickListener
            }
            makeAnOfferDialog.dismiss()
            iosDialog?.show()
            sendMakeAnOfferRequest(dialogEditText.text.toString())
        }
        dialogCancelButton.setOnClickListener() {
           // Utility.hideKeyboardFromDialogs(this@DashboardProductDetailActivity)
            makeAnOfferDialog.dismiss()
        }

    }

    private fun sendMakeAnOfferRequest(offer : String) {
        val makeAnOfferData = MakeAnOfferData()
        makeAnOfferData.email = mDashboardDetailModel?.sellerEmail!!
        makeAnOfferData.message = SessionState.instance.displayName + " " + LanguagePack.getString("is interested to buy") + " ${mDashboardDetailModel?.title!!} " + LanguagePack.getString("at") + " " + Utility.decodeUnicode(mDashboardDetailModel?.currency!!)+ " $offer "
        makeAnOfferData.ownerName = mDashboardDetailModel?.sellerName!!
        makeAnOfferData.productId = mDashboardDetailModel?.id!!
        makeAnOfferData.productName = mDashboardDetailModel?.title!!
        makeAnOfferData.userId = mDashboardDetailModel?.sellerEmail!!
        makeAnOfferData.type = "make_offer"
        makeAnOfferData.senderId = SessionState.instance.userId
        makeAnOfferData.senderName = SessionState.instance.displayName
        makeAnOfferData.subject = LanguagePack.getString("Offer from") + " " + getString(R.string.app_name)
        RetrofitController.makeAnOffer(makeAnOfferData,object: Callback<MakeAnOfferStatus> {
            override fun onFailure(call: Call<MakeAnOfferStatus>?, t: Throwable?) {
                removeProgressBar()
                Utility.showSnackBar(dashboard_product_detail_parent_layout, LanguagePack.getString(getString(R.string.internet_issue)), this@DashboardProductDetailActivity)
            }

            override fun onResponse(call: Call<MakeAnOfferStatus>?, response: Response<MakeAnOfferStatus>?) {
                removeProgressBar()
                Utility.showSnackBar(dashboard_product_detail_parent_layout, LanguagePack.getString(getString(R.string.offer_submitted)), this@DashboardProductDetailActivity)
            }

        })
    }

    private fun removeProgressBar() {
        if (iosDialog != null) iosDialog?.dismiss()
    }
}