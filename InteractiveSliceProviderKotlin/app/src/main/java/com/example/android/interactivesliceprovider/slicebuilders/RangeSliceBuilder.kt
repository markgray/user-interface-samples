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

package com.example.android.interactivesliceprovider.slicebuilders

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.SliceAction
import androidx.slice.builders.list
import androidx.slice.builders.range
import com.example.android.interactivesliceprovider.InteractiveSliceProvider
import com.example.android.interactivesliceprovider.SliceActionsBroadcastReceiver
import com.example.android.interactivesliceprovider.R
import com.example.android.interactivesliceprovider.SliceBuilder

/**
 * Demonstrates how to build a "range" slice. A range slice is a slice that contains a component
 * that indicates progress, such as a progress bar or a slider. This is useful for displaying
 * things like download progress, media playback position, or volume levels.
 *
 * This specific implementation creates a slice with a range input that visually represents a
 * download at 75% completion.
 *
 * @param context The context required to build the slice.
 * @param sliceUri The URI for the slice being built.
 * @see SliceBuilder
 */
class RangeSliceBuilder(
    val context: Context,
    sliceUri: Uri
) : SliceBuilder(sliceUri) {

    /**
     * Builds a [Slice] that displays a range/progress bar.
     *
     * This method uses the [list] and [range] builders from the `androidx.slice.builders.ktx`
     * library to construct the Slice. The Slice will contain a single row with a range input,
     * representing a download progress of 75 out of 100.
     *
     * The row includes a title, subtitle, and a primary action that triggers a toast message.
     * An accent color is also set for the Slice.
     *
     * @return The constructed [Slice] object.
     */
    override fun buildSlice(): Slice {
        val icon = IconCompat.createWithResource(context, R.drawable.ic_star_on)
        return list(context, sliceUri, ListBuilder.INFINITY) {
            setAccentColor(ContextCompat.getColor(context, R.color.slice_accent_color))
            range {
                title = "Download progress"
                subtitle = "Download is happening"
                max = 100
                value = 75
                primaryAction = SliceAction.create(
                    SliceActionsBroadcastReceiver.getIntent(
                        context, InteractiveSliceProvider.ACTION_TOAST, "open download"
                    ),
                    icon,
                    ListBuilder.ICON_IMAGE,
                    "Download"
                )
            }
        }
    }

    companion object {
        /**
         * Tag for logging.
         */
        const val TAG: String = "ListSliceBuilder"
    }
}