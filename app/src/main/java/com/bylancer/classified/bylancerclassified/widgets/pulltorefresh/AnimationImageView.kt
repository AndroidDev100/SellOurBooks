/*
 * Copyright (C) 2015 RECRUIT LIFESTYLE CO., LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bylancer.classified.bylancerclassified.widgets.pulltorefresh

import android.content.Context
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView

/**
 * @author amyu
 */
internal open class AnimationImageView
/**
 * コンストラクタ
 * {@inheritDoc}
 */
(context: Context) : AppCompatImageView(context) {

    /**
     * AnimationのStartとEnd時にListenerにアレする
     */
    private var mListener: Animation.AnimationListener? = null

    /**
     * [AnimationImageView.mListener] のセット
     *
     * @param listener [Animation.AnimationListener]
     */
    fun setAnimationListener(listener: Animation.AnimationListener) {
        mListener = listener
    }

    /**
     * ViewのAnimationのStart時にセットされたListenerの [Animation.AnimationListener.onAnimationStart]
     * を呼ぶ
     */
    public override fun onAnimationStart() {
        super.onAnimationStart()
        if (mListener != null) {
            mListener!!.onAnimationStart(animation)
        }
    }

    /**
     * ViewのAnimationのEnd時にセットされたListenerの [Animation.AnimationListener.onAnimationEnd]
     * (Animation)} を呼ぶ
     */
    public override fun onAnimationEnd() {
        super.onAnimationEnd()
        if (mListener != null) {
            mListener!!.onAnimationEnd(animation)
        }
    }
}