package com.bylancer.classified.bylancerclassified.dashboard.locationselector

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.appconfig.AppConfigDetail
import com.bylancer.classified.bylancerclassified.fragments.BylancerBuilderFragment
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.SessionState
import com.bylancer.classified.bylancerclassified.utils.Utility
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.settings.CityListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.CountryListModel
import com.bylancer.classified.bylancerclassified.webservices.settings.StateListModel
import com.gmail.samehadar.iosdialog.IOSDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_location_selector.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Location Selection Fragment
 */
private const val ARG_PARAM = "param_position"

class LocationSelectorFragment : BylancerBuilderFragment(), LocationSelectedListener {
    private var mViewPagerPosition: Int? = null
    private var mProgressDialog: IOSDialog? = null
    private val countryList = arrayListOf<CountryListModel>()
    private val stateList = arrayListOf<StateListModel>()
    private val cityList = arrayListOf<CityListModel>()
    val filteredList = arrayListOf<String>()
    private lateinit var mViewPagerUpdateListener: ViewPagerUpdateListener

    companion object {
        /**
         *
         * @param position - view pager
         * @return A new instance of fragment LocationSelectorFragment.
         */
        @JvmStatic
        fun newInstance(position: Int) =
                LocationSelectorFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_PARAM, position)
                    }
                }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mViewPagerPosition = it.getInt(ARG_PARAM)
        }
    }

    override fun setLayoutView() = R.layout.fragment_location_selector

    override fun initialize(savedInstanceState: Bundle?) {
        mViewPagerUpdateListener = activity as ViewPagerUpdateListener
        refresh(AppConstants.COUNTRY_PAGER_POSITION)
        initializeListener()
        initializeRecyclerView()
        initializeSearchListener()
    }

    private fun initializeSearchListener() {
        location_selector_search_bar_edit_text?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                when (mViewPagerPosition) {
                    0 -> {
                        if (!countryList.isNullOrEmpty()) {
                            filteredList.clear()
                            countryList?.forEach {
                                if (it.name != null && it.name!!.contains(text.toString(), true)) {
                                    filteredList.add(it.name!!)
                                }
                            }
                            setLocationAdapter()
                        }
                    }
                    1 -> {
                        if (!stateList.isNullOrEmpty()) {
                            filteredList.clear()
                            stateList?.forEach {
                                if (it.name != null && it.name!!.contains(text.toString(), true)) {
                                    filteredList.add(it.name!!)
                                }
                            }
                            setLocationAdapter()
                        }
                    }
                    else -> {
                        if (!cityList.isNullOrEmpty()) {
                            filteredList.clear()
                            cityList?.forEach {
                                if (it.name != null && it.name!!.contains(text.toString(), true)) {
                                    filteredList.add(it.name!!)
                                }
                            }
                            setLocationAdapter(true)
                        }
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })
    }

    private fun initializeRecyclerView() {
        if (mContext != null) {
            location_list_recycler_view.layoutManager = LinearLayoutManager(mContext)
            location_list_recycler_view.setHasFixedSize(true)
            location_list_recycler_view.itemAnimator = DefaultItemAnimator()
        }
    }

    private fun initializeListener() {
        location_selector_search_bar_edit_text?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                search_text_delete_imageView?.visibility = if (text.isNullOrEmpty()) View.INVISIBLE else View.VISIBLE
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })

        search_text_delete_imageView.setOnClickListener() {
            location_selector_search_bar_edit_text?.setText("")
        }
    }

    private fun fetchCountryList() {
        showProgressDialog(getString(R.string.fetch_country))
        RetrofitController.fetchCountryDetails(object: Callback<List<CountryListModel>> {
            override fun onResponse(call: Call<List<CountryListModel>>?, response: Response<List<CountryListModel>>?) {
                if (isAdded && isVisible) {
                    if (response != null && response.isSuccessful) {
                        countryList.addAll(response.body())
                        saveCountryDetailData()
                        setCountryDataToAdapter()
                    }
                    dismissProgressDialog()
                }
            }

            override fun onFailure(call: Call<List<CountryListModel>>?, t: Throwable?) {
                if (isAdded && isVisible) {
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
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful) {
                        stateList.addAll(response.body())
                        saveStateDetailData()
                        setStateDataToAdapter()
                    }
                }
            }

            override fun onFailure(call: Call<List<StateListModel>>?, t: Throwable?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun fetchCityList(stateId: String) {
        showProgressDialog(getString(R.string.fetch_city))
        RetrofitController.fetchCityDetails(stateId, object: Callback<List<CityListModel>> {
            override fun onResponse(call: Call<List<CityListModel>>?, response: Response<List<CityListModel>>?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    if (response != null && response.isSuccessful) {
                        cityList.addAll(response.body())
                        saveCityDetailData()
                        setCityDataToAdapter()
                    }
                }
            }

            override fun onFailure(call: Call<List<CityListModel>>?, t: Throwable?) {
                if (isAdded && isVisible) {
                    dismissProgressDialog()
                    showNetworkErrorSnackBar()
                }
            }
        })
    }

    private fun saveCountryDetailData() {
        if (isAdded && isVisible && mContext != null && !countryList.isNullOrEmpty()) {
            AppConfigDetail.countryList = countryList
            AppConfigDetail.saveCountryListData(mContext!!, Gson().toJson(countryList))
        }
    }

    private fun saveStateDetailData() {
        if (isAdded && isVisible && mContext != null) {
            if (!stateList.isNullOrEmpty()) {
                AppConfigDetail.stateList = stateList
                AppConfigDetail.saveSateListData(mContext!!, Gson().toJson(stateList))
            } else {
                AppConfigDetail.stateList = null
                AppConfigDetail.saveSateListData(mContext!!, "")
            }
        }
    }

    private fun saveCityDetailData() {
        if (isAdded && isVisible && mContext != null) {
            if (!cityList.isNullOrEmpty()) {
                AppConfigDetail.cityList = cityList
                AppConfigDetail.saveCityListData(mContext!!, Gson().toJson(cityList))
            } else {
                AppConfigDetail.cityList = null
                AppConfigDetail.saveCityListData(mContext!!, "")
            }
        }
    }


    private fun showProgressDialog(message: String) {
        if (mContext != null) {
            mProgressDialog = Utility.showProgressView(mContext!!, message)
            mProgressDialog?.show()
        }
    }

    private fun dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }

    private fun showNetworkErrorSnackBar() {
        if (location_selector_fragment_parent_view != null && mContext != null) Utility.showSnackBar(location_selector_fragment_parent_view, "Please check your internet connection", mContext!!)
    }

    private fun setCountryDataToAdapter() {
        filteredList.clear()
        countryList?.forEach {
            if (null != it?.name) {
                filteredList.add(it.name!!)
            }
        }
        setLocationAdapter()
    }

    private fun setStateDataToAdapter() {
        filteredList.clear()
        stateList?.forEach {
            if (null != it?.name) {
                filteredList.add(it.name!!)
            }
        }
        setLocationAdapter()
    }

    private fun setCityDataToAdapter() {
        filteredList.clear()
        cityList?.forEach {
            if (null != it?.name) {
                filteredList.add(it.name!!)
            }
        }
        setLocationAdapter(true)
    }

    private fun setLocationAdapter(isCity: Boolean = false) {
        if (location_list_recycler_view?.adapter != null) {
            location_list_recycler_view?.adapter!!.notifyDataSetChanged()
        } else {
            location_list_recycler_view?.adapter = LocationSelectorAdapter(filteredList, this, isCity)
        }
    }


    override fun onLocationSelected(filteredPosition: Int) {
        var position = 0
        if (!filteredList.isNullOrEmpty()) {
            val name = filteredList[filteredPosition]
            when (mViewPagerPosition) {
                0 -> {
                    if (!countryList.isNullOrEmpty()) {
                        for (i in 0 until countryList.size) {
                            if (name != null && name.equals(countryList[i].name)) {
                                position = i
                                break
                            }
                        }
                    }
                }
                1 -> {
                    if (!stateList.isNullOrEmpty()) {
                        for (i in 0 until stateList.size) {
                            if (name != null && name.equals(stateList[i].name)) {
                                position = i
                                break
                            }
                        }
                    }
                }
                else -> {
                    if (!cityList.isNullOrEmpty()) {
                        for (i in 0 until cityList.size) {
                            if (name != null && name.equals(cityList[i].name)) {
                                position = i
                                break
                            }
                        }
                    }
                }

            }
        }
        when (mViewPagerPosition) {
            0 -> {
                if (countryList[position] != null) {
                    SessionState.instance.selectedCountry = countryList.get(position).name!!
                    SessionState.instance.selectedCountryCode = countryList.get(position).lowercaseCode!!
                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY.toString(),
                            countryList[position].name!!)
                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_COUNTRY_CODE.toString(),
                            countryList[position].lowercaseCode!!)
                    SessionState.instance.selectedState = ""
                    SessionState.instance.selectedCity = ""
                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                            "")
                    SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                            "")
                    stateList?.clear()
                    saveStateDetailData()
                    AppConfigDetail.stateList = null
                    refresh(AppConstants.STATE_PAGER_POSITION)
                }
            }
            1 -> {
                SessionState.instance.selectedState = if (stateList[position] != null) stateList[position].name!! else ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE.toString(),
                        if (stateList[position] != null) stateList[position].name!! else "")
                SessionState.instance.selectedStateCode = if (stateList[position] != null) stateList[position].code!! else ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_STATE_CODE.toString(),
                        if (stateList[position] != null) stateList[position].code!! else "")
                SessionState.instance.selectedCity = ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                        "")
                saveCityDetailData()
                AppConfigDetail.cityList = null
                refresh(AppConstants.CITY_PAGER_POSITION)
            }
            else -> {
                SessionState.instance.selectedCity = if (cityList[position] != null) cityList[position].name!! else ""
                SessionState.instance.saveValuesToPreferences(context!!, AppConstants.Companion.PREFERENCES.SELECTED_CITY.toString(),
                        if (cityList[position] != null) cityList[position].name!! else "")
                updateViewPagerPosition(-1)
            }
        }
    }

    private fun updateViewPagerPosition(position: Int) {
        if (mViewPagerUpdateListener != null) {
            mViewPagerUpdateListener.updateViewPagerPosition(position)
        }
    }

    private fun refresh(position: Int) {
        when (position) {
            0 -> {
                mViewPagerPosition = 0
                location_selector_search_bar_edit_text?.hint = LanguagePack.getString(getString(R.string.country))
                if (AppConfigDetail.countryList.isNullOrEmpty()) {
                    fetchCountryList()
                } else {
                    countryList.clear()
                    countryList.addAll(AppConfigDetail.countryList!!)
                    setCountryDataToAdapter()
                }
            }
            1 -> {
                mViewPagerPosition = 1
                location_selector_search_bar_edit_text?.hint = LanguagePack.getString(getString(R.string.state))
                if (AppConfigDetail.stateList.isNullOrEmpty()) {
                    fetchStateList(SessionState.instance.selectedCountryCode)
                } else {
                    stateList.clear()
                    stateList.addAll(AppConfigDetail.stateList!!)
                    setStateDataToAdapter()
                }
            }
            else -> {
                mViewPagerPosition = 2
                location_selector_search_bar_edit_text?.hint = LanguagePack.getString(getString(R.string.city))
                if (AppConfigDetail.cityList.isNullOrEmpty()) {
                    fetchCityList(SessionState.instance.selectedStateCode)
                } else {
                    cityList.clear()
                    cityList.addAll(AppConfigDetail.cityList!!)
                    setCityDataToAdapter()
                }
            }
        }
    }
}
