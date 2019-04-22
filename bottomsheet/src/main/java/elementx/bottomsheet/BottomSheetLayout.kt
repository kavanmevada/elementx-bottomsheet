/*
 * Copyright (C) 2019  Kavan Mevada
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package elementx.bottomsheet

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout


class BottomSheetLayout : LinearLayout {
    constructor(context: Context) : super(context) {
        initView(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView(attrs)
    }

    private var valueAnimator = ValueAnimator()
    private var peekHeight: Int = 0
    private var withPeekHeight = false

    private var progress = 0f

    private var scrollTranslationY = 0f
    private var userTranslationY = 0f

    private var isScrollingUp: Boolean = false
    var animationDuration: Long = 450

    var eventListener: BottomSheetCallback? = null

    private var mState: BottomSheetBehavior = BottomSheetBehavior.STATE_COLLAPSED
    private var startsCollapsed = true


    override fun setTranslationY(translationY: Float) {
        userTranslationY = translationY
        super.setTranslationY(scrollTranslationY + userTranslationY)
    }

    private fun initView(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetLayout)

        peekHeight = a.getDimensionPixelSize(R.styleable.BottomSheetLayout_behavior_peekHeight, 0)
        withPeekHeight = a.getBoolean(R.styleable.BottomSheetLayout_height_withoutPeek, false)

        minimumHeight = Math.max(minimumHeight, peekHeight)

        a.recycle()

        setOnTouchListener(touchToDragListener)

        if (height == 0) addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(
                view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int) {
                removeOnLayoutChangeListener(this)
                animate(0f)
            }
        }) else animate(0f)


    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //setMeasuredDimension(measuredWidth, measuredHeight+peekHeight)
        super.onMeasure(widthMeasureSpec, if (withPeekHeight) heightMeasureSpec+peekHeight else heightMeasureSpec)
    }


    //1 is expanded, 0 is collapsed
    private fun animate(progress: Float) {
        this.progress = progress
        val distance = measuredHeight - peekHeight
        scrollTranslationY = distance * (1 - progress)
        super.setTranslationY(scrollTranslationY + userTranslationY)

        eventListener?.onSlide(this, progress)
    }

    private fun animateScroll(firstPos: Float, touchPos: Float) {
        val distance = touchPos - firstPos
        val totalDistance = measuredHeight - peekHeight
        var progress = this.progress
        if (!startsCollapsed) {
            isScrollingUp = false
            progress = Math.max(0f, 1 - distance / totalDistance)
        } else if (startsCollapsed) {
            isScrollingUp = true
            progress = Math.min(1f, -distance / totalDistance)
        }
        progress = Math.max(0f, Math.min(1f, progress))
        animate(progress)

        setSheetState(this, BottomSheetBehavior.STATE_DRAGGING)
    }

    private fun animateScrollEnd() {
        if (valueAnimator.isRunning) valueAnimator.cancel()
        val progressLimit = if (isScrollingUp) 0.1f else 0.9f
        if (progress > progressLimit) show() else close()
    }


    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { when (ev.action) { MotionEvent.ACTION_UP -> animateScrollEnd() } }
        return super.dispatchTouchEvent(ev)
    }


    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { touchToDragListener.onTouch(this, ev) }
        return passEventToChild(ev)
    }


    private var startX = 0f
    private var startY = 0f
    private fun passEventToChild(ev: MotionEvent?): Boolean {
        return ev?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = it.rawX
                    startY = it.rawY
                    false
                }
                // Only Allow click on child while COLLAPSED or EXPANDED
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> !(progress == 1f || progress == 0f)
//                MotionEvent.ACTION_MOVE -> {
//                    if (progress==1f) false else true
//                }
//                MotionEvent.ACTION_UP -> (Math.abs(startX - it.rawX) > 50 || Math.abs(startY - it.rawY) > 50)
                else -> false
            }
        } ?: false
    }


    private val touchToDragListener = object : OnTouchListener {


        private var startX: Float = 0.toFloat()
        private var startY: Float = 0.toFloat()
        private var startTime: Double = 0.toDouble()

        override fun onTouch(v: View, ev: MotionEvent): Boolean {
            //val action = MotionEventCompat.getActionMasked(ev)
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = ev.rawX
                    startY = ev.rawY//+10 // plus value create lift on touch which make feel like ready to open
                    startTime = System.currentTimeMillis().toDouble()
                    startsCollapsed = progress < 0.5
                }

                MotionEvent.ACTION_MOVE -> {
                    animateScroll(startY, ev.rawY)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> {
                    animateScrollEnd()
                    performClick()
                }
            }
            return true
            //v.parent.requestDisallowInterceptTouchEvent(true) //specific to my project
        }
    }

    private fun animateTranslate(toState: BottomSheetBehavior) {
        if (valueAnimator.isRunning) valueAnimator.cancel()

        valueAnimator.apply {
            interpolator = DecelerateInterpolator()

            if (toState == BottomSheetBehavior.STATE_COLLAPSED) {
                setFloatValues(progress, 0f)
                duration = (animationDuration * progress).toLong()
            } else if (toState == BottomSheetBehavior.STATE_EXPANDED) {
                setFloatValues(progress, 1f)
                duration = (animationDuration * (1 - progress)).toLong()
            }

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                animate(progress)
                setSheetState(
                    this@BottomSheetLayout, when (progress) {
                        0f -> BottomSheetBehavior.STATE_COLLAPSED
                        1f -> BottomSheetBehavior.STATE_EXPANDED
                        else -> BottomSheetBehavior.STATE_SETTLING
                    }
                )
            }
        }.start()
    }

    fun show() = animateTranslate(BottomSheetBehavior.STATE_EXPANDED)
    fun close() = animateTranslate(BottomSheetBehavior.STATE_COLLAPSED)

    fun toggle() {
        if (valueAnimator.isRunning) valueAnimator.cancel()
        if (progress > 0.5f) close() else show()
    }

    private fun setSheetState(view: View, state: BottomSheetBehavior) {
        if (mState != state) {
            eventListener?.onStateChanged(view, state)
            mState = state
        }
    }

    val state get() = mState

    fun setState(state: BottomSheetBehavior) {
        mState = state
        if (state == BottomSheetBehavior.STATE_COLLAPSED) startsCollapsed = true
        else if (state == BottomSheetBehavior.STATE_COLLAPSED) startsCollapsed = false
    }

    interface BottomSheetCallback {
        fun onStateChanged(bottomSheet: View, newState: BottomSheetBehavior)
        fun onSlide(bottomSheet: View, slideOffset: Float)
    }
}

enum class BottomSheetBehavior {
    STATE_DRAGGING, STATE_SETTLING, STATE_EXPANDED, STATE_COLLAPSED
}