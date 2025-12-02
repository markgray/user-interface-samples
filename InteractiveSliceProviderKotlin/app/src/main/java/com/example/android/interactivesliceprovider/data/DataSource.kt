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

import com.example.android.interactivesliceprovider.slicebuilders.GridSliceBuilder
import com.example.android.interactivesliceprovider.slicebuilders.ListSliceBuilder

/**
 * Defines the `interface` used by [ListSliceBuilder] and [GridSliceBuilder] to access the data
 * that it uses to build their Slices. It is currently only implemented by [FakeDataSource] but
 * could be implemented by data sources supplying real data without changing the slice building
 * code (theoretically).
 */
interface DataSource {
    /**
     * Data class holding data that the [GridSliceBuilder.buildSlice] method uses when it builds
     * its `Slice`
     */
    val gridData: GridData

    /**
     * Data class holding data that the [ListSliceBuilder.buildSlice] method uses when it builds
     * its `Slice`. Its members are [String]'s to be used in a `row` of a list.
     */
    val listData: ListData

    /**
     * This function is called to simulate a data fetch for the grid data used by the
     * [GridSliceBuilder]. In a real world application this would be where you make a network
     * request or query a database, but in our case it just randomly modifies the [gridData]
     * field then executes all of the [Runnable] callbacks that have been registered using
     * the [registerGridDataCallback] method.
     */
    fun triggerGridDataFetch()

    /**
     * Registers a [Runnable] to be executed when the grid data changes. The `Runnable`'s
     * [Runnable.run] method will be called when the [triggerGridDataFetch] method is called.
     *
     * @param r the [Runnable] whose [Runnable.run] method should be called when grid data changes.
     */
    fun registerGridDataCallback(r: Runnable)

    /**
     * Unregisters all of the [Runnable] callbacks that have been registered using the
     * [registerGridDataCallback] method. In a real app you might want to take a `Runnable`
     * argument to unregister only a specific callback.
     */
    fun unregisterGridDataCallbacks()

    /**
     * Registers a [Runnable] to be executed when the list data changes. This is used by
     * [ListSliceBuilder] to trigger a slice update when its underlying data is updated by a
     * call to [triggerListDataFetch].
     *
     * @param r the [Runnable] to be executed.
     */
    fun registerListDataCallback(r: Runnable)

    /**
     * Unregisters all of the [Runnable] callbacks that have been registered to be executed when
     * the list data changes.
     */
    fun unregisterListDataCallbacks()

    /**
     * This function is called to simulate a data fetch for the list data used by the
     * [ListSliceBuilder]. In a real world application this would be where you make a network
     * request or query a database, but in our case it just randomly modifies the [listData]
     * field then executes all of the [Runnable] callbacks that have been registered using
     * the [registerListDataCallback] method.
     */
    fun triggerListDataFetch()
}