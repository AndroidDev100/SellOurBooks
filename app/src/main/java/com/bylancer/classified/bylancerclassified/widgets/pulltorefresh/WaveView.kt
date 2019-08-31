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

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import androidx.core.view.ViewCompat

/**
 * @author amyu
 *
 * 波と落ちる円を描画するView
 */
class WaveView
/**
 * Constructor
 * {@inheritDoc}
 */
(context: Context) : View(context), ViewTreeObserver.OnPreDrawListener {

    /**
     * 円のRadius
     */
    private var mDropCircleRadius = 100f

    /**
     * すべてを描画するPaint
     */
    private var mPaint: Paint? = null

    /**
     * 画面の波を描画するためのPath
     */
    private var mWavePath: Path? = null

    /**
     * 落ちる円の接線を描画するためのPath
     */
    private var mDropTangentPath: Path? = null

    /**
     * 落ちる円を描画するためのPath
     */
    private var mDropCirclePath: Path? = null

    /**
     * 影のPaint
     */
    private var mShadowPaint: Paint? = null

    /**
     * 影のPath
     */
    private var mShadowPath: Path? = null

    /**
     * 落ちる円の座標を入れているRectF
     */
    private var mDropRect: RectF? = null

    /**
     * Viewの横幅
     */
    private var mWidth: Int = 0

    /**
     * [WaveView.mDropCircleAnimator] でアニメーションしてる時の円の中心のY座標
     */
    var currentCircleCenterY: Float = 0.toFloat()
        private set

    /**
     * 円が落ちる最大の高さ
     */
    private var mMaxDropHeight: Float = 0F

    private var mIsManualRefreshing = false

    /**
     * 落ちる円の高さが更新されたかどうか
     */
    private var mDropHeightUpdated = false

    /**
     * [WaveView.mMaxDropHeight] を更新するための一時的な値の置き場
     */
    private var mUpdateMaxDropHeight: Int = 0

    /**
     * 落ちてくる円についてくる三角形の一番上の頂点のAnimator
     */
    private var mDropVertexAnimator: ValueAnimator? = null

    /**
     * 落ちた円が横に伸びるときのAnimator
     */
    private var mDropBounceVerticalAnimator: ValueAnimator? = null

    /**
     * 落ちた縁が縦に伸びるときのAnimator
     */
    private var mDropBounceHorizontalAnimator: ValueAnimator? = null

    /**
     * 落ちる円の中心座標のAnimator
     */
    private var mDropCircleAnimator: ValueAnimator? = null

    /**
     * 落ちた円を消すためのAnimator
     */
    private var mDisappearCircleAnimator: ValueAnimator? = null

    /**
     * 帰ってくる波ののAnimator
     */
    private var mWaveReverseAnimator: ValueAnimator? = null

    /**
     * 各AnimatorのAnimatorUpdateListener
     */
    private val mAnimatorUpdateListener = ValueAnimator.AnimatorUpdateListener { postInvalidate() }

    val isDisappearCircleAnimatorRunning: Boolean
        get() = mDisappearCircleAnimator!!.isRunning

    init {
        viewTreeObserver.addOnPreDrawListener(this)
        initView()
    }

    /**
     * Viewのサイズが決まったら [WaveView.mWidth] に横幅
     * {@inheritDoc}
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        mWidth = w
        mDropCircleRadius = w / 14.4f
        updateMaxDropHeight(Math.min(Math.min(w, h).toFloat(), height - mDropCircleRadius).toInt())
        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * 描画されてから [WaveView.mMaxDropHeight] を更新する
     * {@inheritDoc}
     */
    override fun onPreDraw(): Boolean {
        viewTreeObserver.removeOnPreDrawListener(this)
        if (mDropHeightUpdated) {
            updateMaxDropHeight(mUpdateMaxDropHeight)
        }
        return false
    }

    override fun onDraw(canvas: Canvas) {
        //引っ張ってる最中の波と終わったあとの波
        canvas.drawPath(mWavePath!!, mShadowPaint!!)
        canvas.drawPath(mWavePath!!, mPaint!!)
        mWavePath!!.rewind()

        //円が落ちる部分の描画
        mDropTangentPath!!.rewind()
        mDropCirclePath!!.rewind()
        val circleCenterY = mDropCircleAnimator!!.animatedValue as Float
        val circleCenterX = mWidth / 2f
        mDropRect!!.setEmpty()
        //円の座標をRectFに保存
        val scale = mDisappearCircleAnimator!!.animatedValue as Float
        val vertical = mDropBounceVerticalAnimator!!.animatedValue as Float
        val horizontal = mDropBounceHorizontalAnimator!!.animatedValue as Float
        mDropRect!!.set(circleCenterX - mDropCircleRadius * (1 + vertical) * scale + mDropCircleRadius * horizontal / 2,
                circleCenterY + mDropCircleRadius * (1 + horizontal) * scale - mDropCircleRadius * vertical / 2,
                circleCenterX + mDropCircleRadius * (1 + vertical) * scale - mDropCircleRadius * horizontal / 2,
                circleCenterY - mDropCircleRadius * (1 + horizontal) * scale + mDropCircleRadius * vertical / 2)
        val vertex = mDropVertexAnimator!!.animatedValue as Float
        mDropTangentPath!!.moveTo(circleCenterX, vertex)
        //円の接点(p1,q),(p2,q)
        val q = (Math.pow(mDropCircleRadius.toDouble(), 2.0) + circleCenterY * vertex - Math.pow(circleCenterY.toDouble(), 2.0)) / (vertex - circleCenterY)
        //2次方程式解くための解の公式
        val b = -2.0 * mWidth / 2
        val c = Math.pow(q - circleCenterY, 2.0) + Math.pow(circleCenterX.toDouble(), 2.0) - Math.pow(mDropCircleRadius.toDouble(),
                2.0)
        val p1 = (-b + Math.sqrt(b * b - 4 * c)) / 2
        val p2 = (-b - Math.sqrt(b * b - 4 * c)) / 2
        mDropTangentPath!!.lineTo(p1.toFloat(), q.toFloat())
        mDropTangentPath!!.lineTo(p2.toFloat(), q.toFloat())
        mDropTangentPath!!.close()
        mShadowPath!!.set(mDropTangentPath!!)
        mShadowPath!!.addOval(mDropRect!!, Path.Direction.CCW)
        mDropCirclePath!!.addOval(mDropRect!!, Path.Direction.CCW)
        if (mDropVertexAnimator!!.isRunning) {
            canvas.drawPath(mShadowPath!!, mShadowPaint!!)
        } else {
            canvas.drawPath(mDropCirclePath!!, mShadowPaint!!)
        }
        canvas.drawPath(mDropTangentPath!!, mPaint!!)
        canvas.drawPath(mDropCirclePath!!, mPaint!!)
    }

    override fun onDetachedFromWindow() {
        if (mDisappearCircleAnimator != null) {
            mDisappearCircleAnimator!!.end()
            mDisappearCircleAnimator!!.removeAllUpdateListeners()
        }
        if (mDropCircleAnimator != null) {
            mDropCircleAnimator!!.end()
            mDropCircleAnimator!!.removeAllUpdateListeners()
        }
        if (mDropVertexAnimator != null) {
            mDropVertexAnimator!!.end()
            mDropVertexAnimator!!.removeAllUpdateListeners()
        }
        if (mWaveReverseAnimator != null) {
            mWaveReverseAnimator!!.end()
            mWaveReverseAnimator!!.removeAllUpdateListeners()
        }
        if (mDropBounceHorizontalAnimator != null) {
            mDropBounceHorizontalAnimator!!.end()
            mDropBounceHorizontalAnimator!!.removeAllUpdateListeners()
        }
        if (mDropBounceVerticalAnimator != null) {
            mDropBounceVerticalAnimator!!.end()
            mDropBounceVerticalAnimator!!.removeAllUpdateListeners()
        }
        super.onDetachedFromWindow()
    }

    private fun initView() {
        setUpPaint()
        setUpPath()
        resetAnimator()

        mDropRect = RectF()
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    private fun setUpPaint() {
        mPaint = Paint()
        mPaint!!.color = -0xde690d
        mPaint!!.isAntiAlias = true
        mPaint!!.style = Paint.Style.FILL

        mShadowPaint = Paint()
        mShadowPaint!!.setShadowLayer(10.0f, 0.0f, 2.0f, SHADOW_COLOR)
    }

    private fun setUpPath() {
        mWavePath = Path()
        mDropTangentPath = Path()
        mDropCirclePath = Path()
        mShadowPath = Path()
    }

    private fun resetAnimator() {
        mDropVertexAnimator = ValueAnimator.ofFloat(0f, 0f)
        mDropBounceVerticalAnimator = ValueAnimator.ofFloat(0f, 0f)
        mDropBounceHorizontalAnimator = ValueAnimator.ofFloat(0f, 0f)
        mDropCircleAnimator = ValueAnimator.ofFloat(-1000f, -1000f)
        mDropCircleAnimator!!.start()
        mDisappearCircleAnimator = ValueAnimator.ofFloat(1f, 1f)
        mDisappearCircleAnimator!!.duration = 1 // immediately finish animation cycle
        mDisappearCircleAnimator!!.start()
    }

    private fun onPreDragWave() {
        if (mWaveReverseAnimator != null) {
            if (mWaveReverseAnimator!!.isRunning) {
                mWaveReverseAnimator!!.cancel()
            }
        }
    }

    fun manualRefresh() {
        if (mIsManualRefreshing) {
            return
        }
        mIsManualRefreshing = true
        mDropCircleAnimator = ValueAnimator.ofFloat(mMaxDropHeight, mMaxDropHeight)
        mDropCircleAnimator!!.start()
        mDropVertexAnimator = ValueAnimator.ofFloat(mMaxDropHeight - mDropCircleRadius,
                mMaxDropHeight - mDropCircleRadius)
        mDropVertexAnimator!!.start()
        currentCircleCenterY = mMaxDropHeight.toFloat()
        postInvalidate()
    }

    fun beginPhase(move1: Float) {
        onPreDragWave()
        //円を描画し始める前の引っ張ったら膨れる波の部分の描画
        mWavePath!!.moveTo(0f, 0f)
        //左半分の描画
        mWavePath!!.cubicTo(mWidth * BEGIN_PHASE_POINTS[0][0], BEGIN_PHASE_POINTS[0][1],
                mWidth * BEGIN_PHASE_POINTS[1][0], mWidth * (BEGIN_PHASE_POINTS[1][1] + move1),
                mWidth * BEGIN_PHASE_POINTS[2][0], mWidth * (BEGIN_PHASE_POINTS[2][1] + move1))
        mWavePath!!.cubicTo(mWidth * BEGIN_PHASE_POINTS[3][0],
                mWidth * (BEGIN_PHASE_POINTS[3][1] + move1), mWidth * BEGIN_PHASE_POINTS[4][0],
                mWidth * (BEGIN_PHASE_POINTS[4][1] + move1), mWidth * BEGIN_PHASE_POINTS[5][0],
                mWidth * (BEGIN_PHASE_POINTS[5][1] + move1))
        //右半分の描画
        mWavePath!!.cubicTo(mWidth - mWidth * BEGIN_PHASE_POINTS[4][0],
                mWidth * (BEGIN_PHASE_POINTS[4][1] + move1), mWidth - mWidth * BEGIN_PHASE_POINTS[3][0],
                mWidth * (BEGIN_PHASE_POINTS[3][1] + move1), mWidth - mWidth * BEGIN_PHASE_POINTS[2][0],
                mWidth * (BEGIN_PHASE_POINTS[2][1] + move1))
        mWavePath!!.cubicTo(mWidth - mWidth * BEGIN_PHASE_POINTS[1][0],
                mWidth * (BEGIN_PHASE_POINTS[1][1] + move1), mWidth - mWidth * BEGIN_PHASE_POINTS[0][0],
                BEGIN_PHASE_POINTS[0][1], mWidth.toFloat(), 0f)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun appearPhase(move1: Float, move2: Float) {
        onPreDragWave()
        mWavePath!!.moveTo(0f, 0f)
        //左半分の描画
        mWavePath!!.cubicTo(mWidth * APPEAR_PHASE_POINTS[0][0], mWidth * APPEAR_PHASE_POINTS[0][1],
                mWidth * Math.min(BEGIN_PHASE_POINTS[1][0] + move2, APPEAR_PHASE_POINTS[1][0]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[1][1] + move1 - move2, APPEAR_PHASE_POINTS[1][1]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[2][0] - move2, APPEAR_PHASE_POINTS[2][0]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[2][1] + move1 - move2, APPEAR_PHASE_POINTS[2][1]))
        mWavePath!!.cubicTo(
                mWidth * Math.max(BEGIN_PHASE_POINTS[3][0] - move2, APPEAR_PHASE_POINTS[3][0]),
                mWidth * Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[4][0] - move2, APPEAR_PHASE_POINTS[4][0]),
                mWidth * Math.min(BEGIN_PHASE_POINTS[4][1] + move1 + move2, APPEAR_PHASE_POINTS[4][1]),
                mWidth * APPEAR_PHASE_POINTS[5][0],
                mWidth * Math.min(BEGIN_PHASE_POINTS[0][1] + move1 + move2, APPEAR_PHASE_POINTS[5][1]))
        //右半分の描画
        mWavePath!!.cubicTo(
                mWidth - mWidth * Math.max(BEGIN_PHASE_POINTS[4][0] - move2, APPEAR_PHASE_POINTS[4][0]),
                mWidth * Math.min(BEGIN_PHASE_POINTS[4][1] + move1 + move2, APPEAR_PHASE_POINTS[4][1]),
                mWidth - mWidth * Math.max(BEGIN_PHASE_POINTS[3][0] - move2, APPEAR_PHASE_POINTS[3][0]),
                mWidth * Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]),
                mWidth - mWidth * Math.max(BEGIN_PHASE_POINTS[2][0] - move2, APPEAR_PHASE_POINTS[2][0]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[2][1] + move1 - move2, APPEAR_PHASE_POINTS[2][1]))
        mWavePath!!.cubicTo(
                mWidth - mWidth * Math.min(BEGIN_PHASE_POINTS[1][0] + move2, APPEAR_PHASE_POINTS[1][0]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[1][1] + move1 - move2, APPEAR_PHASE_POINTS[1][1]),
                mWidth - mWidth * APPEAR_PHASE_POINTS[0][0], mWidth * APPEAR_PHASE_POINTS[0][1], mWidth.toFloat(), 0f)
        currentCircleCenterY = mWidth * Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]) + mDropCircleRadius
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun expandPhase(move1: Float, move2: Float, move3: Float) {
        onPreDragWave()
        mWavePath!!.moveTo(0f, 0f)
        //左半分の描画
        mWavePath!!.cubicTo(mWidth * EXPAND_PHASE_POINTS[0][0], mWidth * EXPAND_PHASE_POINTS[0][1],
                mWidth * Math.min(
                        Math.min(BEGIN_PHASE_POINTS[1][0] + move2, APPEAR_PHASE_POINTS[1][0]) + move3,
                        EXPAND_PHASE_POINTS[1][0]), mWidth * Math.max(
                Math.max(BEGIN_PHASE_POINTS[1][1] + move1 - move2, APPEAR_PHASE_POINTS[1][1]) - move3,
                EXPAND_PHASE_POINTS[1][1]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[2][0] - move2, EXPAND_PHASE_POINTS[2][0]),
                mWidth * Math.min(
                        Math.max(BEGIN_PHASE_POINTS[2][1] + move1 - move2, APPEAR_PHASE_POINTS[2][1]) + move3,
                        EXPAND_PHASE_POINTS[2][1]))
        mWavePath!!.cubicTo(mWidth * Math.min(
                Math.max(BEGIN_PHASE_POINTS[3][0] - move2, APPEAR_PHASE_POINTS[3][0]) + move3,
                EXPAND_PHASE_POINTS[3][0]), mWidth * Math.min(
                Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]) + move3,
                EXPAND_PHASE_POINTS[3][1]),
                mWidth * Math.max(BEGIN_PHASE_POINTS[4][0] - move2, EXPAND_PHASE_POINTS[4][0]),
                mWidth * Math.min(
                        Math.min(BEGIN_PHASE_POINTS[4][1] + move1 + move2, APPEAR_PHASE_POINTS[4][1]) + move3,
                        EXPAND_PHASE_POINTS[4][1]), mWidth * EXPAND_PHASE_POINTS[5][0], mWidth * Math.min(
                Math.min(BEGIN_PHASE_POINTS[0][1] + move1 + move2, APPEAR_PHASE_POINTS[5][1]) + move3,
                EXPAND_PHASE_POINTS[5][1]))

        //右半分の描画
        mWavePath!!.cubicTo(
                mWidth - mWidth * Math.max(BEGIN_PHASE_POINTS[4][0] - move2, EXPAND_PHASE_POINTS[4][0]),
                mWidth * Math.min(
                        Math.min(BEGIN_PHASE_POINTS[4][1] + move1 + move2, APPEAR_PHASE_POINTS[4][1]) + move3,
                        EXPAND_PHASE_POINTS[4][1]), mWidth - mWidth * Math.min(
                Math.max(BEGIN_PHASE_POINTS[3][0] - move2, APPEAR_PHASE_POINTS[3][0]) + move3,
                EXPAND_PHASE_POINTS[3][0]), mWidth * Math.min(
                Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]) + move3,
                EXPAND_PHASE_POINTS[3][1]),
                mWidth - mWidth * Math.max(BEGIN_PHASE_POINTS[2][0] - move2, EXPAND_PHASE_POINTS[2][0]),
                mWidth * Math.min(
                        Math.max(BEGIN_PHASE_POINTS[2][1] + move1 - move2, APPEAR_PHASE_POINTS[2][1]) + move3,
                        EXPAND_PHASE_POINTS[2][1]))
        mWavePath!!.cubicTo(mWidth - mWidth * Math.min(
                Math.min(BEGIN_PHASE_POINTS[1][0] + move2, APPEAR_PHASE_POINTS[1][0]) + move3,
                EXPAND_PHASE_POINTS[1][0]), mWidth * Math.max(
                Math.max(BEGIN_PHASE_POINTS[1][1] + move1 - move2, APPEAR_PHASE_POINTS[1][1]) - move3,
                EXPAND_PHASE_POINTS[1][1]), mWidth - mWidth * EXPAND_PHASE_POINTS[0][0],
                mWidth * EXPAND_PHASE_POINTS[0][1], mWidth.toFloat(), 0f)
        currentCircleCenterY = mWidth * Math.min(
                Math.min(BEGIN_PHASE_POINTS[3][1] + move1 + move2, APPEAR_PHASE_POINTS[3][1]) + move3,
                EXPAND_PHASE_POINTS[3][1]) + mDropCircleRadius
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * @param height 高さ
     */
    private fun updateMaxDropHeight(height: Int) {
        if (500 * (mWidth / 1440f) > height) {
            Log.w("WaveView", "DropHeight is more than " + 500 * (mWidth / 1440f))
            return
        }
        mMaxDropHeight = Math.min(height.toFloat(), getHeight() - mDropCircleRadius)
        if (mIsManualRefreshing) {
            mIsManualRefreshing = false
            manualRefresh()
        }
    }

    fun startDropAnimation() {
        // show dropBubble again
        mDisappearCircleAnimator = ValueAnimator.ofFloat(1f, 1f)
        mDisappearCircleAnimator!!.duration = 1
        mDisappearCircleAnimator!!.start()

        mDropCircleAnimator = ValueAnimator.ofFloat(500 * (mWidth / 1440f), mMaxDropHeight)
        mDropCircleAnimator!!.duration = DROP_CIRCLE_ANIMATOR_DURATION
        mDropCircleAnimator!!.addUpdateListener { animation ->
            currentCircleCenterY = animation.animatedValue as Float
            ViewCompat.postInvalidateOnAnimation(this@WaveView)
        }
        mDropCircleAnimator!!.interpolator = AccelerateDecelerateInterpolator()
        mDropCircleAnimator!!.start()

        mDropVertexAnimator = ValueAnimator.ofFloat(0f, mMaxDropHeight - mDropCircleRadius)
        mDropVertexAnimator!!.duration = DROP_VERTEX_ANIMATION_DURATION
        mDropVertexAnimator!!.addUpdateListener(mAnimatorUpdateListener)
        mDropVertexAnimator!!.start()

        mDropBounceVerticalAnimator = ValueAnimator.ofFloat(0f, 1f)
        mDropBounceVerticalAnimator!!.duration = DROP_BOUNCE_ANIMATOR_DURATION
        mDropBounceVerticalAnimator!!.addUpdateListener(mAnimatorUpdateListener)
        mDropBounceVerticalAnimator!!.interpolator = DropBounceInterpolator()
        mDropBounceVerticalAnimator!!.startDelay = DROP_VERTEX_ANIMATION_DURATION
        mDropBounceVerticalAnimator!!.start()

        mDropBounceHorizontalAnimator = ValueAnimator.ofFloat(0f, 1f)
        mDropBounceHorizontalAnimator!!.duration = DROP_BOUNCE_ANIMATOR_DURATION
        mDropBounceHorizontalAnimator!!.addUpdateListener(mAnimatorUpdateListener)
        mDropBounceHorizontalAnimator!!.interpolator = DropBounceInterpolator()
        mDropBounceHorizontalAnimator!!.startDelay = (DROP_VERTEX_ANIMATION_DURATION + DROP_BOUNCE_ANIMATOR_DURATION * 0.25).toLong()
        mDropBounceHorizontalAnimator!!.start()
    }

    fun startDisappearCircleAnimation() {
        mDisappearCircleAnimator = ValueAnimator.ofFloat(1f, 0f)
        mDisappearCircleAnimator!!.addUpdateListener(mAnimatorUpdateListener)
        mDisappearCircleAnimator!!.duration = DROP_REMOVE_ANIMATOR_DURATION.toLong()
        mDisappearCircleAnimator!!.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                //アニメーション修旅時にAnimatorをリセットすることにより落ちてくる円の初期位置を-100.fにする
                resetAnimator()
                mIsManualRefreshing = false
            }

            override fun onAnimationCancel(animator: Animator) {

            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })
        mDisappearCircleAnimator!!.start()
    }

    /**
     * @param h 波が始まる高さ
     */
    fun startWaveAnimation(h: Float) {
        var h = h
        h = Math.min(h, MAX_WAVE_HEIGHT) * mWidth
        mWaveReverseAnimator = ValueAnimator.ofFloat(h, 0f)
        mWaveReverseAnimator!!.duration = WAVE_ANIMATOR_DURATION.toLong()
        mWaveReverseAnimator!!.addUpdateListener { valueAnimator ->
            val h = valueAnimator.animatedValue as Float
            mWavePath!!.moveTo(0f, 0f)
            mWavePath!!.quadTo(0.25f * mWidth, 0f, 0.333f * mWidth, h * 0.5f)
            mWavePath!!.quadTo(mWidth * 0.5f, h * 1.4f, 0.666f * mWidth, h * 0.5f)
            mWavePath!!.quadTo(0.75f * mWidth, 0f, mWidth.toFloat(), 0f)
            postInvalidate()
        }
        mWaveReverseAnimator!!.interpolator = BounceInterpolator()
        mWaveReverseAnimator!!.start()
    }

    fun animationDropCircle() {
        if (mDisappearCircleAnimator!!.isRunning) {
            return
        }
        startDropAnimation()
        startWaveAnimation(0.1f)
    }

    /**
     * @param maxDropHeight ある程度の高さ
     */
    fun setMaxDropHeight(maxDropHeight: Int) {
        if (mDropHeightUpdated) {
            updateMaxDropHeight(maxDropHeight)
        } else {
            mUpdateMaxDropHeight = maxDropHeight
            mDropHeightUpdated = true
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.removeOnPreDrawListener(this)
                viewTreeObserver.addOnPreDrawListener(this)
            }
        }
    }

    /**
     * @param radius 影の深さ
     */
    fun setShadowRadius(radius: Int) {
        mShadowPaint!!.setShadowLayer(radius.toFloat(), 0.0f, 2.0f, SHADOW_COLOR)
    }

    /**
     * WaveView is colored by given color (including alpha)
     *
     * @param color ARGB color. WaveView will be colored by Black if rgb color is provided.
     * @see Paint.setColor
     */
    fun setWaveColor(color: Int) {
        mPaint!!.color = color
        invalidate()
    }

    fun setWaveARGBColor(a: Int, r: Int, g: Int, b: Int) {
        mPaint!!.setARGB(a, r, g, b)
        invalidate()
    }

    companion object {

        /**
         * [WaveView.mDropCircleAnimator] のDuration
         */
        private val DROP_CIRCLE_ANIMATOR_DURATION: Long = 500

        /**
         * [WaveView.mDropBounceVerticalAnimator] のDuration
         */
        private val DROP_VERTEX_ANIMATION_DURATION: Long = 500

        /**
         * [WaveView.mDropBounceVerticalAnimator] と [WaveView.mDropBounceHorizontalAnimator]
         * のDuration
         */
        private val DROP_BOUNCE_ANIMATOR_DURATION: Long = 500

        /**
         * [WaveView.mDisappearCircleAnimator] のDuration
         */
        private val DROP_REMOVE_ANIMATOR_DURATION = 200

        /**
         * 波がくねくねしているDuration
         */
        private val WAVE_ANIMATOR_DURATION = 1000

        /**
         * 波の最大の高さ
         */
        private val MAX_WAVE_HEIGHT = 0.2f

        /**
         * 影の色
         */
        private val SHADOW_COLOR = -0x1000000

        /**
         * ベジェ曲線を引く際の座標
         * 左側の2つのアンカーポイントでいい感じに右側にも
         */
        private val BEGIN_PHASE_POINTS = arrayOf(
                //1
                floatArrayOf(0.1655f, 0f), //ハンドル
                floatArrayOf(0.4188f, -0.0109f), //ハンドル
                floatArrayOf(0.4606f, -0.0049f), //アンカーポイント

                //2
                floatArrayOf(0.4893f, 0f), //ハンドル
                floatArrayOf(0.4893f, 0f), //ハンドル
                floatArrayOf(0.5f, 0f)             //アンカーポイント
        )

        private val APPEAR_PHASE_POINTS = arrayOf(
                //1
                floatArrayOf(0.1655f, 0f), //ハンドル
                floatArrayOf(0.5237f, 0.0553f), //ハンドル
                floatArrayOf(0.4557f, 0.0936f), //アンカーポイント

                //2
                floatArrayOf(0.3908f, 0.1302f), //ハンドル
                floatArrayOf(0.4303f, 0.2173f), //ハンドル
                floatArrayOf(0.5f, 0.2173f)         //アンカーポイント
        )

        private val EXPAND_PHASE_POINTS = arrayOf(
                //1
                floatArrayOf(0.1655f, 0f), //ハンドル
                floatArrayOf(0.5909f, 0.0000f), //ハンドル
                floatArrayOf(0.4557f, 0.1642f), //アンカーポイント

                //2
                floatArrayOf(0.3941f, 0.2061f), //ハンドル
                floatArrayOf(0.4303f, 0.2889f), //ハンドル
                floatArrayOf(0.5f, 0.2889f)         //アンカーポイント
        )
    }
}
