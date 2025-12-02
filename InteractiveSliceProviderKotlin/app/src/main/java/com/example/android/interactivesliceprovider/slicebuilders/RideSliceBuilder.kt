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
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.slice.Slice
import androidx.slice.builders.ListBuilder
import androidx.slice.builders.SliceAction
import androidx.slice.builders.header
import androidx.slice.builders.list
import androidx.slice.builders.row
import com.example.android.interactivesliceprovider.InteractiveSliceProvider
import com.example.android.interactivesliceprovider.R
import com.example.android.interactivesliceprovider.SliceActionsBroadcastReceiver
import com.example.android.interactivesliceprovider.R.drawable
import com.example.android.interactivesliceprovider.SliceBuilder
import java.util.concurrent.TimeUnit

/**
 * Demonstrates how to build a "Ride" Slice, which is a common use-case for Slices.
 *
 * This Slice features a header, two rows, and actions to simulate ordering a ride.
 * It uses a [ListBuilder] to structure the content, showcasing how to set titles,
 * subtitles with styled text (using [SpannableString]), and end items with actions.
 *
 * The Slice is designed to be interactive, with actions that trigger a
 * [SliceActionsBroadcastReceiver].
 *
 * @param context The context required to access resources and create intents.
 * @param sliceUri The URI for the Slice being built.
 */
class RideSliceBuilder(
    val context: Context,
    sliceUri: Uri
) : SliceBuilder(sliceUri) {

    /**
     * Builds a slice that represents a ride-sharing service.
     *
     * This slice is constructed as a list and includes:
     *  - A header with the title "Get ride", a subtitle indicating the wait time, a summary
     *  of ride times to work and home, and a primary action to "Get Ride".
     *  - A row for a ride to "Work", showing distance, duration, and price, with a
     *  corresponding action.
     *  - A row for a ride to "Home", showing distance, duration, and price, with a
     *  corresponding action.
     *
     * The slice is configured with an accent color and has a time-to-live (TTL) of 10 seconds.
     * Spannable strings are used to apply custom colors to parts of the subtitles.
     *
     * @return The constructed `Slice` object.
     */
    override fun buildSlice(): Slice {
        val colorSpan = ForegroundColorSpan(-0xf062a8)
        val headerSubtitle = SpannableString("Ride in 4 min").apply {
            setSpan(
                colorSpan, 8, length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val homeSubtitle = SpannableString("12 miles | 12 min | $9.00").apply {
            setSpan(
                colorSpan, 20, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val workSubtitle = SpannableString("44 miles | 1 hour 45 min | $31.41").apply {
            setSpan(
                colorSpan, 27, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        val action = SliceAction.create(
            SliceActionsBroadcastReceiver.getIntent(
                context, InteractiveSliceProvider.ACTION_TOAST, "get ride"
            ),
            IconCompat.createWithResource(context, drawable.ic_car),
            ListBuilder.ICON_IMAGE,
            "Get Ride"
        )
        return list(context, sliceUri, TimeUnit.SECONDS.toMillis(10)) {
            setAccentColor(ContextCompat.getColor(context, R.color.slice_accent_color))
            header {
                title = "Get ride"
                subtitle = headerSubtitle
                summary = "Ride to work in 12 min | Ride home in 1 hour 45 min"
                primaryAction = action
            }
            row {
                title = "Work"
                subtitle = workSubtitle
                addEndItem(
                    SliceAction.create(
                        SliceActionsBroadcastReceiver.getIntent(
                            context, InteractiveSliceProvider.ACTION_TOAST, "work"
                        ),
                        IconCompat.createWithResource(context, drawable.ic_work),
                        ListBuilder.ICON_IMAGE,
                        "Get ride to work"
                    )
                )
            }
            row {
                title = "Home"
                subtitle = homeSubtitle
                addEndItem(
                    SliceAction.create(
                        SliceActionsBroadcastReceiver.getIntent(
                            context, InteractiveSliceProvider.ACTION_TOAST, "home"
                        ),
                        IconCompat.createWithResource(context, drawable.ic_home),
                        ListBuilder.ICON_IMAGE,
                        "Get ride home"
                    )
                )
            }
        }
    }

    companion object {
        /**
         * The tag used for logging in this class.
         */
        const val TAG: String = "ListSliceBuilder"
    }
}