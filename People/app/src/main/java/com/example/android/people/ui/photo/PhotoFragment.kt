/*
 * Copyright (C) 2019 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.people.ui.photo

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.transition.Fade
import android.view.View
import androidx.annotation.DrawableRes
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.android.people.R
import com.example.android.people.databinding.PhotoFragmentBinding
import com.example.android.people.getNavigationController
import com.example.android.people.ui.viewBindings

/**
 * Shows the specified [DrawableRes] as a full-screen photo.
 */
class PhotoFragment : Fragment(R.layout.photo_fragment) {

    companion object {
        /**
         * The argument key for the photo URI.
         */
        private const val ARG_PHOTO = "photo"

        /**
         * Creates a new instance of [PhotoFragment] to show the specified photo.
         *
         * @param photo The URI of the photo to show.
         * @return A new instance of [PhotoFragment].
         */
        fun newInstance(photo: Uri): PhotoFragment = PhotoFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_PHOTO, photo)
            }
        }
    }

    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. It just sets a fade-in enter transition for the fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = Fade()
    }

    /**
     * Called when the fragment's view has been created.
     * This method retrieves the photo URI from the fragment's arguments,
     * hides the app bar, and then loads the photo into an ImageView using Glide.
     * If the photo URI is not found, it pops the fragment from the back stack.
     *
     * @param view The fragment's view.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val photo: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_PHOTO, Uri::class.java)
        } else {
            @Suppress("DEPRECATION") // Needed for VERSION.SDK_INT < TIRAMISU
            arguments?.getParcelable(ARG_PHOTO)
        }
        if (photo == null) {
            if (isAdded) {
                parentFragmentManager.popBackStack()
            }
            return
        }
        getNavigationController().updateAppBar(hidden = true)
        val binding by viewBindings(PhotoFragmentBinding::bind)
        Glide.with(this).load(photo).into(binding.photo)
    }
}
