package com.bylancer.classified.bylancerclassified.uploadproduct.postdetails

import android.os.Bundle
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_location_selection.*

class LocationSelectionActivity: BylancerBuilderActivity(), View.OnClickListener{
    private var googleMap: GoogleMap? = null
    private var center: LatLng? = null
    private var latitudeStr = ""
    private var longitudeStr = ""
    private val PLACE_PICKER_REQUEST = 111
    private var mGoogleApiClient: GoogleApiClient? = null

    override fun setLayoutView() = R.layout.activity_location_selection

    override fun initialize(savedInstanceState: Bundle?) {
        location_selection_title.text = LanguagePack.getString(getString(R.string.select_location))
        /*mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()*/
        if (intent != null) {
            val bundle = intent.getBundleExtra(AppConstants.BUNDLE)
            if (bundle != null) {
                initializeMap(bundle!!.getDouble(AppConstants.SELECTED_PRODUCT_LATITUDE), bundle!!.getDouble(AppConstants.SELECTED_PRODUCT_LONGITUDE))
            } else {
                someThingWrong()
            }
        } else {
            someThingWrong()
        }
    }

    /*override fun onResume() {
        super.onResume()
        try {
            val builder = PlacePicker.IntentBuilder()
            startActivityForResult(builder.build(this@LocationSelectionActivity), PLACE_PICKER_REQUEST)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }
    }*/

    private fun initializeMap(latitude : Double, longitude : Double) {
        if (googleMap == null) {
            (supportFragmentManager.findFragmentById(R.id.location_selection_map) as com.google.android.gms.maps.SupportMapFragment).getMapAsync {
                this.googleMap = it
                if (googleMap != null) {
                    addMarker(latitude, longitude)
                }
            }
        }
    }

    private fun addMarker(latitude: Double, longitude: Double) {
        val cameraPosition = CameraPosition.Builder().target(
                LatLng(latitude, longitude)).zoom(15f).build()

        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        googleMap?.setOnCameraIdleListener(GoogleMap.OnCameraIdleListener {
            // TODO Auto-generated method stub
            center = googleMap?.getCameraPosition()?.target
            latitudeStr = center?.latitude.toString()
            longitudeStr = center?.longitude.toString()
            googleMap?.clear()
        })
    }

    fun someThingWrong() {
        Utility.showSnackBar(activity_location_selection_parent_layout, getString(R.string.some_wrong), this)
    }

    private fun sendResult() {
        SessionState.instance.uploadedProductLatitude = latitudeStr
        SessionState.instance.uploadedProductLongitude = longitudeStr
        onBackPressed()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.selection_done -> sendResult()
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST && data != null) {
            if (resultCode == Activity.RESULT_OK) {
                val place = PlacePicker.getPlace(data!!, this)
                val stBuilder = StringBuilder()
                val placename = String.format("%s", place.name)
                val latitude = place.latLng.latitude.toString()
                val longitude = place.latLng.longitude.toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        mGoogleApiClient?.disconnect()
        super.onStop()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }*/
}