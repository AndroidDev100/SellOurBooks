package com.bylancer.classified.bylancerclassified.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.bylancer.classified.bylancerclassified.chat.ChatActivity
import com.bylancer.classified.bylancerclassified.login.LoginRequiredActivity
import com.bylancer.classified.bylancerclassified.login.LoginActivity
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.Utility

/**
 * Created by Ani on 3/20/18.
 */
abstract class BylancerBuilderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(this::class.simpleName != LoginRequiredActivity::class.simpleName &&
                this::class.simpleName != LoginActivity::class.simpleName &&
                this::class.simpleName != ChatActivity::class.simpleName) {
            Utility.slideActivityRightToLeft(this)
        } else {
            Utility.slideActivityBottomToTop(this)
        }
        setContentView(setLayoutView())

        setOrientation() // Fixing Android O issue

        initialize(savedInstanceState)
    }

    protected abstract fun setLayoutView(): Int

    protected abstract fun initialize(savedInstanceState: Bundle?)

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean) {
        val intent = Intent(this, clazz)
        if(isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun setOrientation() {
       // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    fun startActivity(clazz: Class<out Activity>, isNewTask:Boolean, bundle: Bundle) {
        val intent = Intent(this, clazz)
        intent.putExtra(AppConstants.BUNDLE, bundle)
        if(isNewTask) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    fun startActivity(clazz: Class<out Activity>, extra:Bundle, parcelName:String) {
        val intent = Intent(this, clazz)
        intent.putExtra(AppConstants.BUNDLE, extra)
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(this::class.simpleName != LoginRequiredActivity::class.simpleName &&
                this::class.simpleName != LoginActivity::class.simpleName &&
                this::class.simpleName != ChatActivity::class.simpleName) {
            Utility.slideActivityLeftToRight(this)
        } else {
            Utility.slideActivityTopToBottom(this)
        }
        finish()
    }
}