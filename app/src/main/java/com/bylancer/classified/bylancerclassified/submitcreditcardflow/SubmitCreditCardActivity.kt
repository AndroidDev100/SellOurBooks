package com.bylancer.classified.bylancerclassified.submitcreditcardflow

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.databinding.ActivitySubmitCreditCardBinding
import com.bylancer.classified.bylancerclassified.utils.AppConstants
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import com.bylancer.classified.bylancerclassified.utils.showToast
import kotlinx.android.synthetic.main.activity_submit_credit_card.*

class SubmitCreditCardActivity : AppCompatActivity() {
    private var showingGray = true
    private var inSet: AnimatorSet? = null
    private var outSet: AnimatorSet? = null
    private var activitySubmitCreditCardBinding: ActivitySubmitCreditCardBinding? = null
    private var card: PayStackCard? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activitySubmitCreditCardBinding = DataBindingUtil.setContentView(this, R.layout.activity_submit_credit_card)
        card = PayStackCard("","","","")

        setSupportActionBar(activitySubmitCreditCardBinding?.toolbar)

        val onHelpClickListener = View.OnClickListener { Toast.makeText(this@SubmitCreditCardActivity, "The CVV Number (\"Card Verification Value\") is a 3 or 4 digit number on your credit and debit cards", Toast.LENGTH_LONG).show() }

        activitySubmitCreditCardBinding?.iconHelpGray?.setOnClickListener(onHelpClickListener)
        activitySubmitCreditCardBinding?.iconHelpBlue?.setOnClickListener(onHelpClickListener)

        activitySubmitCreditCardBinding?.inputEditCardNumber?.addTextChangedListener(object : TextWatcher {

            private var lock: Boolean = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty()) {
                    flipToBlue()
                }
            }

            override fun afterTextChanged(s: Editable) {
                if (lock || s.length > 18) {
                    if (s.length > 18) {
                        activitySubmitCreditCardBinding?.viewPager?.currentItem = 1
                    }
                    return
                }
                lock = true
                var i = 4
                while (i < s.length) {
                    if (s.toString()[i] != ' ') {
                        s.insert(i, " ")
                    }
                    i += 5
                }
                lock = false
            }
        })

        activitySubmitCreditCardBinding?.inputEditExpiredDate?.addTextChangedListener(object : TextWatcher {

            private var lock: Boolean = false

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (lock || s.length > 4) {
                    if (s.length > 4) {
                        activitySubmitCreditCardBinding?.viewPager?.currentItem = 2
                    }
                    return
                }
                lock = true
                if (s.length > 2 && s.toString()[2] != '/') {
                    s.insert(2, "/")
                }
                lock = false
            }
        })

        activitySubmitCreditCardBinding?.inputEditCvvCode?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.length > 3) {
                    activitySubmitCreditCardBinding?.viewPager?.currentItem = 3
                    return
                }
            }
        })

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        val height = size.y

        val adapter = MyPagerAdapter()
        activitySubmitCreditCardBinding?.viewPager?.adapter = adapter
        activitySubmitCreditCardBinding?.viewPager?.clipToPadding = false
        activitySubmitCreditCardBinding?.viewPager?.setPadding(width / 4, 0, width / 4, 0)
        activitySubmitCreditCardBinding?.viewPager?.pageMargin = width / 14
        activitySubmitCreditCardBinding?.viewPager?.setPagingEnabled(false)
        activitySubmitCreditCardBinding?.viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        updateProgressBar(25)
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.setTextColor(resources.getColor(R.color.shadow_black))
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.isFocusableInTouchMode = true
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.requestFocus()
                        return
                    }
                    1 -> {
                        updateProgressBar(50)
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setTextColor(resources.getColor(R.color.shadow_black))
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.isFocusableInTouchMode = true
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.requestFocus()
                        return
                    }
                    2 -> {
                        updateProgressBar(75)
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.setTextColor(resources.getColor(R.color.shadow_black))
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.isFocusableInTouchMode = true
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.requestFocus()
                        return
                    }
                    3 -> {
                        updateProgressBar(100)
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.setTextColor(resources.getColor(R.color.shadow_black))
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.isFocusableInTouchMode = true
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.requestFocus()
                        return
                    }
                    4 -> {
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.setTextColor(resources.getColor(R.color.white_color_text))
                        activitySubmitCreditCardBinding?.inputEditCardNumber?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditExpiredDate?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCardHolder?.isFocusable = false
                        activitySubmitCreditCardBinding?.inputEditCvvCode?.isFocusable = false
                        return
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

        val onEditorActionListener = TextView.OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                activitySubmitCreditCardBinding?.viewPager?.currentItem = activitySubmitCreditCardBinding?.viewPager?.currentItem!! + 1
                handled = true
            }
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
                handled = true
            }
            handled
        }

        activitySubmitCreditCardBinding?.inputEditCardNumber?.setOnEditorActionListener(onEditorActionListener)
        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setOnEditorActionListener(onEditorActionListener)
        activitySubmitCreditCardBinding?.inputEditCardHolder?.setOnEditorActionListener(onEditorActionListener)
        activitySubmitCreditCardBinding?.inputEditCvvCode?.setOnEditorActionListener(onEditorActionListener)

        activitySubmitCreditCardBinding?.inputEditCardNumber?.requestFocus()

        inSet = AnimatorInflater.loadAnimator(this, R.animator.card_flip_in) as AnimatorSet
        outSet = AnimatorInflater.loadAnimator(this, R.animator.card_flip_out) as AnimatorSet

        label_secure_submission?.setOnClickListener {
            sendDataToProcessPayment()
        }
    }

    private inner class MyPagerAdapter : PagerAdapter() {

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var resId = 0
            when (position) {
                0 -> resId = R.id.input_layout_card_number
                1 -> resId = R.id.input_layout_expired_date
                2 -> resId = R.id.input_layout_cvv_code
                3 -> resId = R.id.input_layout_card_holder
                4 -> resId = R.id.space
            }
            return findViewById(resId)
        }


        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {}


        override fun getCount(): Int {
            return 5
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, 0)
    }

    private fun updateProgressBar(progress: Int) {
        val animation = ObjectAnimator.ofInt(activitySubmitCreditCardBinding?.progressHorizontal, "progress", progress)
        animation.duration = 300
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }

    private fun submit() {
        activitySubmitCreditCardBinding?.viewPager?.currentItem = 4
        card?.cardNumber = activitySubmitCreditCardBinding?.inputEditCardNumber?.text.toString()
        card?.expiredDate = activitySubmitCreditCardBinding?.inputEditExpiredDate?.text.toString()
        card?.cardHolder = activitySubmitCreditCardBinding?.inputEditCardHolder?.text.toString()
        card?.cvvCode = activitySubmitCreditCardBinding?.inputEditCvvCode?.text.toString()

        Handler().postDelayed({
            activitySubmitCreditCardBinding?.inputEditCardHolder?.visibility = View.INVISIBLE
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
            activitySubmitCreditCardBinding?.labelSecureSubmission?.visibility = View.VISIBLE
            hideKeyboard(activitySubmitCreditCardBinding?.inputEditCardHolder!!)
            activitySubmitCreditCardBinding?.progressCircle?.visibility = View.VISIBLE
        }, 300)
    }

    private fun sendDataToProcessPayment() {
        if (card != null && !card?.expiredDate.isNullOrEmpty() && !card?.cardHolder.isNullOrEmpty() &&
                !card?.cardNumber.isNullOrEmpty() && !card?.cvvCode.isNullOrEmpty()) {
            val cardIntent = Intent()
            cardIntent.putExtra(AppConstants.PAY_STACK_CARD_DETAILS, card)
            setResult(Activity.RESULT_OK, cardIntent)
            finish()
        } else {
            showToast(LanguagePack.getString(getString(R.string.enter_card_details_err)))
        }
    }

    private fun reset() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        activitySubmitCreditCardBinding?.inputLayoutCvvCode?.visibility = View.VISIBLE
        activitySubmitCreditCardBinding?.progressCircle?.visibility = View.GONE
        activitySubmitCreditCardBinding?.labelSecureSubmission?.visibility = View.GONE
        flipToGray()
        activitySubmitCreditCardBinding?.viewPager?.currentItem = 0
        activitySubmitCreditCardBinding?.inputEditCardNumber?.setText("")
        activitySubmitCreditCardBinding?.inputEditExpiredDate?.setText("")
        activitySubmitCreditCardBinding?.inputEditCardHolder?.setText("")
        activitySubmitCreditCardBinding?.inputEditCvvCode?.setText("")
        activitySubmitCreditCardBinding?.inputEditCardNumber?.requestFocus()
        showKeyboard(activitySubmitCreditCardBinding?.inputEditCardNumber!!)
    }

    private fun flipToGray() {
        if (!showingGray && !outSet!!.isRunning && !inSet!!.isRunning) {
            showingGray = true

            activitySubmitCreditCardBinding?.cardBlue?.cardElevation = 0f
            activitySubmitCreditCardBinding?.cardGray?.cardElevation = 0f

            outSet?.setTarget(activitySubmitCreditCardBinding?.cardBlue)
            outSet?.start()

            inSet?.setTarget(activitySubmitCreditCardBinding?.cardGray)
            inSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    activitySubmitCreditCardBinding?.cardGray?.cardElevation = convertDpToPixel(12f, this@SubmitCreditCardActivity)
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            inSet?.start()
        }
    }

    private fun flipToBlue() {
        if (showingGray && !outSet?.isRunning!! && !inSet!!.isRunning) {
            showingGray = false

            activitySubmitCreditCardBinding?.cardGray?.cardElevation = 0f
            activitySubmitCreditCardBinding?.cardBlue?.cardElevation = 0f

            outSet?.setTarget(activitySubmitCreditCardBinding?.cardGray)
            outSet?.start()

            inSet?.setTarget(activitySubmitCreditCardBinding?.cardBlue)
            inSet?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    activitySubmitCreditCardBinding?.cardBlue?.cardElevation = convertDpToPixel(12f, this@SubmitCreditCardActivity)
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            inSet?.start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.credit_card_entry_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_reset -> {
                reset()
                return true
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        fun convertDpToPixel(dp: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }

        fun convertPixelsToDp(px: Float, context: Context): Float {
            val resources = context.resources
            val metrics = resources.displayMetrics
            return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
        }
    }

}
