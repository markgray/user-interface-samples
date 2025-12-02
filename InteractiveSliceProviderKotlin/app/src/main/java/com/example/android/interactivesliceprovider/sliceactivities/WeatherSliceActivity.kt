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
 * This activity is displayed when the user clicks on the "See weather" action on the slice.
 *
 * In a real world scenario, this would be a fully featured weather app.
 */
class WeatherSliceActivity : AppCompatActivity() {

    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     *
     * This implementation simply sets the content view to a layout resource that displays a
     * placeholder for a weather activity.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in
     * `onSaveInstanceState(Bundle)`. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_slice)
    }
}
