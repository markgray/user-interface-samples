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

package com.example.android.sliceviewer.provider

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.slice.builders.SliceAction

/**
 * This activity is used as the destination of the `SliceAction` of the slice URI:
 *
 *     content://com.example.android.sliceviewer/test
 *
 * and is launched when that slice URI is clicked in the `RecycleView`.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * Convenience function for constructing an [Intent] to launch the [MainActivity] activity.
         * Used in the `createTestSlice` method of [SampleSliceProvider] to create the [PendingIntent]
         * that is invoked when the [SliceAction] of the slice URI is executed.
         *
         * @param origin the [Context] that the [SampleSliceProvider] provider is running in.
         * @return an [Intent] which will launch our [MainActivity] activity.
         */
        fun getIntent(origin: Context): Intent {
            return Intent(origin, MainActivity::class.java)
        }
    }
}