/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.example.android.elevationbasic

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log

/**
 * This sample uses two shapes, a circle and a square, and it demonstrates two alternative ways to
 * move a view in the z-axis. The first shape, the circle, has a fixed elevation, which is defined
 * in XML. The second view, the square, changes its elevation using `setTranslationZ` when a user
 * touches it. The elevation reverts back once the touch is removed.
 */
class ElevationBasicFragment : Fragment() {
    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in
     * the process of being created. As such, you can not rely on things like the activity's
     * content view hierarchy being initialized at this point. If you want to do work once the
     * activity itself is created, see [onActivityCreated].
     *
     * First we call our super's implementation of `onCreate`, then we call the [setHasOptionsMenu]
     * method with `true` to report that this fragment would like to participate in populating the
     * options menu by receiving a call to [onCreateOptionsMenu] and related methods.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state. We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION") // TODO: Use MenuProvider
        setHasOptionsMenu(true)
    }

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. It is recommended to **only** inflate the layout in
     * this method and move logic that operates on the returned View to [onViewCreated].
     *
     * We use our [LayoutInflater] parameter [inflater] to inflate the layout file whose resource ID
     * is `R.layout.elevation_basic` using our [ViewGroup] parameter [container] for its
     * `LayoutParams` without attaching to it and use the [View] that [inflater] returns to
     * initialize our variable `val rootView`, then initialize our [View] variable `val shape2` by
     * finding the view in `rootView` with ID `R.id.floating_shape_2`. Then we set the
     * [OnTouchListener] of `shape2` to an anonymous class which branches on the value of the masked
     * action being performed by the [MotionEvent] `shape2` received:
     *  - [MotionEvent.ACTION_DOWN] (a pressed gesture has started) it logs the message "ACTION_DOWN
     *  on view." and sets the `translationZ` property of the `view` that was touched to 120f. It
     *  then returns `true` to consume the event.
     *  - [MotionEvent.ACTION_UP] (a pressed gesture has finished) it logs the message "ACTION_UP on
     *  view." and sets the `translationZ` property of the `view` that was touched to 0f. It then
     *  returns `true` to consume the event.
     *  - for all other [MotionEvent] actions it returns `false` to report it did not consume the
     *  event.
     *
     * Having set up our UI we return `rootView` to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment,
     * @param container If non-`null`, this is the parent view that the fragment's UI will be
     * attached to. The fragment should not add the view itself, but this can be used to generate
     * the `LayoutParams` of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed from a
     * previous saved state as given here.
     * @return Return the [View] for the fragment's UI, or `null`.
     */
    @SuppressLint("ClickableViewAccessibility") // Elevation is only visible if you can see?
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Inflates an XML containing two shapes: the first has a fixed elevation
         * and the second ones raises when tapped.
         */
        val rootView = inflater.inflate(R.layout.elevation_basic, container, false)
        val shape2 = rootView.findViewById<View>(R.id.floating_shape_2)
        /**
         * Sets a [View.OnTouchListener] that responds to a touch event on `shape2`.
         *
         * `view` -> The [View] the touch event has been dispatched to.
         * `motionEvent` -> The [MotionEvent] object containing full information about the event.
         * Return `true` if the listener has consumed the event, `false` otherwise.
         */
        shape2.setOnTouchListener(OnTouchListener { view, motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "ACTION_DOWN on view.")
                    view.translationZ = 120f
                }

                MotionEvent.ACTION_UP -> {
                    Log.d(TAG, "ACTION_UP on view.")
                    view.translationZ = 0f
                }

                else -> return@OnTouchListener false
            }
            true
        })
        return rootView
    }

    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "ElevationBasicFragment"
    }
}
