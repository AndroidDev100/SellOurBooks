package com.bylancer.classified.bylancerclassified.dashboard.locationselector

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bylancer.classified.bylancerclassified.R
import com.bylancer.classified.bylancerclassified.activities.BylancerBuilderActivity
import com.bylancer.classified.bylancerclassified.fragments.BylancerBuilderFragment
import com.bylancer.classified.bylancerclassified.utils.LanguagePack
import kotlinx.android.synthetic.main.activity_location_selector.*

class LocationSelectorActivity : BylancerBuilderActivity(), ViewPagerUpdateListener {

    override fun setLayoutView() = R.layout.activity_location_selector

    override fun initialize(savedInstanceState: Bundle?) {
        location_selector_title_text_view.text = LanguagePack.getString(getString(R.string.location_selector))

        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        location_selector_view_pager?.adapter = pagerAdapter
        /*location_selector_view_pager?.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                (pagerAdapter?.getItem(position) as LocationSelectorFragment).refresh(position)
            }

        })*/

        location_selector_back_image_view.setOnClickListener() {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        when {
            location_selector_view_pager?.currentItem == 0 -> {
                super.onBackPressed()
                finish()
            }
            location_selector_view_pager != null -> location_selector_view_pager?.currentItem = location_selector_view_pager.currentItem - 1
            else -> finish()
        }
    }

    override fun updateViewPagerPosition(position: Int) {
        if (!this.isFinishing) {
            if (position > 0) {
                //location_selector_view_pager?.currentItem = position
                location_selector_back_image_view?.setImageResource(R.drawable.ic_ok)
            } else {
                finish()
            }
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = 1 // Country, State, City

        override fun getItem(position: Int): BylancerBuilderFragment = LocationSelectorFragment.newInstance(position)

        private fun getCurrentPosition(): Int {
            return if (location_selector_view_pager != null) {
                location_selector_view_pager.currentItem
            } else {
                0
            }
        }
    }
}
