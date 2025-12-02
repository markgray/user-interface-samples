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
 * This activity is launched when the user clicks on the header of the list slice, or when they
 * click the "See more" row at the bottom of the list slice. It is a full screen version of the
 * list slice, and is used to respond to the URI: "content://com.example.android.interactivesliceprovider/list"
 */
class ListSliceActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting.
     *
     * This is where most initialization should go: calling `setContentView(int)`
     * to inflate the activity's UI, using `findViewById(int)` to programmatically
     * interact with widgets in the UI, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in `onSaveInstanceState(Bundle)`.
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_slice)
    }
}
