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
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.SliceProvider
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.SliceAction
import androidx.slice.builders.header
import androidx.slice.builders.list
import androidx.slice.builders.row
import androidx.slice.core.SliceHints
import com.example.android.sliceviewer.R

/**
 * This is the [SliceProvider] for the four [Slice]s contained in our app:
 *  - content://com.example.android.sliceviewer/hello
 *  - content://com.example.android.sliceviewer/test
 *  - https://sliceviewer.android.example.com/hello
 *  - https://sliceviewer.android.example.com/test
 *
 * It is named as such by a `provider` element in our AndroidManifest.xml file. The attribute
 * `android:authorities` assigns it the authority "com.example.android.sliceviewer", and it has
 * an `intent-filter` with an `action` name "android.intent.action.VIEW", and a `category` name
 * "android.app.slice.category.SLICE" (Category used to resolve intents that can be rendered as
 * slices. This category should be included on intent filters of providers that extend
 * [SliceProvider]). The `intent-filter` also has a `data` element which defines the android:scheme
 * "https" for the `android:host` "sliceviewer.android.example.com"
 */
class SampleSliceProvider : SliceProvider() {
    override fun onBindSlice(sliceUri: Uri): Slice? {
        @Suppress("NullChecksToSafeCall", "SENSELESS_COMPARISON")
        if (sliceUri == null || sliceUri.path == null) {
            return null
        }
        return when (sliceUri.path) {
            "/hello" -> createHelloWorldSlice(sliceUri)
            "/test" -> createTestSlice(sliceUri)
            else -> null
        }
    }

    override fun onCreateSliceProvider() = true

    override fun onMapIntentToUri(intent: Intent): Uri {
        val path = intent.data?.path ?: ""
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(context!!.packageName)
            .path(path)
            .build()
    }

    private fun createHelloWorldSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, ListBuilder.INFINITY) {
            header {
                title = "Hello World"
            }
        }
    }

    private fun createTestSlice(sliceUri: Uri): Slice {
        val activityAction = SliceAction.create(
            PendingIntent.getActivity(
                context, 0,
                MainActivity.getIntent(context!!), 0
            ),
            IconCompat.createWithResource(
                context,
                R.drawable.ic_arrow_forward_black_24dp
            ),
            ListBuilder.ICON_IMAGE,
            "Go to app."
        )
        return list(context!!, sliceUri, SliceHints.INFINITY) {
            setAccentColor(0x7f040047)
            header {
                title = "Test Slice"
                subtitle = "Slice for testing purposes"
                summary = "Welcome to the basic Slice presenter."
            }
            row {
                title = "Example Row"
                subtitle = "Row Subtitle"
                addEndItem(
                    IconCompat.createWithResource(
                        context, R.drawable.ic_arrow_forward_black_24dp
                    ), ListBuilder.ICON_IMAGE
                )
            }
            addAction(activityAction)
        }
    }
}