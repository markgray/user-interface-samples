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

package com.example.android.sliceviewer.ui.list

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.slice.Slice
import androidx.slice.widget.SliceView
import com.example.android.sliceviewer.domain.LocalUriDataSource
import com.example.android.sliceviewer.domain.UriDataSource
import com.example.android.sliceviewer.ui.ViewModelFactory

/**
 * The [ViewModel] used by [SliceViewerActivity].
 *
 * @param uriDataSource the [UriDataSource] interface used for persistent storage of slice [Uri]s.
 * In our case it is the instance of [LocalUriDataSource] passed to us by our [ViewModelFactory].
 */
class SliceViewModel(
    private val uriDataSource: UriDataSource
) : ViewModel() {

    /**
     * The display mode that should be used for [Slice]s displayed in our [SliceView]s. It is set
     * to one of [SliceView.MODE_LARGE], [SliceView.MODE_SMALL], or [SliceView.MODE_SHORTCUT] by
     * the options menu and observers in [SliceViewerActivity] and [SliceAdapter] take appropriate
     * action when it changes value.
     */
    val selectedMode = MutableLiveData<Int>().apply { value = SliceView.MODE_LARGE }

    /**
     * Retrieves the [List] of slice [Uri]s stored in our persistent storage. It is used to update
     * the dataset of the [SliceAdapter] which feeds slice [Uri]s to our UI's `RecyclerView`. We
     * just return the [List] of slice [Uri]s returned by the [UriDataSource.getAllUris] method of
     * our [uriDataSource] field.
     */
    val slices
        get() = uriDataSource.getAllUris()

    /**
     * Adds its [Uri] parameter [uri] to the [List] of slice [Uri]s stored in our persistent storage.
     * We just call the [UriDataSource.addUri] method of our [uriDataSource] field with our [uri]
     * parameter to have it update our [SharedPreferences] by adding [uri] to the string [Set] it
     * stores all of our [Uri]s in.
     *
     * @param uri the slice [Uri] that we want to add to our dataset.
     */
    fun addSlice(uri: Uri) {
        uriDataSource.addUri(uri)
    }

    /**
     * Removes the slice [Uri] at position [position] in our database of slice [Uri]s. We just call
     * the [UriDataSource.removeFromPosition] method of our [uriDataSource] field with our [position]
     * parameter to have it remove the slice [Uri] at position [position] from the string [Set] it
     * stores all of our [Uri]s in.
     *
     * @param position the position of the slice [Uri] in our database of slice [Uri]s that we want
     * to remove.
     */
    fun removeFromPosition(position: Int) {
        uriDataSource.removeFromPosition(position)
    }
}