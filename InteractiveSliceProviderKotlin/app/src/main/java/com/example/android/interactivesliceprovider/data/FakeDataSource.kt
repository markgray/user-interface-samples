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

import android.os.Handler

/**
 * A fake data source that provides data for the slice. This class simulates network requests by
 * posting delayed tasks to a [Handler].
 */
class FakeDataSource(private val handler: Handler) : DataSource {
    /**
     * This represents the data that our Slice would show in a grid format. This includes a title,
     * subtitle, and travel times to various locations.
     */
    override var gridData: GridData = GridData(title = "", subtitle = "", home = "", work = "", school = "")

    /**
     * This represents the data that our Slice would show in a list format. This includes travel
     * times to various locations.
     */
    override var listData: ListData = ListData(home = "", work = "", school = "")

    /**
     * This is the fake data that will be used to populate the `gridData` property after a
     * simulated network request. It represents a typical traffic scenario with estimated travel
     * times.
     */
    private val fakeGridData =
        GridData(
            title = "Heavy traffic in your area",
            subtitle = "Typical conditions, with delays up to 28 min.",
            home = "41 min",
            work = "33 min",
            school = "12 min"
        )

    /**
     * A set of callbacks that are invoked when the grid data is updated.
     */
    private val gridDataCallbacks = mutableSetOf<Runnable>()

    /**
     * This is the fake data that will be used to populate the `listData` property after a
     * simulated network request. It represents a typical traffic scenario with estimated travel
     * times.
     */
    private val fakeListData =
        ListData(
            home = "41 min",
            work = "33 min",
            school = "12 min"
        )

    /**
     * A set of callbacks that are invoked when the list data is updated.
     */
    private val listDataCallbacks = mutableSetOf<Runnable>()

    /**
     * Simulates a network request to fetch grid data.
     *
     * After a delay, this function updates the `gridData` with fake data and then
     * invokes all registered callbacks to notify observers of the change.
     */
    override fun triggerGridDataFetch() {
        handler.postDelayed({
            gridData = fakeGridData
            gridDataCallbacks.forEach { it.run() }
        }, 1_500L)
    }

    /**
     * Registers a callback to be invoked when grid data is updated.
     *
     * @param r The [Runnable] to be executed when the data changes.
     */
    override fun registerGridDataCallback(r: Runnable) {
        gridDataCallbacks.add(r)
    }

    /**
     * Unregisters all callbacks that were previously registered to listen for grid data updates.
     * This is typically called when the observer is no longer interested in updates, for example,
     * when the associated UI component is destroyed.
     */
    override fun unregisterGridDataCallbacks() {
        gridDataCallbacks.clear()
    }

    /**
     * Simulates a network request to fetch the list data.
     *
     * After a delay, this function updates the [listData] with [fakeListData] and then
     * invokes all registered callbacks to notify observers of the data change.
     */
    override fun triggerListDataFetch() {
        handler.postDelayed({
            listData = fakeListData
            listDataCallbacks.forEach { it.run() }
        }, 1_500L)
    }

    /**
     * Registers a callback to be invoked when the list data is updated.
     *
     * @param r The callback to be invoked.
     */
    override fun registerListDataCallback(r: Runnable) {
        listDataCallbacks.add(r)
    }

    /**
     * Unregisters all callbacks that were previously registered to listen for list data updates.
     * This is typically called when the observer is no longer interested in updates, for example,
     * when the associated UI component is destroyed.
     */
    override fun unregisterListDataCallbacks() {
        listDataCallbacks.clear()
    }
}