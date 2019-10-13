package com.bylancer.classified.bylancerclassified.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants

/**
 * Created by Ani on 3/20/18.
 */
abstract class BylancerBuilderFragment : Fragment() {
    var mContext : Context? = null
    var mActivity : BylancerBuilderActivity? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(setLayoutView(), container, false)
    }

    protected abstract fun setLayoutView(): Int

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mContext = activity
        if (activity is BylancerBuilderActivity) {
            mActivity = activity as BylancerBuilderActivity
        }

        initialize(savedInstanceState)
    }

    protected abstract fun initialize(savedInstanceState: Bundle?)

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean) {
        val intent = Intent(context, clazz)
        if(isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun startActivity(clazz: Class<out Activity>, extra:Bundle) {
        val intent = Intent(context, clazz)
        intent.putExtra(AppConstants.BUNDLE, extra)
        startActivity(intent)
    }

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean, bundle: Bundle) {
        val intent = Intent(context, clazz)
        intent.putExtra(AppConstants.BUNDLE, bundle)
        if(isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }
}