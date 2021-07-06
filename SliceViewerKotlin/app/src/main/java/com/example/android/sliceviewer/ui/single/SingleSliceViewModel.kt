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

package com.example.android.sliceviewer.ui.single

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.slice.widget.SliceView
import com.example.android.sliceviewer.domain.UriDataSource
import com.example.android.sliceviewer.ui.ViewModelFactory

/**
 * This is the [ViewModel] used by [SingleSliceViewerActivity]. It holds a handle to the persistent
 * storage [UriDataSource] interface implementation passed it by [ViewModelFactory] in its field
 * [uriDataSource], and a [MutableLiveData] wrapped [Int] property [selectedMode] that holds the
 * slice display mode that the user selected using the options menu. An observer added to this
 * property in the [SingleSliceViewerActivity.bindSlice] method sets the mode that the [SliceView]
 * in the UI displays to this when it changes value. [SingleSliceViewModel] also contains an [addSlice]
 * method which uses the [UriDataSource.addUri] method of [uriDataSource] to add its [Uri] parameter
 * to the persistent set of slice [Uri]s saved in our shared preferences file.
 *
 * @param uriDataSource the implementation of the [UriDataSource] interface we should use for
 * persistent storage of slice [Uri]s.
 */
class SingleSliceViewModel(
    private val uriDataSource: UriDataSource
) : ViewModel() {

    /**
     * The display mode of the [SliceView] in our UI that the user has selected using the options
     * menu. It is one of:
     *  - [SliceView.MODE_SHORTCUT] slice should be presented as a tappable icon.
     *  - [SliceView.MODE_SMALL] slice should be presented in small format, only top-level
     *  information and actions from the slice are shown.
     *  - [SliceView.MODE_LARGE] slice should be presented in large format, as much or all of the
     *  slice contents are shown
     */
    val selectedMode = MutableLiveData<Int>().apply { value = SliceView.MODE_LARGE }

    /**
     * Uses the [UriDataSource.addUri] method of [uriDataSource] to add its [Uri] parameter [uri]
     * to the persistent set of slice [Uri]s saved in our shared preferences file.
     *
     * @param uri the slice [Uri] we are to add to the persistent set of slice [Uri]s saved in our
     * shared preferences file.
     */
    fun addSlice(uri: Uri) {
        uriDataSource.addUri(uri)
    }
}