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
package com.example.android.interactivesliceprovider.data

import android.content.ContentResolver
import android.net.Uri
import com.example.android.interactivesliceprovider.InteractiveSliceProvider
import com.example.android.interactivesliceprovider.slicebuilders.GridSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.ListSliceBuilder

/**
 * This class defines an API for fetching data and registering callbacks for a data source which
 * implements the [DataSource] interface like our [FakeDataSource] field [dataSource] does.
 *
 * @param dataSource the [FakeDataSource] that we relay calls to our methods to.
 */
class DataRepository(private val dataSource: FakeDataSource) {

    /**
     * Retrieves the current [GridData] instance that our [FakeDataSource] field [dataSource] stores
     * in its [FakeDataSource.gridData] field. It starts out as an "empty" instance then a runnable
     * posted by the [FakeDataSource.triggerGridDataFetch] method of [dataSource] sets it to its
     * [FakeDataSource.fakeGridData] field after a delay of 1500 milliseconds (to simulate network
     * delay).
     */
    fun getGridData() = dataSource.gridData

    /**
     * Retrieves the current [ListData] instance that our [FakeDataSource] field [dataSource] stores
     * in its [FakeDataSource.listData] field. It starts out as an "empty" instance then a runnable
     * posted by the [FakeDataSource.triggerListDataFetch] method of [dataSource] sets it to its
     * [FakeDataSource.fakeListData] field after a delay of 1500 milliseconds (to simulate network
     * delay).
     */
    fun getListData() = dataSource.listData

    /**
     * Registers its [Runnable] parameter [r] as a callback that will be called when the delayed
     * [Runnable] posted by the [FakeDataSource.triggerGridDataFetch] method runs (delayed for 1500
     * milliseconds to simulate network access), then calls the [FakeDataSource.triggerGridDataFetch]
     * method which posts a [Runnable] that delays 1500 milliseconds, sets its [FakeDataSource.gridData]
     * field to its [FakeDataSource.fakeGridData] field, and then runs all the [Runnable] instances
     * in its [MutableSet] of [Runnable] field [FakeDataSource.gridDataCallbacks] (which its method
     * [FakeDataSource.registerGridDataCallback] has been adding [Runnable]'s to).
     *
     * This is called by the [InteractiveSliceProvider.onSlicePinned] method when it is called to
     * inform our app that a slice has been pinned. Pinning is a way that slice hosts use to notify
     * apps of which slices they care about updates for. When a slice is pinned the content is
     * expected to be relatively fresh and kept up to date. [InteractiveSliceProvider.onSlicePinned]
     * is called with the [Uri] of the slice that was pinned, and the [Runnable] that it registers
     * for that [Uri] calls the [ContentResolver.notifyChange] method with that [Uri] to notify
     * registered observers that a row was updated and attempt to sync changes to the network.
     *
     * @param r the [Runnable] we are to have the [FakeDataSource.registerGridDataCallback] method
     * add to its [MutableSet] of [Runnable] field [FakeDataSource.gridDataCallbacks].
     */
    fun registerGridSliceDataCallback(r: Runnable) {
        dataSource.registerGridDataCallback(r)
        dataSource.triggerGridDataFetch()
    }

    /**
     * Calls the [FakeDataSource.unregisterGridDataCallbacks] method of our [dataSource] field to
     * have it remove all the entries in its [MutableSet] of [Runnable] field `gridDataCallbacks`.
     */
    fun unregisterGridSliceDataCallbacks() {
        dataSource.unregisterGridDataCallbacks()
    }

    /**
     * Registers its [Runnable] parameter [r] as a callback that will be called when the delayed
     * [Runnable] posted by the [FakeDataSource.triggerListDataFetch] method runs (delayed for 1500
     * milliseconds to simulate network access), then calls the [FakeDataSource.triggerListDataFetch]
     * method which posts a [Runnable] that delays 1500 milliseconds, sets its [FakeDataSource.listData]
     * field to its [FakeDataSource.fakeListData] field, and then runs all the [Runnable] instances
     * in its [MutableSet] of [Runnable] field [FakeDataSource.listDataCallbacks] (which its method
     * [FakeDataSource.registerListDataCallback] has been adding [Runnable]'s to).
     *
     * This is called by the [InteractiveSliceProvider.onSlicePinned] method when it is called to
     * inform our app that a slice has been pinned. Pinning is a way that slice hosts use to notify
     * apps of which slices they care about updates for. When a slice is pinned the content is
     * expected to be relatively fresh and kept up to date. [InteractiveSliceProvider.onSlicePinned]
     * is called with the [Uri] of the slice that was pinned, and the [Runnable] that it registers
     * for that [Uri] calls the [ContentResolver.notifyChange] method with that [Uri] to notify
     * registered observers that a row was updated and attempt to sync changes to the network.
     *
     * @param r the [Runnable] we are to have the [FakeDataSource.registerListDataCallback] method
     * add to its [MutableSet] of [Runnable] field [FakeDataSource.listDataCallbacks].
     */
    fun registerListSliceDataCallback(r: Runnable) {
        dataSource.registerListDataCallback(r)
        dataSource.triggerListDataFetch()
    }

    /**
     * Calls the [FakeDataSource.unregisterListDataCallbacks] method of our [dataSource] field to
     * have it remove all the entries in its [MutableSet] of [Runnable] field `listDataCallbacks`.
     */
    fun unregisterListSliceDataCallbacks() {
        dataSource.unregisterListDataCallbacks()
    }

    companion object {
        /**
         * Unused.
         */
        const val TAG = "DataRepository"
    }
}

// Model classes

/**
 * Data class holding data that the [GridSliceBuilder.buildSlice] method uses when it builds its
 * `Slice`
 */
data class GridData(
    /**
     * Used as the title for the header of the `Slice`: "Heavy traffic in your area"
     */
    val title: String,
    /**
     * Used as the subtitle for the header of the `Slice`: "Typical conditions, with delays up to 28 min."
     */
    val subtitle: String,
    /**
     * Used as text in a cell of a `gridRow`, its title: "Home" example: "41 min"
     */
    val home: String,
    /**
     * Used as text in a cell of a `gridRow`, its title: "Work" example: "33 min"
     */
    val work: String,
    /**
     * Used as text in a cell of a `gridRow`, its title: "School" example: "12 min"
      */
    val school: String
)

/**
 * Data class holding data that the [ListSliceBuilder.buildSlice] method uses when it builds its
 * `Slice`
 */
data class ListData(
    /**
     * Used as text in a `row` of a `list`, its title: "Home" example: "41 min"
     */
    val home: String,
    /**
     * Used as text in a `row` of a `list`, its title: "Work" example: "33 min"
     */
    val work: String,
    /**
     * Used as text in a `row` of a `list`, its title: "School" example: "12 min"
     */
    val school: String
)