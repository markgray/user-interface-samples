/*
 * Copyright 2020 The Android Open Source Project
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

package com.google.android.samples.insetsanimation

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup

/**
 * A temporary [IntArray] which we use to retrieve the X Y coordinates of a view in its window.
 */
private val tmpIntArr = IntArray(2)

/**
 * Extension function which updates the given [rect] with this view's position and bounds in its
 * window. If `this` view has been through at least one layout since it was last attached to or
 * detached from a window and is currently attached to a window we set our [Rect] parameter [rect]
 * to have its left top at (0,0) and right bottom at the width and height of the [View]. Then we use
 * the [View.getLocationInWindow] method of our [View] to store the X,Y location of the [View] in
 * our temp [IntArray] field [tmpIntArr] and finally use the [Rect.offset] method of [rect] to
 * offset the [Rect] by adding `tmpIntArr[0]` (X coordinate in window) to its left and right
 * coordinates, and adding `tmpIntArr[1]` (Y coordinate in window) to its top and bottom coordinates.
 *
 * If our [View] has not been laid out, or is not currently attached to a window we throw an
 * [IllegalArgumentException] explaining why we cannot copy the bounds.
 *
 * @param rect a [Rect] in which to store the position and bounds of the receiver [View].
 */
fun View.copyBoundsInWindow(rect: Rect) {
    if (isLaidOut && isAttachedToWindow) {
        rect.set(0, 0, width, height)
        getLocationInWindow(tmpIntArr)
        rect.offset(tmpIntArr[0], tmpIntArr[1])
    } else {
        throw IllegalArgumentException(
            "Can not copy bounds as view is not laid out" +
                " or attached to window"
        )
    }
}

/**
 * Provides access to the hidden [ViewGroup.suppressLayout] method. If our device is running SDK 29
 * or greater we just call the [ViewGroup.suppressLayout] method directly, otherwise we have to call
 * our [hiddenSuppressLayout] method.
 *
 * @param suppress if `true` suppress all layout() calls until layout suppression is disabled with a
 * later call to [suppressLayoutCompat] with `false`.
 */
fun ViewGroup.suppressLayoutCompat(suppress: Boolean) {
    if (Build.VERSION.SDK_INT >= 29) {
        suppressLayout(suppress)
    } else {
        hiddenSuppressLayout(this, suppress)
    }
}

/**
 * False when linking of the hidden suppressLayout method has previously failed.
 */
private var tryHiddenSuppressLayout = true

/**
 * Tries to access the hidden [ViewGroup.suppressLayout] method if our [tryHiddenSuppressLayout] is
 * `true`, setting [tryHiddenSuppressLayout] to `false` if it fails the first time we try it.
 */
@SuppressLint("NewApi") // Lint doesn't know about the hidden method.
private fun hiddenSuppressLayout(group: ViewGroup, suppress: Boolean) {
    if (tryHiddenSuppressLayout) {
        // Since this was an @hide method made public, we can link directly against it with
        // a try/catch for its absence instead of doing the same through reflection.
        try {
            group.suppressLayout(suppress)
        } catch (e: NoSuchMethodError) {
            tryHiddenSuppressLayout = false
        }
    }
}
