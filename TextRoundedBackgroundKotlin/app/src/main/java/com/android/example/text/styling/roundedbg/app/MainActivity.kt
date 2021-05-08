/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.example.text.styling.roundedbg.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Sample activity that uses [com.android.example.text.styling.roundedbg.RoundedBgTextView].
 * Our layout file [R.layout.activity_main] holds a bunch of these widgets demonstrating what
 * the `RoundedBgTextView` does when displaying different text using different styles
 */
class MainActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main] which exercises all
     * the features of [com.android.example.text.styling.roundedbg.RoundedBgTextView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
