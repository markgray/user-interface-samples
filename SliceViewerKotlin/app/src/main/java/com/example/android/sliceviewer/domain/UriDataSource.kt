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

package com.example.android.sliceviewer.domain

import android.net.Uri

/**
 * Abstract interface declaring methods for accessing a database of slice [Uri]s. It serves as the
 * supertype of our [LocalUriDataSource] class which stores the slice [Uri]s in the app's shared
 * preference file.
 */
interface UriDataSource {
    /**
     * Returns the entire [List] of [Uri]s stored in our database.
     *
     * @return the entire [List] of [Uri]s stored in our database.
     */
    fun getAllUris(): List<Uri>

    /**
     * Adds its [Uri] parameter [uri] to our database of slice [Uri]s.
     *
     * @param uri the [Uri] to add to our database of slice [Uri]s.
     */
    fun addUri(uri: Uri)

    /**
     * Removes the slice [Uri] at position [index] in our database of slice [Uri]s.
     *
     * @param index the position of the slice [Uri] in our database of slice [Uri]s that we want to
     * remove.
     */
    fun removeFromPosition(index: Int)
}