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

import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit

/**
 * Simple CRUD local data source for persisting list of slice URIs in the app's [SharedPreferences]
 * file. Implements the [UriDataSource] interface.
 *
 * @param sharedPrefs a handle to the [SharedPreferences] for access to the prefences file with the
 * name [SHARED_PREFS_NAME] ("shared_prefs").
 */
class LocalUriDataSource(
    private val sharedPrefs: SharedPreferences
) : UriDataSource {
    /**
     * Returns the entire [List] of [Uri]s stored in our database. In order to construct the [List]
     * that we return we use the [SharedPreferences.getStringSet] method of our [sharedPrefs] field
     * to retrieve the set of [String] values stored in it under the key [KEY_URI], defaulting to
     * an empty set of [String] if none was stored yet. We then use the [map] extension function to
     * execute a lambda on each [String] which parses the [String] into a [Uri], and then chain the
     * resulting [List] to a call to the method [toMutableList] which fills a new [MutableList] with
     * all the elements of that [List] of [Uri]s (which we then return to the caller).
     *
     * @return the entire [List] of [Uri]s stored in our database.
     */
    override fun getAllUris(): MutableList<Uri> {
        return sharedPrefs.getStringSet(
            KEY_URI, setOf<String>()
        )!!.map {
            Uri.parse(it)
        }.toMutableList()
    }

    /**
     * Adds its [Uri] parameter [uri] to our database of slice [Uri]s. We use our [getAllUris] method
     * to retrieve the current [List] of [Uri]s from our [SharedPreferences] file and then use the
     * [apply] extension function to execute a lambda on that [List] which adds our [Uri] parameter
     * [uri] to the [List], calling our [save] method to save the result back to the [SharedPreferences]
     * whose handle is in our [sharedPrefs] field.
     *
     * @param uri the [Uri] to add to our database of slice [Uri]s.
     */
    override fun addUri(uri: Uri) {
        save(getAllUris().apply { add(uri) })
    }

    /**
     * Removes the slice [Uri] at position [index] in our database of slice [Uri]s. We use our
     * [getAllUris] method to retrieve the current [List] of [Uri]s from our [SharedPreferences]
     * file and then use the [apply] extension function to execute a lambda on that [List] which
     * removes the entry at positon [index] in the [List], calling our [save] method to save the
     * result back to the [SharedPreferences] whose handle is in our [sharedPrefs] field.
     *
     * @param index the position of the slice [Uri] in our database of slice [Uri]s that we want to
     * remove.
     */
    override fun removeFromPosition(index: Int) {
        save(getAllUris().apply { removeAt(index) })
    }

    /**
     * Saves its [List] of [Uri]s parameter [list] in our shared preferences file under the key
     * [KEY_URI]. We use the [SharedPreferences.edit] method of our field [sharedPrefs] to create a
     * [SharedPreferences.Editor] and use it to execute the [SharedPreferences.Editor.putStringSet]
     * method to store under the key [KEY_URI] a [Set] of [String]s constructed by using the [map]
     * extension function to convert each of the [Uri]s in [list] to a [String], and then using the
     * [toSet] method to convert the resulting list of [String]s to a [Set] of [String]s.
     *
     * @param list the [List] of [Uri]s to save to our shared preferences file database.
     */
    private fun save(list: List<Uri>) {
        sharedPrefs.edit {
            putStringSet(KEY_URI, list.map { it.toString() }.toSet())
        }
    }

    companion object {
        /**
         * The name of the shared preferences file we use for our database.
         */
        const val SHARED_PREFS_NAME: String = "shared_prefs"

        /**
         * The name of the preference under which we store the set of [String] values that serves as
         * our database of slice [Uri]s.
         */
        const val KEY_URI: String = "com.example.android.sliceviewer.sliceuris"
    }
}