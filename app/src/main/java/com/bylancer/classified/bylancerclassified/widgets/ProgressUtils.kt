package com.bylancer.classified.bylancerclassified.widgets

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.Window
import com.bylancer.classified.bylancerclassified.R

class ProgressUtils {
    companion object {
        private var progressDialog: Dialog? = null

        fun showLoadingDialog(context: Context) {
            if (!(progressDialog != null && progressDialog!!.isShowing)) {
                progressDialog = Dialog(context)
                progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
                progressDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
                progressDialog!!.setContentView(R.layout.progress_dialog)
                progressDialog!!.setCancelable(false)
                progressDialog!!.show()
            }
        }

        fun cancelLoading() {
            if (progressDialog != null && progressDialog!!.isShowing)
                progressDialog!!.cancel()
        }
    }
}
