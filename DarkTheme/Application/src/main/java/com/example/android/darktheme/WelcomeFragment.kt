/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.example.android.darktheme

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Just consists of a `TextView` with a welcome message and two `ImageView` in a vertical
 * `LinearLayout` (does nothing). It is used as the starting contents of the `FrameLayout`
 * with ID [R.id.fragment_layout] in our UI.
 */
class WelcomeFragment : Fragment() {
    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. It is recommended to __only__ inflate the layout in this
     * method and move logic that operates on the returned View to [onViewCreated]. We return the
     * [View] that our [LayoutInflater] parameter [inflater] inflates from our layout file
     * [R.layout.fragment_welcome] using our [ViewGroup] parameter [container] for its `LayoutParams`
     * without attaching to it.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the [Fragment],
     * @param container If non-`null`, this is the parent [View] that the fragment's
     * UI will be attached to. The fragment should not add the view itself, but this
     * can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return Return the [View] for the fragment's UI, or `null`.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    companion object {
        /**
         * TAG used by [MainActivity.showFragment] when adding an instance of [WelcomeFragment] to
         * the activity state.
         */
        const val TAG: String = "VectorDrawableFragmentTag"
    }
}