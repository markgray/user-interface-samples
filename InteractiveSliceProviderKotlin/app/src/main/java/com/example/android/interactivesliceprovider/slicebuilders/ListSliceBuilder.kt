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
import com.example.android.interactivesliceprovider.SliceBuilder
import com.example.android.interactivesliceprovider.data.DataRepository

/**
 * Demonstrates how to build a list slice.
 *
 * A list slice is ideal for displaying multiple related items, such as a list of destinations
 * with their corresponding travel times. This builder creates a slice with a header and several
 * rows, each representing a destination.
 *
 * The slice is configured to show a loading state for subtitles while data is being fetched
 * asynchronously.
 *
 * @param context The context required to build the slice.
 * @param sliceUri The URI identifying the slice.
 * @param repo The data repository used to fetch destination data.
 */
class ListSliceBuilder(
    val context: Context,
    sliceUri: Uri,
    val repo: DataRepository
) : SliceBuilder(sliceUri) {

    /**
     * Builds a `Slice` that displays a list of destinations and the travel times to them.
     *
     * This method fetches destination data from the repository and uses the `list` builder
     * to construct the slice. It includes:
     *  - A header with a title, subtitle, and a primary action.
     *  - Three rows, each representing a destination (Work, Home, School).
     *  - Each row displays the destination name, the travel time (as a subtitle), and an icon.
     *  - The subtitles are configured to show a loading state if the travel time data is not yet
     *  available, providing a better user experience for asynchronous data loading.
     *
     * @return The constructed `Slice` object.
     */
    override fun buildSlice(): Slice {
        val listData = repo.getListData()
        return list(context, sliceUri, 6_000) {
            header {
                title = "Times to Destinations"
                subtitle = "List Slice Type"
                primaryAction = SliceAction.create(
                    SliceActionsBroadcastReceiver.getIntent(
                        context,
                        InteractiveSliceProvider.ACTION_TOAST,
                        "Primary Action for List Slice"
                    ),
                    IconCompat.createWithResource(context, R.drawable.ic_work),
                    ListBuilder.ICON_IMAGE,
                    "Primary"
                )
            }
            row {
                title = "Work"
                // Second argument for subtitle informs system we are waiting for data to load.
                setSubtitle(listData.work, listData.work.isEmpty())
                addEndItem(
                    IconCompat.createWithResource(context, R.drawable.ic_work),
                    ListBuilder.ICON_IMAGE
                )
            }
            row {
                title = "Home"
                // Second argument for subtitle informs system we are waiting for data to load.
                setSubtitle(listData.home, listData.home.isEmpty())
                addEndItem(
                    IconCompat.createWithResource(context, R.drawable.ic_home),
                    ListBuilder.ICON_IMAGE
                )
            }
            row {
                title = "School"
                // Second argument for subtitle informs system we are waiting for data to load.
                setSubtitle(listData.school, listData.school.isEmpty())
                addEndItem(
                    IconCompat.createWithResource(context, R.drawable.ic_school),
                    ListBuilder.ICON_IMAGE
                )
            }
        }
    }

    companion object {
        /**
         * Tag to be used for logging purposes.
         */
        const val TAG: String = "ListSliceBuilder"
    }
}