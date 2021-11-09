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
    val gridData: GridData
    val listData: ListData
    fun triggerGridDataFetch()
    fun registerGridDataCallback(r: Runnable)
    fun unregisterGridDataCallbacks()
    fun registerListDataCallback(r: Runnable)
    fun unregisterListDataCallbacks()
    fun triggerListDataFetch()
}