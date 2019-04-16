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

package elementx.test.slideupactivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import elementx.bottomsheet.BottomSheetBehavior
import elementx.bottomsheet.BottomSheetLayout
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scrollView.requestDisallowInterceptTouchEvent(true) //specific to my project

        button.setOnClickListener {
            println("Button CLicked!")
        }



        //bottomSheetLayout.setState(BottomSheetBehavior.STATE_COLLAPSED)
        //bottomSheetLayout.setPeekHeight(200)
        bottomSheetLayout.eventListener = object : BottomSheetLayout.BottomSheetCallback {
            override fun onStateChanged(bottomSheet: View, newState: BottomSheetBehavior) {
                val state = if (newState == BottomSheetBehavior.STATE_COLLAPSED) "STATE_COLLAPSED"
                else if (newState == BottomSheetBehavior.STATE_EXPANDED) "STATE_EXPANDED"
                else if (newState == BottomSheetBehavior.STATE_DRAGGING) "STATE_DRAGGING"
                else "STATE_SETTLING"

                println(state)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                println(slideOffset)
            }
        }

    }


}



