/*
 * Copyright 2018 The Android Open Source Project
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
package com.example.android.interactivesliceprovider.sliceactivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.interactivesliceprovider.R

/**
 * This Activity is launched when the user clicks on the wifi slice. It is a "full screen"
 * representation of the data and actions in the slice.
 *
 * It is registered in the AndroidManifest.xml with an intent-filter that matches the
 * PendingIntent created in the `InteractiveSliceProvider`.
 */
class WifiSliceActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc. This method also provides you with a
     * Bundle containing the activity's previously frozen state, if there was one.
     *
     * Always followed by `onStart()`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this Bundle contains the data it most recently supplied in `onSaveInstanceState(Bundle)`.
     * **Note: Otherwise it is null.**
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wifi_slice)
    }
}
