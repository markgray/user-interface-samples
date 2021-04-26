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

class ElevationBasicFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        /**
         * Inflates an XML containing two shapes: the first has a fixed elevation
         * and the second ones raises when tapped.
         */
        val rootView = inflater.inflate(R.layout.elevation_basic, container, false)
        val shape2 = rootView.findViewById<View>(R.id.floating_shape_2)
        /**
         * Sets a {@Link View.OnTouchListener} that responds to a touch event on shape2.
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