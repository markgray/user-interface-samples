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
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log
import java.util.Locale

/**
 * This demo consists of a circle which the user can drag around the screen which casts a shadow when
 * dragged, and two [Button]s which allow the user to increase or decrease the resting `elevation` of
 * the circle.
 */
class ElevationDragFragment : Fragment() {
    /**
     * The circular outline provider
     */
    private lateinit var mOutlineProviderCircle: ViewOutlineProvider

    /**
     * The current elevation of the floating view.
     */
    private var mElevation = 0f

    /**
     * The step in elevation when changing the Z value
     */
    private var mElevationStep: Int = 0

    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in
     * the process of being created. As such, you can not rely on things like the activity's
     * content view hierarchy being initialized at this point. If you want to do work once the
     * activity itself is created, see [onActivityCreated].
     *
     * First we call our super's implementation of `onCreate`, then we initialize our [ViewOutlineProvider]
     * field [mOutlineProviderCircle] to a new instance of our [CircleOutlineProvider] class and initialize
     * our [Int] field [mElevationStep] to the raw pixel value of our [R.dimen.elevation_step] "dimen"
     * resource (8dp) given the display metrics of the device we running on.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state. We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mOutlineProviderCircle = CircleOutlineProvider()
        mElevationStep = resources.getDimensionPixelSize(R.dimen.elevation_step)
    }

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. It is recommended to **only** inflate the layout in
     * this method and move logic that operates on the returned View to [onViewCreated].
     *
     * We use our [LayoutInflater] parameter [inflater] to inflate the layout file whose resource ID
     * is [R.layout.ztranslation] using our [ViewGroup] parameter [container] for its
     * `LayoutParams` without attaching to it and use the [View] that [inflater] returns to
     * initialize our variable `val rootView`. This layout file consists of a [DragFrameLayout] root
     * [ViewGroup] holding a 96dp by 96dp [View] with the ID [R.id.circle] whose outline is set to
     * a circle and which the user can drag around the screen, and a horizontal `LinearLayout` which
     * holds "Z+" and "Z-" [Button]s which the user can click to raise and lower the `elevation` of
     * the [R.id.circle] circle.
     *
     * Next we initialize our [View] variable `val floatingShape` by finding the [View] in `rootView`
     * with ID [R.id.circle] (this is the [View] to apply z-translation to) and  then set its
     * [ViewOutlineProvider] to our field [mOutlineProviderCircle] (this defines the shape of the
     * [View]'s shadow), and we set the `clipToOutline` property of `floatingShape` to `true` to
     * have the [View] use its [Outline] to clip the contents of the [View].
     *
     * Next we initialize our [DragFrameLayout] variable `val dragLayout` by finding the [View] in
     * `rootView` with ID [R.id.main_layout] then set its `DragFrameController` to a lambda which
     * animates the `translationZ` property of `floatingShape` by 50f if it is currently `captured`
     * for drag, and by 0f otherwise (if the lambda's argument `captured` is `true` it logs "Drag"
     * and if `false` it logs "Drop"). Then we add `floatingShape` to the list of [View]s that are
     * draggable within the container `dragLayout`.
     *
     * Next we locate the "Z+" [Button] in `rootView` by finding the [View] with ID [R.id.raise_bt]
     * and set its [View.OnClickListener] to a lambda which increments our [mElevation] field by
     * our [mElevationStep] field, logs the new value of [mElevation], and sets the `elevation`
     * property of `floatingShape` to the new [mElevation]. Then we locate the "Z-" [Button] in
     * `rootView` by finding the [View] with ID [R.id.lower_bt] and set its [View.OnClickListener]
     * to a lambda which decrements our [mElevation] field by our [mElevationStep] field (down to
     * a minimum of 0f only), logs the new value of [mElevation], and sets the `elevation` property
     * of `floatingShape` to the new [mElevation].
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
            floatingShape.animate().translationZ(if (captured) 50f else 0f).duration = 100
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
        /**
         * Called to get the provider to populate the Outline. This method will be called by a [View]
         * when its owned Drawables are invalidated, when the [View]'s size changes, or if
         * [View.invalidateOutline] is called explicitly. The input [Outline] paramer [outline] is
         * empty and has an alpha of 1.0f.
         *
         * We just call the [Outline.setOval] method of [outline] to have it set itself to an oval
         * whose left side is at 0, top is at 0, right is at the `width` of [View] parameter [view],
         * and whose bottom is at the height of [view].
         *
         * @param view The view building the outline.
         * @param outline The empty outline to be populated.
         */
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