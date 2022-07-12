/*
 *  Copyright 2018 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.example.android.sliceviewer.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.android.sliceviewer.domain.LocalUriDataSource
import com.example.android.sliceviewer.domain.UriDataSource
import com.example.android.sliceviewer.ui.list.SliceViewModel
import com.example.android.sliceviewer.ui.list.SliceViewerActivity
import com.example.android.sliceviewer.ui.single.SingleSliceViewModel
import com.example.android.sliceviewer.ui.single.SingleSliceViewerActivity

/**
 * This is the [ViewModelProvider.NewInstanceFactory] that is used to create both a
 * [SliceViewModel] view model for [SliceViewerActivity] and a [SingleSliceViewModel]
 * view model for [SingleSliceViewerActivity].
 */
class ViewModelFactory private constructor(
    private val uriDataSource: UriDataSource
) : ViewModelProvider.NewInstanceFactory() {

    /**
     * Creates a new instance of the given [Class]. Where `<T>` is the type parameter for the
     * [ViewModel]. If our [modelClass] parameter is either the same as, or is a superclass or
     * superinterface of, the class [SliceViewModel] we return a new instance of [SliceViewModel]
     * constructed to use our [UriDataSource] field [uriDataSource] for its persistent storage.
     * Else if our [modelClass] parameter is either the same as, or is a superclass or superinterface
     * of, the class [SingleSliceViewModel] we return a new instance of [SingleSliceViewModel]
     * constructed to use our [UriDataSource] field [uriDataSource] for its persistent storage.
     *
     * Otherwise we throw an [IllegalArgumentException]: "Unknown ViewModel class".
     *
     * @param modelClass a [Class] whose instance is requested
     * @return a newly created [ViewModel]
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SliceViewModel::class.java)) {
            return SliceViewModel(uriDataSource) as T
        } else if (modelClass.isAssignableFrom(SingleSliceViewModel::class.java)) {
            return SingleSliceViewModel(uriDataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }

    companion object {

        /**
         * Our singleton instance of [ViewModelFactory].
         */
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: ViewModelFactory? = null

        /**
         * Returns our singleton instance of [ViewModelFactory], constructing it if it is `null`. If
         * our singleton instance of [ViewModelFactory] cache [INSTANCE] is `null` we execute a block
         * synchronized on the java [Class] of [ViewModelFactory]. In this block we first check to
         * see if [INSTANCE] is still `null` and if it is we initialize our [SharedPreferences]
         * variable `val sharedPrefs` to the contents of the [Context.MODE_PRIVATE] preferences file
         * whose name is [LocalUriDataSource.SHARED_PREFS_NAME] ("shared_prefs") and then initialize
         * [INSTANCE] to a new instance of [ViewModelFactory] constructed to use an instance of
         * [LocalUriDataSource] constructed to use `sharedPrefs` as its [SharedPreferences].
         *
         * Now that we know that [INSTANCE] has been initialize we return [INSTANCE] to the caller.
         *
         * @param context the [Context] of the application that owns the activity calling us.
         */
        fun getInstance(context: Context): ViewModelFactory? {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory::class.java) {
                    if (INSTANCE == null) {
                        val sharedPrefs: SharedPreferences = context.getSharedPreferences(
                            LocalUriDataSource.SHARED_PREFS_NAME,
                            Context.MODE_PRIVATE
                        )
                        INSTANCE = ViewModelFactory(LocalUriDataSource(sharedPrefs))
                    }
                }
            }
            return INSTANCE
        }
    }
}