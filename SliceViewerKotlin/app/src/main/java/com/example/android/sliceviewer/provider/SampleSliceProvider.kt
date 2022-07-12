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

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    /**
     * Implemented to create a slice. [onBindSlice] should return as quickly as possible so that
     * the UI tied to this slice can be responsive. No network or other IO will be allowed during
     * [onBindSlice]. Any loading that needs to be done should happen in the background with a call
     * to `ContentResolver.notifyChange` when the app is ready to provide the complete data in
     * [onBindSlice].
     *
     * If our [Uri] parameter [sliceUri] is `null` or its `path` property is `null` we return `null`
     * having done nothing. Otherwise we branch on the `path` property of [sliceUri]:
     *  - "/hello" -> we return the [Slice] created by our [createHelloWorldSlice] method from
     *  [sliceUri] to the caller.
     *  - "/test" -> we return the [Slice] created by our [createTestSlice] method from [sliceUri]
     *  to the caller.
     *  - any other `path` -> we return `null`.
     *
     * @param sliceUri the [Uri] of the [Slice] we are to create.
     * @return the [Slice] corresponding to the slice [Uri] parameter [sliceUri] or `null`.
     */
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

    /**
     * Implement this to initialize your slice provider on startup. This method is called for all
     * registered slice providers on the application main thread at application launch time. It must
     * not perform lengthy operations, or application startup will be delayed.
     *
     * We just return `true` to indicate that we were successfully loaded.
     *
     * @return `true` if the provider was successfully loaded, `false` otherwise
     */
    override fun onCreateSliceProvider(): Boolean = true

    /**
     * This method must be overridden if an [IntentFilter] is specified on the [SliceProvider]. In
     * that case, this method can be called and is expected to return a non-null [Uri] representing
     * a [Slice]. Otherwise this will throw [UnsupportedOperationException].
     *
     * We initialize our [String] variable `val path` to the `path` property of the `data` [Uri] of
     * our [Intent] parameter [intent]. Then we construct a new instance of [Uri.Builder], set its
     * scheme to [ContentResolver.SCHEME_CONTENT] ("content"), set its authority to our application's
     * package name, and set its path to `path`. We build this [Uri.Builder] and return the [Uri]
     * that is created to the caller.
     *
     * @return [Uri] representing the slice associated with the provided [Intent] parameter [intent].
     */
    override fun onMapIntentToUri(intent: Intent): Uri {
        val path: String = intent.data?.path ?: ""
        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(context!!.packageName)
            .path(path)
            .build()
    }

    /**
     * Creates a minimalist [Slice] which consists of only a header displaying the text "Hello World".
     * We use the [list] method found in `androidx.slice.builders.ListBuilder.kt` to construct a
     * `ListBuilderDsl` using the [Context] this provider is running in, our [Uri] parameter [sliceUri],
     * and a time to live of [ListBuilder.INFINITY] (Constant representing infinity) and then apply
     * a lambda to that `ListBuilderDsl` which adds a [header] with the `title`: "Hello World". The
     * [list] method then builds the [ListBuilder] into a [Slice] which we return to the caller.
     *
     * @param sliceUri a [Uri] whose `path` is "/hello".
     * @return a [Slice] consisting only of a header displaying the text "Hello World".
     */
    private fun createHelloWorldSlice(sliceUri: Uri): Slice {
        return list(context!!, sliceUri, ListBuilder.INFINITY) {
            header {
                title = "Hello World"
            }
        }
    }

    /**
     * Creates a [Slice] which consists of a header with the title "Test Slice", subtitle "Slice for
     * testing purposes", and a summary "Welcome to the basic Slice presenter." It also has a row
     * following the header whose title is "Example Row", subtitle is "Row Subtitle", and whose end
     * icon is the drawable with resource ID [R.drawable.ic_arrow_forward_black_24dp]. This [Slice]
     * has an explicit action intended to launch our activity [MainActivity].
     *
     * @param sliceUri the [Uri] for the [Slice] we are to create.
     * @return a [Slice] with the header "Test Slice"
     */
    @SuppressLint("InlinedApi", "RestrictedApi")
    private fun createTestSlice(sliceUri: Uri): Slice {
        val activityAction = SliceAction.create(
            PendingIntent.getActivity(
                context, 0,
                MainActivity.getIntent(context!!),
                PendingIntent.FLAG_IMMUTABLE
            ),
            IconCompat.createWithResource(
                context as Context,
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
                primaryAction = activityAction
            }
            row {
                title = "Example Row"
                subtitle = "Row Subtitle"
                addEndItem(
                    IconCompat.createWithResource(
                        context as Context, R.drawable.ic_arrow_forward_black_24dp
                    ), ListBuilder.ICON_IMAGE
                )
            }
            addAction(activityAction)
        }
    }
}