# elementx-bottomsheet

## Introduction
Bottom Sheet for android platform.

## Use Library
In the `layout.xml`

```
  <elementx.bottomsheet.BottomSheetLayout
    ...>

    // All layouts here ...
  
    <View android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:background="#41000000"
          tools:visibility="gone" />

    <include android:layout_gravity="bottom"
      ... />

</elementx.bottomsheet.BottomSheetLayout>
```
In the `MainActivity.kt`

```     
        // All bellow these methods are optional
        // `bottomSheet` is ID of <elementx.bottomsheet.BottomSheetLayout...>
        // To set it's state before draw
        bottomSheetLayout.setState(BottomSheetBehavior.STATE_COLLAPSED)
        // Set is height in collapse mode
        bottomSheetLayout.setPeekHeight(200)
        // Add Listen to listen its callback
        bottomSheetLayout.eventListener = object : BottomSheetLayout.BottomSheetCallback {
            override fun onStateChanged(mState: BottomSheetBehavior) {
                val state = if (mState == BottomSheetBehavior.STATE_COLLAPSED) "STATE_COLLAPSED"
                else if (mState == BottomSheetBehavior.STATE_EXPANDED) "STATE_EXPANDED"
                else if (mState == BottomSheetBehavior.STATE_DRAGGING) "STATE_DRAGGING"
                else "STATE_SETTLING"
            }

            override fun onSlide(it: Float) {
              // it gives animated values from [0-1] during animation
            }
        }
        // Toggle its state using buttom
        button.setOnClickListener {
            if (bottomSheetLayout.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetLayout.show()
            } else bottomSheetLayout.close()
        }
```


thanks to it you will learn how to use the library.

*There are different ways of adding this library to your code*

### Gradle / Maven dependency
At the moment we do not have a publishing mechanism to a maven repository so the easiest way to add the library to your app is via a GitLab Dependency

```
repositories {
    ...
    maven { url "https://github.com/kavanmevada/maven-repo/raw/master" }
}
dependencies {
    ...
    implementation 'elementx.bottomsheet:bottomsheet:1.0.0'
```

##  License

```
Copyright 2018 Kavan Mevada

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
