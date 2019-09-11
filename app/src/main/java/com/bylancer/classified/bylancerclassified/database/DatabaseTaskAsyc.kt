package com.bylancer.classified.bylancerclassified.database

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.widget.AppCompatImageView
import android.view.View
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.dashboard.DashboardDetailModel
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.Utility

class DatabaseTaskAsyc(val context: Context, val mDashboardDetailModel: DashboardDetailModel,
                       val favImageView: AppCompatImageView, val parentView: View,
                       val isForValidating: Boolean): AsyncTask<Void, Void, Boolean>() {

    override fun doInBackground(vararg p0: Void): Boolean {
        if (context != null && mDashboardDetailModel != null) {
            if (mDashboardDetailModel?.productId != null && DBUtil.getDatabaseInstance(context)?.daoAccess()?.
                            checkPropertyExist(mDashboardDetailModel?.productId!!) > 0) {
                if (!isForValidating) {
                    DBUtil.getDatabaseInstance(context).daoAccess().
                            deleteProperty(mDashboardDetailModel!!)
                    return false
                } else {
                    return true
                }
            } else {
                if(isForValidating) {
                    return false
                }
                DBUtil.getDatabaseInstance(context)?.daoAccess()?.
                        insertProperty(mDashboardDetailModel!!)
                return true
            }
        }

        return false
    }

    override fun onPostExecute(result: Boolean) {
        super.onPostExecute(result)
        if (favImageView != null) {
            if (result) {
                favImageView.setImageResource(R.drawable.ic_favorite_selected)
                if (parentView != null && context != null && !isForValidating) Utility.showSnackBar(parentView, LanguagePack.getString("Added to your favorite list"), context)
            } else {
                favImageView.setImageResource(R.drawable.ic_favorite_small)
                if (parentView != null && context != null && !isForValidating) Utility.showSnackBar(parentView, LanguagePack.getString("Removed from favorite list"), context)
            }
        }
    }

}