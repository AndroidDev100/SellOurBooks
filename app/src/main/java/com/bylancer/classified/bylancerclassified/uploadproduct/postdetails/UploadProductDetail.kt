package com.bylancer.classified.bylancerclassified.uploadproduct.postdetails

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.asksira.bsimagepicker.BSImagePicker
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.appconfig.Category
import com.bylancer.classified.bylancerclassified.utils.*
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController.Companion.fetchCityDetails
import com.bylancer.classified.bylancerclassified.webservices.settings.CityListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.CountryListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.StateListModel
import com.bylancer.classified.bylancerclassified.webservices.uploadproduct.PostedProductResponseModel
import com.bylancer.classified.bylancerclassified.webservices.uploadproduct.UploadDataDetailModel
import com.bylancer.classified.bylancerclassified.webservices.uploadproduct.UploadProductModel
import com.gmail.samehadar.iosdialog.IOSDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_upload_product_detail.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class UploadProductDetail : BylancerBuilderActivity(), View.OnClickListener, BSImagePicker.OnSingleImageSelectedListener, TextWatcher {
    private var googleMap: GoogleMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val MY_LOCATION_REQUEST = 7
    private val MY_LOCATION_SELECTION = 6
    private val PRODUCT_IMAGE_PICKER = "product_image_picker"
    private val selectedImageList = arrayListOf<Uri>()
    private var productCategoryId = ""
    private var productSubCategoryId = ""
    private var isJobCategory = false
    private var countryCode = ""
    private var stateCode = ""
    private var cityId = ""
    private var selectedImagesToPost = ""
    var mProgressDialog: IOSDialog? = null
    val countryList = arrayListOf<CountryListModel>()
    val stateList = arrayListOf<StateListModel>()
    val cityList = arrayListOf<CityListModel>()

    override fun setLayoutView() = R.layout.activity_upload_product_detail

    override fun initialize(savedInstanceState: Bundle?) {
        upload_listing_under_text_view.text = LanguagePack.getString(getString(R.string.this_is_listing_under))
        upload_detail_enter_a_title_edit_text_input_layout.hint = LanguagePack.getString(getString(R.string.enter_upload_title))
        upload_details_add_images_button.text = LanguagePack.getString(getString(R.string.add_photo))
        upload_detail_hide_phone_switch.text = LanguagePack.getString(getString(R.string.hide_phone))
        upload_detail_is_negotiable_switch.text = LanguagePack.getString(getString(R.string.negotiable))
        upload_detail_enter_phone_edit_text_input_layout.hint = LanguagePack.getString(getString(R.string.phone_no))
        upload_detail_enter_price_text_input_layout.hint = LanguagePack.getString(getString(R.string.price))
        upload_detail_enter_description_edit_text_input_layout.hint = LanguagePack.getString(getString(R.string.description))
        upload_detail_enter_country_text_input_layout.hint = LanguagePack.getString(getString(R.string.country))
        upload_detail_enter_state_text_input_layout.hint = LanguagePack.getString(getString(R.string.state))
        upload_detail_enter_city_text_input_layout.hint = LanguagePack.getString(getString(R.string.city))
        upload_detail_title_text_view.text = LanguagePack.getString(getString(R.string.place_ad))
        upload_detail_pin_in_map_text_view.text = LanguagePack.getString(getString(R.string.tap_to_pin_location))
        upload_product_detail_button.text = LanguagePack.getString(getString(R.string.post))

        if (intent != null && intent.getBundleExtra(AppConstants.BUNDLE) != null && AppConfigDetail.category != null) {
            var bundle = intent.getBundleExtra(AppConstants.BUNDLE)
            upload_detail_enter_a_title_edit_text.setText(bundle.getString(AppConstants.UPLOAD_PRODUCT_SELECTED_TITLE))
            var category = AppConfigDetail.category!!.get(bundle.getInt(AppConstants.SELECTED_CATEGORY_POSITION))
            productCategoryId = category.id!!
            isJobCategory = category.name!!.contains("Job")
            productSubCategoryId = bundle.getString(AppConstants.SELECTED_SUB_CATEGORY_ID, "0")
            setProductListingUnder(category)
        } else {
            Utility.showSnackBar(activity_upload_products_parent_layout, getString(R.string.some_wrong_retry), this)
        }

        if (isJobCategory) {
            upload_detail_enter_price_text_input_layout.hint = LanguagePack.getString(getString(R.string.salary_text))
            upload_details_add_images_button.text = LanguagePack.getString(getString(R.string.upload_cv))
        }

        upload_detail_images_recycler_view.layoutManager = LinearLayoutManager(this);
        upload_detail_images_recycler_view.setHasFixedSize(false)
        upload_detail_images_recycler_view.isNestedScrollingEnabled = false

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        initializeMap(null)

        setTextChangeListeners()
        setItemSelectedListeners()
        fetchCountryList()
    }

    private fun setProductListingUnder(category: Category) {
        for (x in category.subCategory!!) {
            if (x != null && productSubCategoryId.equals(x.id)) {
                upload_listing_under_detail_text_view.text = category.name!! + " > " + x.name
                break
            }
        }
    }

    private fun initializeMap(location: Location?) {
        val latitude = location?.latitude ?: 40.730610
        val longitude = location?.longitude ?: -73.935242
        if (googleMap == null) {
            (supportFragmentManager.findFragmentById(R.id.pin_map) as com.google.android.gms.maps.SupportMapFragment).getMapAsync {
                this.googleMap = it
                if (googleMap != null) {
                    addMarker(latitude, longitude)
                }
            }
        } else {
            addMarker(latitude, longitude)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionState.instance.uploadedProductLongitude.isNullOrEmpty()) {
            initiateFusedLocation()
        } else if (googleMap != null) {
            val latitude = SessionState.instance.uploadedProductLatitude.toDouble()
            val longitude = SessionState.instance.uploadedProductLongitude.toDouble()
            addMarker(latitude, longitude)
            postButtonEnableListener()
        } else {
            initiateFusedLocation()
        }
    }

    private fun addMarker(latitude: Double, longitude : Double) {

        /*MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude, longitude)).title("Your Property").draggable(true);
        marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_icon));
        googleMap.addMarker(marker);*/

        val cameraPosition = CameraPosition.Builder().target(
                LatLng(latitude, longitude)).zoom(15f).build()

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        googleMap?.getUiSettings()?.isZoomControlsEnabled = false
        googleMap?.getUiSettings()?.isScrollGesturesEnabled = false
        googleMap?.getUiSettings()?.isZoomGesturesEnabled = false

        googleMap?.setOnMapClickListener(GoogleMap.OnMapClickListener {
            /*if (location == null) {
                Utility.showSnackBar(activity_upload_products_parent_layout, getString(R.string.map_initializing), this)
            } else {*/
                val locationBundle = Bundle()
                locationBundle.putDouble(AppConstants.SELECTED_PRODUCT_LONGITUDE, longitude)
                locationBundle.putDouble(AppConstants.SELECTED_PRODUCT_LATITUDE, latitude)
                startActivity(LocationSelectionActivity::class.java, false, locationBundle)
           // }
        })
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.close_upload_product_detail_image_view -> {
               // SessionState.instance.uploadedProductLongitude = ""
               // SessionState.instance.uploadedProductLatitude = ""
                onBackPressed()
            }

            R.id.upload_details_add_images_button -> {
                val profileImagePicker = getSingleImagePickerDialog(PRODUCT_IMAGE_PICKER)
                profileImagePicker.show(supportFragmentManager, AppConstants.IMAGE_PICKER_FRAGMENT)
            }

            R.id.upload_product_detail_button -> {
                upload_product_detail_button.isEnabled = false
                postImageDataAndGetPath()
            }

            R.id.upload_detail_add_more_info -> {
                val bundle = Bundle()
                bundle.putString(AppConstants.SELECTED_CATEGORY_ID, productCategoryId)
                bundle.putString(AppConstants.SELECTED_SUB_CATEGORY_ID, productSubCategoryId)
                bundle.putString(AppConstants.ADDITIONAL_INFO_ACTIVITY_TITLE, getString(R.string.add_more_info))
                startActivity(ProductAdditionalInfoWebview::class.java, false, bundle)
            }
        }
    }

    private fun initiateFusedLocation() {
        if (isLocationEnabled() && Utility.checkLocationAndPhonePermission(MY_LOCATION_REQUEST, this)) {
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            initializeMap(location)
                        } else {
                            initiateFusedLocation()
                        }
                    }
        } else {
            showLocationAlertDialog()
        }
    }

    private fun showLocationAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("")
        builder.setCancelable(false)
        builder.setMessage(LanguagePack.getString("Please enable location service to continue. We require it to get your current Ad location"))
        builder.setPositiveButton(LanguagePack.getString("OK")){dialog, _ ->
            dialog.dismiss()
            var myIntent = Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(myIntent)
        }
        builder.create().show()
    }


    /*public override fun onActivityResult(requestCode :Int, resultCode:Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == MY_LOCATION_SELECTION) {

            }
        }
    }*/

    override fun onSingleImageSelected(uri: Uri?, tag: String?) {
        if (upload_detail_images_recycler_view != null) {
            selectedImageList.add(uri!!)
            if (upload_detail_images_recycler_view.adapter != null) {
                upload_detail_images_recycler_view.adapter!!.notifyDataSetChanged()
            } else {
                upload_detail_images_recycler_view.adapter = UploadSelectedImageAdapter(selectedImageList)
            }
            postButtonEnableListener()
        }
    }

    private fun setTextChangeListeners() {
        upload_detail_enter_a_title_edit_text.addTextChangedListener(this)
        upload_detail_enter_phone_edit_text.addTextChangedListener(this)
        upload_detail_enter_price_edit_text.addTextChangedListener(this)
        upload_detail_enter_description_edit_text.addTextChangedListener(this)
        upload_detail_enter_country_edit_text.addTextChangedListener(this)
        upload_detail_enter_state_edit_text.addTextChangedListener(this)
        upload_detail_enter_city_edit_text.addTextChangedListener(this)
    }

    override fun afterTextChanged(p0: Editable?) {
        postButtonEnableListener()
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
    }

    private fun postButtonEnableListener() {
        upload_product_detail_button.isEnabled = !upload_detail_enter_a_title_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_phone_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_price_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_description_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_country_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_state_edit_text.text.isNullOrEmpty()
                && !upload_detail_enter_city_edit_text.text.isNullOrEmpty()
                && !selectedImageList.isNullOrEmpty()
                && !SessionState.instance.uploadedProductLatitude.isNullOrEmpty()
                && !SessionState.instance.uploadedProductLongitude.isNullOrEmpty()
                && !cityId.isNullOrEmpty()
                && !stateCode.isNullOrEmpty()
    }

    /**
     * Methods created below for
     * COUNTRY/ STATE/ CITY LIST
     */

    private fun setItemSelectedListeners() {
        upload_detail_enter_country_edit_text.setOnItemClickListener {
            countryCode = countryList.get(it).lowercaseCode!!
            stateList.clear()
            cityList.clear()
            stateCode = ""
            cityId = ""
            upload_detail_enter_state_edit_text.setText("")
            upload_detail_enter_city_edit_text.setText("")
            fetchStateList(countryCode)
        }

        upload_detail_enter_state_edit_text.setOnItemClickListener {
            cityList.clear()
            cityId = ""
            upload_detail_enter_city_edit_text.setText("")
            stateCode = stateList.get(it).code!!
            fetchCityList(stateList.get(it).code!!)
        }

        upload_detail_enter_city_edit_text.setOnItemClickListener {
            val cityListModel = cityList.get(it)
            cityId = cityListModel.id!!

            //if (SessionState.instance.uploadedProductLongitude.isNullOrEmpty()) {
                SessionState.instance.uploadedProductLatitude = if (cityListModel.long.isNullOrEmpty()) SessionState.instance.uploadedProductLatitude else cityListModel.long!!
                SessionState.instance.uploadedProductLongitude = if (cityListModel.lat.isNullOrEmpty()) SessionState.instance.uploadedProductLongitude else cityListModel.lat!!
                if (!SessionState.instance.uploadedProductLongitude.isNullOrEmpty() && googleMap != null) {
                    addMarker(SessionState.instance.uploadedProductLatitude.toDouble(),
                            SessionState.instance.uploadedProductLongitude.toDouble())
                }
           // }
        }
    }

    private fun fetchCountryList() {
        showProgressDialog(getString(R.string.loading))
        RetrofitController.fetchCountryDetails(object: Callback<List<CountryListModel>> {
            override fun onResponse(call: Call<List<CountryListModel>>?, response: Response<List<CountryListModel>>?) {
                if (!this@UploadProductDetail.isFinishing) {
                    if (response != null && response.isSuccessful && upload_detail_enter_country_edit_text != null) {
                        countryList.addAll(response.body())
                        val listOfCountry = arrayListOf<String>()
                        countryList.forEach { listOfCountry.add(it.name!!)}
                        upload_detail_enter_country_edit_text.setItems(listOfCountry.toArray(arrayOfNulls<String>(countryList.size)))
                    }
                    dismissProgressDialog()
                }
            }

            override fun onFailure(call: Call<List<CountryListModel>>?, t: Throwable?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun fetchStateList(countryId: String) {
        showProgressDialog(getString(R.string.fetch_state))
        RetrofitController.fetchStateDetails(countryId, object: Callback<List<StateListModel>> {
            override fun onResponse(call: Call<List<StateListModel>>?, response: Response<List<StateListModel>>?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful && upload_detail_enter_state_edit_text != null) {
                        stateList.addAll(response.body())
                        val listOfState = arrayListOf<String>()
                        stateList.forEach { listOfState.add(it.name!!)}
                        upload_detail_enter_state_edit_text.setItems(listOfState.toArray(arrayOfNulls<String>(stateList.size)))
                    }
                }
            }

            override fun onFailure(call: Call<List<StateListModel>>?, t: Throwable?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun fetchCityList(stateId: String) {
        showProgressDialog(getString(R.string.fetch_city))
        fetchCityDetails(stateId, object: Callback<List<CityListModel>> {
            override fun onResponse(call: Call<List<CityListModel>>?, response: Response<List<CityListModel>>?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful && upload_detail_enter_city_edit_text != null) {
                        cityList.addAll(response.body())
                        val listOfCity = arrayListOf<String>()
                        cityList.forEach { listOfCity.add(it.name!!)}
                        upload_detail_enter_city_edit_text.setItems(listOfCity.toArray(arrayOfNulls<String>(cityList.size)))
                    }
                }
            }

            override fun onFailure(call: Call<List<CityListModel>>?, t: Throwable?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    fun showProgressDialog(message: String) {
        mProgressDialog = Utility.showProgressView(this, message)
        mProgressDialog?.show()
    }

    fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }

    private fun showNetworkErrorSnackBar() {
        if (activity_upload_products_parent_layout != null) Utility.showSnackBar(activity_upload_products_parent_layout, "Please check your internet connection", this)
    }

    // END HERE

    /***
     * Gather Data to upload product
     */
    private fun getFinalDataToPostAndUploadToServer() {
        val uploadDataDetailModel = UploadDataDetailModel()
        uploadDataDetailModel.userId = SessionState.instance.userId
        uploadDataDetailModel.categoryId = productCategoryId
        uploadDataDetailModel.subcategoryId = productSubCategoryId
        uploadDataDetailModel.countryCode = countryCode
        uploadDataDetailModel.state = stateCode
        uploadDataDetailModel.city = cityId
        uploadDataDetailModel.description = upload_detail_enter_description_edit_text.text.toString()
        uploadDataDetailModel.location = upload_detail_enter_country_edit_text.text.toString() + " > " + upload_detail_enter_state_edit_text.text.toString() + " > " + upload_detail_enter_city_edit_text.text.toString()
        uploadDataDetailModel.hidePhone = if (upload_detail_hide_phone_switch.isChecked) AppConstants.YES else AppConstants.NO
        uploadDataDetailModel.negotiable = if (upload_detail_is_negotiable_switch.isChecked) AppConstants.YES else AppConstants.NO
        uploadDataDetailModel.price = upload_detail_enter_price_edit_text.text.toString()
        uploadDataDetailModel.phone = upload_detail_enter_phone_edit_text.text.toString()
        uploadDataDetailModel.latitude = SessionState.instance.uploadedProductLatitude
        uploadDataDetailModel.longitude = SessionState.instance.uploadedProductLongitude
        uploadDataDetailModel.itemScreen = selectedImagesToPost
        uploadDataDetailModel.title = upload_detail_enter_a_title_edit_text.text.toString()
        uploadDataDetailModel.additionalInfo = SessionState.instance.uploadedProductAdditionalInfo

        RetrofitController.postUserProduct(uploadDataDetailModel, object : Callback<PostedProductResponseModel>{
            override fun onFailure(call: Call<PostedProductResponseModel>?, t: Throwable?) {
                if (!this@UploadProductDetail.isFinishing) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                    if (upload_product_detail_button != null) upload_product_detail_button.isEnabled = true
                }
            }

            override fun onResponse(call: Call<PostedProductResponseModel>?, response: Response<PostedProductResponseModel>?) {
                if (!this@UploadProductDetail.isFinishing) {
                    if (response != null && response.isSuccessful && response.body() != null && AppConstants.SUCCESS.equals(response.body().status)) {
                        dismissProgressDialog()
                        Toast.makeText(this@UploadProductDetail, "Congratulations!! You have successfully posted", Toast.LENGTH_SHORT).show()
                        SessionState.instance.apply {
                            uploadedProductAdditionalInfo = ""
                            uploadedProductLatitude = ""
                            uploadedProductLongitude = ""
                        }
                        setResult(Activity.RESULT_OK)
                        finish()
                    } else {
                        showNetworkErrorSnackBar()
                        if (upload_product_detail_button != null) upload_product_detail_button.isEnabled = true
                    }
                }
            }
        })
    }

    private fun postImageDataAndGetPath() {
        showProgressDialog("Uploading...")
        var x = 0
        for (uri in selectedImageList) {
            val file = File(uri.path)
            if (file != null) {
                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                // MultipartBody.Part is used to send also the actual file name
                val body = MultipartBody.Part.createFormData("fileToUpload", file.getName(), requestFile)
                RetrofitController.updateUserPostedProductPic(body, object: Callback<UploadProductModel> {
                    override fun onFailure(call: Call<UploadProductModel>?, t: Throwable?) {
                        if (!this@UploadProductDetail.isFinishing) {
                            dismissProgressDialog()
                            Utility.showSnackBar(activity_upload_products_parent_layout, getString(R.string.internet_issue), this@UploadProductDetail)
                        }
                    }

                    override fun onResponse(call: Call<UploadProductModel>?, response: Response<UploadProductModel>?) {
                        if (!this@UploadProductDetail.isFinishing) {
                            if (response != null && response.isSuccessful) {
                                val responseBody = response.body()
                                if (responseBody != null && AppConstants.SUCCESS.equals(responseBody.status)) {
                                    if (selectedImagesToPost.isNullOrEmpty()) {
                                        selectedImagesToPost = responseBody.url!!
                                    } else {
                                        selectedImagesToPost += "," + responseBody.url!!
                                    }
                                }
                            } else {
                                upload_product_detail_button.isEnabled = true
                                //Utility.showSnackBar(activity_upload_products_parent_layout, getString(R.string.internet_issue), this@UploadProductDetail)
                            }

                            x += 1
                            if (x == selectedImageList.size) {
                                getFinalDataToPostAndUploadToServer()
                            }
                        }
                    }
                })
            } else {
               // dismissProgressDialog()
            }
        }

    }
}
