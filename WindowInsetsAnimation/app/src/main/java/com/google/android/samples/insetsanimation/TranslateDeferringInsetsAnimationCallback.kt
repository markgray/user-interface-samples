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
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * A [WindowInsetsAnimationCompat.Callback] which will translate/move the given view during any
 * inset animations of the given inset type.
 *
 * This class works in tandem with [RootViewDeferringInsetsCallback] to support the deferring of
 * certain [WindowInsetsCompat.Type] values during a [WindowInsetsAnimationCompat], provided in
 * [deferredInsetTypes]. The values passed into this constructor should match those which
 * the [RootViewDeferringInsetsCallback] is created with.
 *
 * @param view the view to translate from it's start to end state
 * @param persistentInsetTypes the bitmask of any inset types which were handled as part of the
 * layout
 * @param deferredInsetTypes the bitmask of insets types which should be deferred until after
 * any [WindowInsetsAnimationCompat]s have ended
 * @param dispatchMode The dispatch mode for this callback.
 * See [WindowInsetsAnimationCompat.Callback.getDispatchMode].
 */
class TranslateDeferringInsetsAnimationCallback(
    private val view: View,
    val persistentInsetTypes: Int,
    val deferredInsetTypes: Int,
    dispatchMode: Int = DISPATCH_MODE_STOP
) : WindowInsetsAnimationCompat.Callback(dispatchMode) {
    init {
        require(persistentInsetTypes and deferredInsetTypes == 0) {
            "persistentInsetTypes and deferredInsetTypes can not contain any of " +
                    " same WindowInsetsCompat.Type values"
        }
    }

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
     * children of this view will not receive the callback anymore.
     *
     * First we initialize our [Insets] variable `val typesInset` to the insets which are potentially
     * deferred by calling the [WindowInsetsCompat.getInsets] method of our parameter [insets] with
     * our [deferredInsetTypes] mask of insets types which should be deferred until after any
     * [WindowInsetsAnimationCompat]s have ended. Then we initialize our [Insets] variable
     * `val otherInset` to the insets which are applied as padding during layout by calling the
     * [WindowInsetsCompat.getInsets] method of our parameter [insets] with our [persistentInsetTypes]
     * mask of insets types which were handled as part of the layout.
     *
     * We initialize our [Insets] variable `val diff` by using the [Insets.subtract] method to
     * subtract `otherInset` from `typesInset` and then using the `let` extension function to make
     * sure that each of the four inset values are greater than or equal to 0. The resulting `diff`
     * insets contain the values for us to apply as a translation to the view which we do by setting
     * the [View.setTranslationX] (kotlin `translationX` property) of [view] to the [Insets.left]
     * of `diff` minus the [Insets.right] of `diff`, and setting the [View.setTranslationY] (kotlin
     * `translationY` property) of [view] to the [Insets.top] of `diff` minus the [Insets.bottom]
     * of `diff`. Finally we return [insets] to the caller to have it dispatch them to the subtree
     * of the hierarchy.
     *
     * @param insets The current insets.
     * @param runningAnimations The currently running animations.
     * @return The insets to dispatch to the subtree of the hierarchy.
     */
    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnimations: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        // onProgress() is called when any of the running animations progress...

        // First we get the insets which are potentially deferred
        val typesInset: Insets = insets.getInsets(deferredInsetTypes)
        // Then we get the persistent inset types which are applied as padding during layout
        val otherInset: Insets = insets.getInsets(persistentInsetTypes)

        // Now that we subtract the two insets, to calculate the difference. We also coerce
        // the insets to be >= 0, to make sure we don't use negative insets.
        val diff: Insets = Insets.subtract(typesInset, otherInset).let {
            Insets.max(it, Insets.NONE)
        }

        // The resulting `diff` insets contain the values for us to apply as a translation
        // to the view
        view.translationX = (diff.left - diff.right).toFloat()
        view.translationY = (diff.top - diff.bottom).toFloat()

        return insets
    }

    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        // Once the animation has ended, reset the translation values
        view.translationX = 0f
        view.translationY = 0f
    }
}
