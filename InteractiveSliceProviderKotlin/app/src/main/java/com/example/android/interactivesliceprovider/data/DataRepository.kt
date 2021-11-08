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

    fun unregisterGridSliceDataCallbacks() {
        dataSource.unregisterGridDataCallbacks()
    }

    fun registerListSliceDataCallback(r: Runnable) {
        dataSource.registerListDataCallback(r)
        dataSource.triggerListDataFetch()
    }

    fun unregisterListSliceDataCallbacks() {
        dataSource.unregisterListDataCallbacks()
    }

    companion object {
        const val TAG = "DataRepository"
    }
}

// Model classes

data class GridData(
    val title: String,
    val subtitle: String,
    val home: String,
    val work: String,
    val school: String
)

data class ListData(
    val home: String,
    val work: String,
    val school: String
)