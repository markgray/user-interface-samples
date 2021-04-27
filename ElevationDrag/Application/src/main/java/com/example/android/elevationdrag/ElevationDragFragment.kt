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
package com.example.android.elevationdrag

import android.graphics.Outline
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log
import java.util.Locale

class ElevationDragFragment : Fragment() {
    /**
     * The circular outline provider
     */
    private var mOutlineProviderCircle: ViewOutlineProvider? = null

    /**
     * The current elevation of the floating view.
     */
    private var mElevation = 0f

    /**
     * The step in elevation when changing the Z value
     */
    private var mElevationStep = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mOutlineProviderCircle = CircleOutlineProvider()
        mElevationStep = resources.getDimensionPixelSize(R.dimen.elevation_step)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.ztranslation, container, false)

        /**
         * Find the [View] to apply z-translation to.
         */
        val floatingShape = rootView.findViewById<View>(R.id.circle)

        /**
         * Define the shape of the [View]'s shadow by setting one of the [Outline]s.
         */
        floatingShape.outlineProvider = mOutlineProviderCircle

        /**
         * Clip the [View] with its outline.
         */
        floatingShape.clipToOutline = true
        val dragLayout = rootView.findViewById<View>(R.id.main_layout) as DragFrameLayout
        dragLayout.setDragFrameController { captured ->
            /**
             * Animate the translation of the [View]. Note that the translation
             * is being modified, not the elevation.
             */
            floatingShape.animate()
                .translationZ(if (captured) 50f else 0f).duration = 100
            Log.d(TAG, if (captured) "Drag" else "Drop")
        }
        dragLayout.addDragView(floatingShape)

        /**
         * Raise the circle in z when the "z+" button is clicked.
         */
        rootView.findViewById<View>(R.id.raise_bt).setOnClickListener {
            mElevation += mElevationStep.toFloat()
            Log.d(TAG, String.format(Locale.US, "Elevation: %.1f", mElevation))
            floatingShape.elevation = mElevation
        }

        /**
         * Lower the circle in z when the "z-" button is clicked.
         */
        rootView.findViewById<View>(R.id.lower_bt).setOnClickListener {
            mElevation -= mElevationStep.toFloat()
            // Don't allow for negative values of Z.
            if (mElevation < 0) {
                mElevation = 0f
            }
            Log.d(TAG, String.format(Locale.US, "Elevation: %.1f", mElevation))
            floatingShape.elevation = mElevation
        }
        return rootView
    }

    /**
     * [ViewOutlineProvider] which sets the outline to be an oval which fits the view bounds.
     */
    private inner class CircleOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setOval(0, 0, view.width, view.height)
        }
    }

    companion object {
        /**
         * TAG used for logging
         */
        const val TAG = "ElevationDragFragment"
    }
}