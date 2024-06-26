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
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat

/**
 * A class which extends/implements both [WindowInsetsAnimationCompat.Callback] and
 * [View.OnApplyWindowInsetsListener], which should be set on the root view in your layout.
 *
 * This class enables the root view is selectively defer handling any insets which match
 * [deferredInsetTypes], to enable better looking [WindowInsetsAnimationCompat]s.
 *
 * An example is the following: when a [WindowInsetsAnimationCompat] is started, the system will dispatch
 * a [WindowInsetsCompat] instance which contains the end state of the animation. For the scenario of
 * the IME being animated in, that means that the insets contains the IME height. If the view's
 * [View.OnApplyWindowInsetsListener] simply always applied the combination of
 * [WindowInsetsCompat.Type.ime] and [WindowInsetsCompat.Type.systemBars] using padding, the viewport of any
 * child views would then be smaller. This results in us animating a smaller (padded-in) view into
 * a larger viewport. Visually, this results in the views looking clipped.
 *
 * This class allows us to implement a different strategy for the above scenario, by selectively
 * deferring the [WindowInsetsCompat.Type.ime] insets until the [WindowInsetsAnimationCompat] is ended.
 * For the above example, you would create a [RootViewDeferringInsetsCallback] like so:
 *
 * ```
 * val callback = RootViewDeferringInsetsCallback(
 *     persistentInsetTypes = WindowInsetsCompat.Type.systemBars(),
 *     deferredInsetTypes = WindowInsetsCompat.Type.ime()
 * )
 * ```
 *
 * This class is not limited to just IME animations, and can work with any [WindowInsetsCompat.Type]s.
 *
 * @param persistentInsetTypes the bitmask of any inset types which should always be handled
 * through padding the attached view
 * @param deferredInsetTypes the bitmask of insets types which should be deferred until after
 * any related [WindowInsetsAnimationCompat]s have ended
 */
class RootViewDeferringInsetsCallback(
    val persistentInsetTypes: Int,
    val deferredInsetTypes: Int
) : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE),
    OnApplyWindowInsetsListener {
    init {
        require(persistentInsetTypes and deferredInsetTypes == 0) {
            "persistentInsetTypes and deferredInsetTypes can not contain any of " +
                " same WindowInsetsCompat.Type values"
        }
    }

    /**
     * The [View] applying window insets that is passed to our [onApplyWindowInsets] override.
     */
    private var view: View? = null

    /**
     * The [WindowInsetsCompat] insets to apply that is passed to our [onApplyWindowInsets] override.
     */
    private var lastWindowInsets: WindowInsetsCompat? = null

    /**
     * Flag to indicate whether there are deferred insets for our [onEnd] override to apply when
     * the insets animation has ended. Set to `true` in our [onPrepare] override if the `typeMask`
     * of the [WindowInsetsAnimationCompat] passed it has at least one bit set in it which is also
     * set in our [deferredInsetTypes] property. Set back to `false` in our [onEnd] override.
     */
    private var deferredInsets = false

    /**
     * Part of the [OnApplyWindowInsetsListener] interface. When this class is set as the
     * [OnApplyWindowInsetsListener] of a [View] using the [ViewCompat.setOnApplyWindowInsetsListener]
     * method, this method will be called instead of the view's own [View.onApplyWindowInsets] method.
     *
     * First we save our [View] parameter [v] in our field [view] and our [WindowInsetsCompat] parameter
     * [windowInsets] in our field [lastWindowInsets] for later use in our [onEnd] override. Then we
     * initialize our [Int] variable `val types` to [persistentInsetTypes] if [deferredInsets] is
     * `true` (when the deferred flag is enabled, we only use the systemBars() insets) or to the
     * combination of the the systemBars() and ime() insets (bitwise `or` of [persistentInsetTypes]
     * and [deferredInsetTypes]) if it is `false`. We initialize our [Insets] variable `val typeInsets`
     * to the value returned by the [WindowInsetsCompat.getInsets] method of our parameter [windowInsets]
     * for the inset types specified by `types` and we apply the resolved insets by setting them as
     * padding for our [View] parameter [v]. Finally we return the new [WindowInsetsCompat.CONSUMED]
     * to stop the insets being dispatched any further into the view hierarchy (this is a
     * [WindowInsetsCompat] instance whose [WindowInsetsCompat.isConsumed] returns `true` and replaces
     * the deprecated [WindowInsetsCompat.consumeSystemWindowInsets] and related functions).
     *
     * @param v The view applying window insets
     * @param windowInsets The insets to apply
     * @return The insets supplied, minus any insets that were consumed
     */
    override fun onApplyWindowInsets(
        v: View,
        windowInsets: WindowInsetsCompat
    ): WindowInsetsCompat {
        // Store the view and insets for us in onEnd() below
        view = v
        lastWindowInsets = windowInsets

        val types: Int = when {
            // When the deferred flag is enabled, we only use the systemBars() insets
            deferredInsets -> persistentInsetTypes
            // Otherwise we handle the combination of the the systemBars() and ime() insets
            else -> persistentInsetTypes or deferredInsetTypes
        }

        // Finally we apply the resolved insets by setting them as padding
        val typeInsets: Insets = windowInsets.getInsets(types)
        v.setPadding(typeInsets.left, typeInsets.top, typeInsets.right, typeInsets.bottom)

        // We return the new WindowInsetsCompat.CONSUMED to stop the insets being dispatched any
        // further into the view hierarchy. This replaces the deprecated
        // WindowInsetsCompat.consumeSystemWindowInsets() and related functions.
        return WindowInsetsCompat.CONSUMED
    }

    /**
     * Part of the abstract [WindowInsetsAnimationCompat.Callback] class. Called when an insets
     * animation is about to start and before the views have been re-laid out due to an animation.
     * If one of the bits in our field [deferredInsetTypes] is set in the mask returned by the method
     * [WindowInsetsAnimationCompat.getTypeMask] (aka kotlin `typeMask` property) of our parameter
     * [animation] we set our field [deferredInsets] to `true` (We defer the IME insets if the IME
     * is currently not visible. This results in only the [WindowInsetsCompat.Type.systemBars] being
     * applied, allowing the scrolling view to remain at it's larger size).
     *
     * @param animation The animation that is about to start.
     */
    override fun onPrepare(animation: WindowInsetsAnimationCompat) {
        if (animation.typeMask and deferredInsetTypes != 0) {
            // We defer the WindowInsetsCompat.Type.ime() insets if the IME is currently not visible.
            // This results in only the WindowInsetsCompat.Type.systemBars() being applied, allowing
            // the scrolling view to remain at it's larger size.
            deferredInsets = true
        }
    }

    /**
     * Part of the abstract [WindowInsetsAnimationCompat.Callback] class. Called when the insets
     * change as part of running an animation. This is a no-op. We don't actually want to handle any
     * [WindowInsetsAnimationCompat] animations.
     *
     * @param insets The current [WindowInsetsCompat] insets.
     * @param runningAnims The currently running animations.
     * @return The [WindowInsetsCompat] insets to dispatch to the subtree of the hierarchy.
     */
    override fun onProgress(
        insets: WindowInsetsCompat,
        runningAnims: List<WindowInsetsAnimationCompat>
    ): WindowInsetsCompat {
        // This is a no-op. We don't actually want to handle any WindowInsetsAnimations
        return insets
    }

    /**
     * Part of the abstract [WindowInsetsAnimationCompat.Callback] class. Called when an insets
     * animation has ended. If our field [deferredInsets] is `true` (we have deferred IME insets)
     * and one of the bits in our field [deferredInsetTypes] is set in the mask returned by the method
     * [WindowInsetsAnimationCompat.getTypeMask] (aka kotlin `typeMask` property) of our parameter
     * [animation] we set [deferredInsets] to `false` (we deferred the IME insets and an IME animation
     * has finished, so we need to reset the flag), then if our [WindowInsetsCompat] field
     * [lastWindowInsets] is not `null` and our [View] field [view] is not `null` we use the
     * [ViewCompat.dispatchApplyWindowInsets] method to dispatch the deferred insets in [lastWindowInsets]
     * to [view] now.
     *
     * @param animation The animation that has ended. This will be the same instance as passed into
     * [onStart].
     */
    override fun onEnd(animation: WindowInsetsAnimationCompat) {
        if (deferredInsets && (animation.typeMask and deferredInsetTypes) != 0) {
            // If we deferred the IME insets and an IME animation has finished, we need to reset
            // the flag
            deferredInsets = false

            // And finally dispatch the deferred insets to the view now.
            // Ideally we would just call view.requestApplyInsets() and let the normal dispatch
            // cycle happen, but this happens too late resulting in a visual flicker.
            // Instead we manually dispatch the most recent WindowInsets to the view.
            if (lastWindowInsets != null && view != null) {
                ViewCompat.dispatchApplyWindowInsets(view ?: return, lastWindowInsets ?: return)
            }
        }
    }
}
