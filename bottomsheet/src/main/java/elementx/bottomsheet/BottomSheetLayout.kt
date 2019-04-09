package elementx.bottomsheet

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout

enum class BottomSheetBehavior {
    STATE_DRAGGING, STATE_SETTLING, STATE_EXPANDED, STATE_COLLAPSED
}

class BottomSheetLayout(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet), View.OnTouchListener {

    private var parentView: View = this
    var sheetView: View? = this
    var overlayScreen: View? = this

    private var dY = 0f
    private var timer = 0L
    private var oldY = 0f
    private var peekHeight = 0

    private var parentHeight = 0
    private var viewHeight = 0

    private var upperLimit = 0
    private var lowerLimit = 0
    private var travelY: Float = 0.0f

    var eventListener : BottomSheetCallback? = null

    private var mState: BottomSheetBehavior = BottomSheetBehavior.STATE_EXPANDED




    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        sheetView = getChildAt(childCount-1)
        sheetView?.setOnTouchListener(this)

        overlayScreen = getChildAt(childCount-2)
        overlayScreen?.setOnClickListener {
            sheetView?.collapsPanel()
        }

        parentHeight = parentView.height
        viewHeight = sheetView?.height ?: 0

        if (peekHeight>viewHeight) peekHeight = viewHeight

        upperLimit = parentHeight-viewHeight
        lowerLimit = parentHeight-peekHeight


        sheetView?.let {
            if (mState == BottomSheetBehavior.STATE_COLLAPSED) {
                it.animate()
                    .y((parentHeight-peekHeight).toFloat())
                    .setDuration(0)
                    .start()
                setOverlay(1f)
            }
        }
    }


    override fun onTouch(view: View, event: MotionEvent): Boolean {
        sheetView?.let {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dY = it.y - event.rawY
                    oldY = event.rawY
                    timer = System.currentTimeMillis()
                }

                MotionEvent.ACTION_MOVE -> {
                    travelY = event.rawY + dY

                    if (travelY>=upperLimit && travelY<=lowerLimit){
                        it.animate()
                            .y(travelY)
                            .setDuration(0)
                            .setUpdateListener(null)
                            .start()
                    }
                    val animatedValue = (it.y-upperLimit)/(viewHeight-peekHeight)
                    eventListener?.onSlide(animatedValue)
                    setOverlay(animatedValue)
                    setState(BottomSheetBehavior.STATE_DRAGGING)
                }

                MotionEvent.ACTION_UP -> {
                    val timerTime = System.currentTimeMillis() - timer
                    val speed = travelY / timerTime
                    val deltaY = event.rawY - oldY

                    if (speed>5 && deltaY>=200) {
                        if (deltaY>0) it.collapsPanel() else it.expandPanel()
                    } else {
                        val anchor = upperLimit+((viewHeight-peekHeight) * .7)
                        if (travelY >= anchor) it.collapsPanel() else it.expandPanel()
                    }
                }

                else -> return false
            }
        }
        return true
    }


    private fun View.collapsPanel() = animate()
        .y(lowerLimit.toFloat())
        .setDuration((150).toLong())
        .setInterpolator(DecelerateInterpolator())
        .setUpdateListener {
            val animatedValue = (y-upperLimit)/(viewHeight-peekHeight)
            eventListener?.onSlide(animatedValue)
            setOverlay(animatedValue)
            setState(when (animatedValue) {
                1f -> BottomSheetBehavior.STATE_COLLAPSED
                0f -> BottomSheetBehavior.STATE_EXPANDED
                else -> BottomSheetBehavior.STATE_SETTLING
            })
        }
        .start()

    private fun View.expandPanel() = animate()
        .y(upperLimit.toFloat())
        .setDuration((200).toLong())
        .setInterpolator(DecelerateInterpolator())
        .setUpdateListener {
            val animatedValue = (y-upperLimit)/(viewHeight-peekHeight)
            eventListener?.onSlide(animatedValue)
            setOverlay(animatedValue)
            setState(when (animatedValue) {
                1f -> BottomSheetBehavior.STATE_COLLAPSED
                0f -> BottomSheetBehavior.STATE_EXPANDED
                else -> BottomSheetBehavior.STATE_SETTLING
            })
        }
        .start()

    private fun setOverlay(it: Float){
        overlayScreen?.let { overlay ->
            if (it < 1f) overlay.visibility = View.VISIBLE
            else overlay.visibility = View.INVISIBLE
            overlay.alpha = 1-it
        }
    }





    fun show() = sheetView?.expandPanel()
    fun close() = sheetView?.collapsPanel()

    fun setState(state: BottomSheetBehavior) {
        if (mState != state){
            eventListener?.onStateChanged(state)
            mState = state
        }
    }

    fun getState() = mState

    fun setPeekHeight(height: Int){
        peekHeight = height
    }


    interface BottomSheetCallback {
        fun onStateChanged(mState: BottomSheetBehavior)
        fun onSlide(it: Float) {}
    }



}







