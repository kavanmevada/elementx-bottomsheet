package elementx.bottomsheet

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import elementx.ui.bottomsheet.R


/**
 * Created by quentin on 07/11/2017.
 */
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
    private var collapsedHeight: Int = 0

    private var progress = 0f
    private var startsCollapsed = true

    private var scrollTranslationY = 0f
    private var userTranslationY = 0f

    private var isScrollingUp: Boolean = false
    var animationDuration: Long = 300

    var eventListener: BottomSheetCallback? = null

    private var mState: BottomSheetBehavior = BottomSheetBehavior.STATE_COLLAPSED


    override fun setTranslationY(translationY: Float) {
        userTranslationY = translationY
        super.setTranslationY(scrollTranslationY + userTranslationY)
    }

    private fun initView(attrs: AttributeSet?) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetLayout)

        collapsedHeight = a.getDimensionPixelSize(R.styleable.BottomSheetLayout_behavior_peekHeight, 0)
        minimumHeight = Math.max(minimumHeight, collapsedHeight)

        a.recycle()

        setOnTouchListener(touchToDragListener)

        if (height == 0) {
            addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(view: View, i: Int, i1: Int, i2: Int, i3: Int, i4: Int, i5: Int, i6: Int, i7: Int) {
                    removeOnLayoutChangeListener(this)
                    animate(0f)
                }
            })
        } else {
            animate(0f)
        }
    }

    //1 is expanded, 0 is collapsed
    private fun animate(progress: Float) {
        this.progress = progress
        val height = height
        val distance = height - collapsedHeight
        scrollTranslationY = distance * (1 - progress)
        super.setTranslationY(scrollTranslationY + userTranslationY)

        eventListener?.onSlide(this, progress)
    }

    private fun animateScroll(firstPos: Float, touchPos: Float) {
        val distance = touchPos - firstPos
        val height = height
        val totalDistance = height - collapsedHeight
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

        val progressLimit = if (isScrollingUp) 0.2f else 0.8f

        valueAnimator.apply {
            if (progress > progressLimit) {
                setFloatValues(progress, 1f)
                duration = (animationDuration * (1 - progress)).toLong()
            } else {
                setFloatValues(progress, 0f)
                duration = (animationDuration * progress).toLong()
            }

            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Float
                animate(progress)
                setSheetState(this@BottomSheetLayout, when (progress) {
                    0f -> BottomSheetBehavior.STATE_COLLAPSED
                    1f -> BottomSheetBehavior.STATE_EXPANDED
                    else -> BottomSheetBehavior.STATE_SETTLING
                })
            }
        }.start()
    }



    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?.let { touchToDragListener.onTouch(this, ev) }
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
                MotionEvent.ACTION_UP ->
                    (Math.abs(startX - it.rawX) > 0 || Math.abs(startY - it.rawY) > 0)
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
                MotionEvent.ACTION_DOWN -> if (ev.pointerCount == 1) {
                    startX = ev.rawX
                    startY = ev.rawY
                    startTime = System.currentTimeMillis().toDouble()
                    startsCollapsed = progress < 0.5
                }

                MotionEvent.ACTION_MOVE -> {
                    animateScroll(startY, ev.rawY)
                    invalidate()
                }

                MotionEvent.ACTION_UP -> animateScrollEnd()
            }
            return true
            //v.parent.requestDisallowInterceptTouchEvent(true) //specific to my project
        }
    }


    private fun setSheetState(view: View, state: BottomSheetBehavior) {
        if (mState != state) {
            eventListener?.onStateChanged(view, state)
            mState = state
        }
    }

    interface BottomSheetCallback {
        fun onStateChanged(bottomSheet: View, newState: BottomSheetBehavior)
        fun onSlide(bottomSheet: View, slideOffset: Float)
    }
}

enum class BottomSheetBehavior {
    STATE_DRAGGING, STATE_SETTLING, STATE_EXPANDED, STATE_COLLAPSED
}