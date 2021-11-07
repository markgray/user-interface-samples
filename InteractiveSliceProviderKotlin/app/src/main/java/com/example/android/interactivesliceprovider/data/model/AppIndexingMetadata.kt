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
package com.example.android.interactivesliceprovider.data.model

import com.example.android.interactivesliceprovider.AppIndexingUpdateService
import com.google.firebase.appindexing.FirebaseAppIndex

/**
 * Information about each of the slices of this sample is stored in instances of this class. An
 * index of our slices is returned by the [AppIndexingUpdateService.getIndexableData] in a [List]
 * of [AppIndexingMetadata] to the [AppIndexingUpdateService.onHandleWork] method which calls the
 * [FirebaseAppIndex.update] method with `Indexable` objects built from the [AppIndexingMetadata]
 * in the [List].
 */
data class AppIndexingMetadata(
    /**
     * httpUrl = https://interactivesliceprovider.android.example.com/wifi
     * Becomes the URL of the `Indexable` object to be updated by [FirebaseAppIndex]
     */
    val httpUrl: String,
    /**
     * contentUri = content://com.example.android.interactivesliceprovider/wifi
     * Becomes the Uri of the Slice that represents the `Indexable` object.
     */
    val contentUri: String,
    /**
     * name = Wifi
     * Becomes the name of the content of the `Indexable` object.
     */
    val name: String,
    /**
     * keywords = "wifi", "wifitest1234"
     * Becomes the keywords of the `Indexable` object.
     */
    val keywords: List<String>
)