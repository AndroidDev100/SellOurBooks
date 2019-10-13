package com.bylancer.classified.bylancerclassified.widgets

/*
 * Copyright 2014 Pierre Degand
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView

import androidx.appcompat.widget.AppCompatTextView

/**
 * @author Pierre Degand
 */
class ShinnyTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : AppCompatTextView(context, attrs, defStyleAttr) {

    private val mGradientDiameter = 0.3f

    private var mAnimator: ValueAnimator? = null
    private var mGradientCenter: Float = 0.toFloat()
    private var mShineDrawable: PaintDrawable? = null

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!isInEditMode) {
            if (mAnimator != null) {
                mAnimator!!.cancel()
            }
            mShineDrawable = PaintDrawable()
            mShineDrawable!!.setBounds(0, 0, w, h)
            mShineDrawable!!.paint.shader = generateGradientShader(width, 0.0f, 0.0f, 0.0f)
            mShineDrawable!!.paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

            mAnimator = ValueAnimator.ofFloat(0.0f, 1.0f)
            mAnimator!!.duration = (5 * w).toLong() // custom
            mAnimator!!.repeatCount = ValueAnimator.INFINITE
            mAnimator!!.repeatMode = ValueAnimator.RESTART
            mAnimator!!.interpolator = LinearInterpolator() // Custom
            mAnimator!!.addUpdateListener { animation ->
                val value = animation.animatedFraction
                mGradientCenter = (1 + 2 * mGradientDiameter) * value - mGradientDiameter
                val gradientStart = mGradientCenter - mGradientDiameter
                val gradientEnd = mGradientCenter + mGradientDiameter
                val shader = generateGradientShader(w, gradientStart, mGradientCenter, gradientEnd)
                mShineDrawable!!.paint.shader = shader
                invalidate()
            }
            mAnimator!!.start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode && mShineDrawable != null) {
            mShineDrawable!!.draw(canvas)
        }
    }

    private fun generateGradientShader(width: Int, vararg positions: Float): Shader {
        val colorRepartition = intArrayOf(Color.WHITE, Color.GRAY, Color.WHITE)
        return LinearGradient(
                0f,
                0f,
                width.toFloat(),
                0f,
                colorRepartition,
                positions,
                Shader.TileMode.REPEAT
        )
    }
}
