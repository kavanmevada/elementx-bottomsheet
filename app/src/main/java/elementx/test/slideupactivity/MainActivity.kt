package elementx.test.slideupactivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import elementx.bottomsheet.BottomSheetBehavior
import elementx.bottomsheet.BottomSheetLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        bottomSheetLayout.setState(BottomSheetBehavior.STATE_COLLAPSED)
        bottomSheetLayout.setPeekHeight(200)
        bottomSheetLayout.eventListener = object : BottomSheetLayout.BottomSheetCallback {
            override fun onStateChanged(mState: BottomSheetBehavior) {
                val state = if (mState == BottomSheetBehavior.STATE_COLLAPSED) "STATE_COLLAPSED"
                else if (mState == BottomSheetBehavior.STATE_EXPANDED) "STATE_EXPANDED"
                else if (mState == BottomSheetBehavior.STATE_DRAGGING) "STATE_DRAGGING"
                else "STATE_SETTLING"

                println(state)
            }

            override fun onSlide(it: Float) {}
        }



        button.setOnClickListener {
            if (bottomSheetLayout.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetLayout.show()
            } else bottomSheetLayout.close()
        }


    }


}



