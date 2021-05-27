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

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * A [WindowInsetsAnimationCompat.Callback] which will request and clear focus on the given view,
 * depending on the [WindowInsetsCompat.Type.ime] visibility state when an IME
 * [WindowInsetsAnimationCompat] has finished.
 *
 * This is primarily used when animating the [WindowInsetsCompat.Type.ime], so that the
 * appropriate view is focused for accepting input from the IME.
 *
 * @param view the view to request/clear focus
 * @param dispatchMode The dispatch mode for this callback.
 *
 * @see WindowInsetsAnimationCompat.Callback.getDispatchMode
 */
class ControlFocusInsetsAnimationCallback(
    private val view: View,
    dispatchMode: Int = DISPATCH_MODE_STOP
) : WindowInsetsAnimationCompat.Callback(dispatchMode) {

    /**
     * Called when the insets change as part of running an animation. Note that even if multiple
     * animations for different types are running, there will only be one progress callback per
     * frame. The [insets] passed as an argument represents the overall state and will include all
     * types, regardless of whether they are animating or not.
     *
     * Note that insets dispatch is hierarchical: It will start at the root of the view hierarchy,
     * and then traverse it and invoke the callback of the specific [View] being traversed. The
     * method may return a modified instance by calling [WindowInsetsCompat.inset] to indicate that
     * a part of the insets have been used to offset or clip its children, and the children shouldn't
     * worry about that part anymore. Furthermore, if [getDispatchMode] returns `DISPATCH_MODE_STOP`,
     * children of this view will not receive the callback anymore. We just return our [insets]
     * parameter to pass it on to our children.
     *
     * @param insets            The current insets.
     * @param runningAnimations The currently running animations.
     * @return The insets to dispatch to the subtree of the hierarchy.
     */
    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        // no-op and return the insets
        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask and WindowInsetsCompat.Type.ime() != 0) {
            // The animation has now finished, so we can check the view's focus state.
            // We post the check because the rootWindowInsets has not yet been updated, but will
            // be in the next message traversal
            view.post {
                checkFocus()
            }
        }
    }

    private fun checkFocus() {
        val imeVisible = ViewCompat.getRootWindowInsets(view)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true
        if (imeVisible && view.rootView.findFocus() == null) {
            // If the IME will be visible, and there is not a currently focused view in
            // the hierarchy, request focus on our view
            view.requestFocus()
        } else if (!imeVisible && view.isFocused) {
            // If the IME will not be visible and our view is currently focused, clear the focus
            view.clearFocus()
        }
    }
}
