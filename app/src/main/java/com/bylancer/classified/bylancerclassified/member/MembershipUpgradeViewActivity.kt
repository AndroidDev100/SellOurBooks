package com.bylancer.classified.bylancerclassified.member

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.dashboard.OnProductItemClickListener
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.showToast
import com.bylancer.classified.bylancerclassified.webservices.RetrofitController
import com.bylancer.classified.bylancerclassified.webservices.membership.CurrentUserMembershipPlan
import com.bylancer.classified.bylancerclassified.webservices.membership.MembershipPlan
import com.bylancer.classified.bylancerclassified.webservices.membership.MembershipPlanList
import kotlinx.android.synthetic.main.activity_membership_upgrade.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembershipUpgradeViewActivity : BylancerBuilderActivity(), View.OnClickListener,
    Callback<MembershipPlanList> {

    override fun setLayoutView() = R.layout.activity_membership_upgrade

    override fun initialize(savedInstanceState: Bundle?) {
        membership_title_text_view.text = LanguagePack.getString(getString(R.string.membership))
        membership_recycler_view.layoutManager = LinearLayoutManager(this)
        membership_recycler_view.setHasFixedSize(true)

        showProgressDialog(LanguagePack.getString(getString(R.string.loading)))
        RetrofitController.fetchMembershipPlan(this)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.close_membership_image_view -> onBackPressed()
        }
    }

    override fun onFailure(call: Call<MembershipPlanList>?, t: Throwable?) {
        dismissProgressDialog()
        showToast(getString(R.string.some_wrong))
    }

    override fun onResponse(call: Call<MembershipPlanList>?, response: Response<MembershipPlanList>?) {
        if (response != null && response.isSuccessful && !response.body()?.plans.isNullOrEmpty()) {
            populateUIGetUserPlan(response.body()!!.plans!!)
        } else {
            dismissProgressDialog()
            showToast(getString(R.string.some_wrong))
        }
    }

    private fun populateUIGetUserPlan(membershipPlanListPlan: ArrayList<MembershipPlan>) {
        RetrofitController.fetchCurrentUserMembershipPlan(object : Callback<CurrentUserMembershipPlan>{
            override fun onFailure(call: Call<CurrentUserMembershipPlan>?, t: Throwable?) {
                dismissProgressDialog()
                showToast(getString(R.string.some_wrong))
                populateUI(membershipPlanListPlan, null)
            }

            override fun onResponse(call: Call<CurrentUserMembershipPlan>?, response: Response<CurrentUserMembershipPlan>?) {
                dismissProgressDialog()
                if (response != null && response.isSuccessful && response.body() != null) {
                    populateUI(membershipPlanListPlan, response.body())
                } else {
                    showToast(getString(R.string.some_wrong))
                }
            }

        })
    }

    private fun populateUI(membershipPlanList: ArrayList<MembershipPlan>, userPlan: CurrentUserMembershipPlan?) {
        if (AppConstants.PRODUCT_STATUS.equals(userPlan?.status, true) &&
                AppConstants.SUCCESS.equals(userPlan?.message, true)) {
            for (memberPlan in membershipPlanList) {
                memberPlan.apply {
                    memberPlan.title?.let {
                        if (it.equals(userPlan?.planTitle, true)) {
                            memberPlan.selected = AppConstants.SELECTED
                            memberPlan.duration = userPlan?.expiryDate
                        }
                    }
                }
            }
        }

        membership_recycler_view.adapter = MemberShipItemAdapter(membershipPlanList,
                object : OnProductItemClickListener {
                    override fun onProductItemClicked(productId: String?, cost: String?, title: String?) {
                        if (productId != null && cost != null) {
                            getTransactionVendorCredentials(title
                                    ?: getString(R.string.app_name), cost, AppConstants.GO_FOR_PREMIUM_APP, productId, arrayOf(""))
                        }
                    }

                })

    }

    override fun onProductBecamePremium(productId: String) {
        showToast(getString(R.string.success_upgraded))
        finish()
    }
}

