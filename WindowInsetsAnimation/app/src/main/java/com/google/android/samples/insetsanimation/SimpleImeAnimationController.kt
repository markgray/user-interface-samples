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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.google.android.samples.insetsanimation

import android.os.CancellationSignal
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationControlListenerCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.dynamicanimation.animation.springAnimationOf
import androidx.dynamicanimation.animation.withSpringForceProperties
import kotlin.math.roundToInt

/**
 * A wrapper around the [WindowInsetsAnimationControllerCompat] APIs in AndroidX Core, to simplify
 * the implementation of common use-cases around the IME.
 *
 * See [InsetsAnimationLinearLayout] and [InsetsAnimationTouchListener] for examples of how
 * to use this class.
 */
internal class SimpleImeAnimationController {
    /**
     * The [WindowInsetsAnimationControllerCompat] that we use to animate the IME.
     */
    private var insetsAnimationController: WindowInsetsAnimationControllerCompat? = null

    /**
     * The [CancellationSignal] we use to cancel the controlWindowInsetsAnimation() request which is
     * made in our [startControlRequest] method. Its [CancellationSignal.cancel] method is called by
     * our methods [animateToFinish], [cancel], and [finish].
     */
    private var pendingRequestCancellationSignal: CancellationSignal? = null

    /**
     * This is the lambda passed to our [startControlRequest] method which will be `invoked` in our
     * [onRequestReady] method on our [WindowInsetsAnimationControllerCompat] when the `onReady`
     * override of our [WindowInsetsAnimationControlListenerCompat] field [animationControlListener]
     * is called when the animation is ready to be controlled.
     */
    private var pendingRequestOnReady: ((WindowInsetsAnimationControllerCompat) -> Unit)? = null

    /**
     * To take control of the an WindowInsetsAnimation, we need to pass in a listener to
     * controlWindowInsetsAnimation() in startControlRequest(). The listener created here
     * keeps track of the current WindowInsetsAnimationController and resets our state.
     */
    private val animationControlListener by lazy {
        object : WindowInsetsAnimationControlListenerCompat {
            /**
             * Called when the animation is ready to be controlled. This may be delayed when the IME
             * needs to redraw because of an `EditorInfo` change, or when the window is starting up.
             * Once the request is ready, we call our [onRequestReady] function.
             *
             * @param controller The controller to control the inset animation.
             * @param types The `Type`s it was able to gain control over. Note that this may be
             * different than the types passed into the [WindowInsetsAnimationControllerCompat]
             * method `controlWindowInsetsAnimation` in case the window wasn't able to gain the
             * controls because it wasn't the IME target or not currently the window that's
             * controlling the system bars.
             */
            override fun onReady(
                controller: WindowInsetsAnimationControllerCompat,
                types: Int
            ) = onRequestReady(controller)

            /**
             * Called when the request for control over the insets has finished. If the request is
             * finished we should reset our internal state, which we do by calling our [reset] method.
             *
             * @param controller the controller which has finished.
             */
            override fun onFinished(controller: WindowInsetsAnimationControllerCompat) = reset()

            /**
             * Called when the request for control over the insets has been cancelled, either because
             * the [CancellationSignal] associated with the window insets animation request} has been
             * invoked, or the window has lost control over the insets (e.g. because it lost focus).
             * If the request is cancelled, we should reset our internal state, which we do by calling
             * our [reset] method.
             *
             * @param controller the controller which has been cancelled, or `null` if the request
             * was cancelled before [onReady] was invoked.
             */
            override fun onCancelled(controller: WindowInsetsAnimationControllerCompat?) = reset()
        }
    }

    /**
     * `true` if the IME was shown at the start of the current animation.
     */
    private var isImeShownAtStart = false

    /**
     * [SpringAnimation] that our [animateImeToVisibility] method uses to animate the IME to it's
     * fully shown state, or to it's fully hidden state.
     */
    private var currentSpringAnimation: SpringAnimation? = null

    /**
     * Start a control request to the [view]s [android.view.WindowInsetsController]. This should
     * be called once the view is in a position to take control over the position of the IME. First
     * we [check] that [isInsetAnimationInProgress] is `false` and throw an [IllegalStateException]
     * if it is `true` ("Animation in progress. Can not start a new request..."). Then we initialize
     * our [isImeShownAtStart] field to `true` if the original [WindowInsetsCompat] that was dispatched
     * to the view hierarchy indicates that the IME is visible. We initalize our [CancellationSignal]
     * field [pendingRequestCancellationSignal] to a new instance and save a reference to our
     * [onRequestReady] parameter in our function reference field [pendingRequestOnReady].
     *
     * Finally we make the [WindowInsetsControllerCompat.controlWindowInsetsAnimation] request by
     * retrieving a [WindowInsetsControllerCompat] of the window that our [View] parameter view is
     * attached to and calling its [WindowInsetsControllerCompat.controlWindowInsetsAnimation] method
     * which requests the control of the IME window inset type, with a undetermined duration (a -1 is
     * used to indicate that we're not starting a finite animation, and it will be completely
     * controlled by the user's touch), our [LinearInterpolator] field [linearInterpolator] as the
     * interpolator used for the animation, our [CancellationSignal] field [pendingRequestCancellationSignal]
     * as the cancellation signal that we can use to cancel the request to obtain control, or once
     * we have control, to cancel the control, and our [WindowInsetsAnimationControlListenerCompat]
     * field [animationControlListener] as the listener that will be called when the window is ready
     * to be controlled (its `onReady` callback), along with its callbacks `onFinished` and
     * `onCancelled`.
     *
     * @param view The view which is triggering this request
     * @param onRequestReady optional listener which will be called when the request is ready and
     * the animation can proceed
     */
    fun startControlRequest(
        view: View,
        onRequestReady: ((WindowInsetsAnimationControllerCompat) -> Unit)? = null
    ) {
        check(!isInsetAnimationInProgress()) {
            "Animation in progress. Can not start a new request to controlWindowInsetsAnimation()"
        }

        // Keep track of the IME insets, and the IME visibility, at the start of the request
        isImeShownAtStart = ViewCompat.getRootWindowInsets(view)
            ?.isVisible(WindowInsetsCompat.Type.ime()) == true

        // Create a cancellation signal, which we pass to controlWindowInsetsAnimation() below
        pendingRequestCancellationSignal = CancellationSignal()
        // Keep reference to the onReady callback
        pendingRequestOnReady = onRequestReady

        // Finally we make a controlWindowInsetsAnimation() request:
        ViewCompat.getWindowInsetsController(view)?.controlWindowInsetsAnimation(
            // We're only catering for IME animations in this listener
            WindowInsetsCompat.Type.ime(),
            // Animation duration. This is not used by the system, and is only passed to any
            // WindowInsetsAnimation.Callback set on views. We pass in -1 to indicate that we're
            // not starting a finite animation, and that this is completely controlled by
            // the user's touch.
            -1,
            // The time interpolator used in calculating the animation progress. The fraction value
            // we passed into setInsetsAndAlpha() which be passed into this interpolator before
            // being used by the system to inset the IME. LinearInterpolator is a good type
            // to use for scrolling gestures.
            linearInterpolator,
            // A cancellation signal, which allows us to cancel the request to control
            pendingRequestCancellationSignal,
            // The WindowInsetsAnimationControlListener
            animationControlListener
        )
    }

    /**
     * Start a control request to the [view]s [android.view.WindowInsetsController], similar to
     * [startControlRequest], but immediately fling to a finish using [velocityY] once ready.
     *
     * This function is useful for fire-and-forget operations to animate the IME.
     *
     * It consists of a call to [startControlRequest] with our [View] parameter [view] as the [View]
     * to use to get a [WindowInsetsControllerCompat] and a lambda as the function to call when the
     * controller is ready which calls our [animateToFinish] method with [velocityY] as the velocity
     * to use to determine the direction.
     *
     * @param view The view which is triggering this request
     * @param velocityY the velocity of the touch gesture which caused this call
     */
    fun startAndFling(view: View, velocityY: Float) = startControlRequest(view) {
        animateToFinish(velocityY)
    }

    /**
     * Update the inset position of the IME by the given [dy] value. This value will be coerced
     * into the hidden and shown inset values.
     * This function should only be called if [isInsetAnimationInProgress] returns true.
     *
     * We initialize our [WindowInsetsAnimationControllerCompat] variable `val controller` to our
     * field [insetsAnimationController], throwing [IllegalStateException] if it is `null`. Then we
     * return the value returned by our [insetTo] method when it updates the inset position of the
     * IME to the current bottom inset minus our [Int] parameter [dy].
     *
     * @param dy the number of pixels to change the inset position of the IME by.
     * @return the amount of [dy] consumed by the inset animation, in pixels
     */
    fun insetBy(dy: Int): Int {
        val controller = insetsAnimationController
            ?: throw IllegalStateException(
                "Current WindowInsetsAnimationController is null." +
                        "This should only be called if isAnimationInProgress() returns true"
            )

        // Call updateInsetTo() with the new inset value
        return insetTo(controller.currentInsets.bottom - dy)
    }

    /**
     * Update the inset position of the IME to be the given [inset] value. This value will be
     * coerced into the hidden and shown inset values.
     * This function should only be called if [isInsetAnimationInProgress] returns true.
     *
     * We initialize our [WindowInsetsAnimationControllerCompat] variable `val controller` to our
     * field [insetsAnimationController], throwing [IllegalStateException] if it is `null`. We
     * initialize our [Int] variable `val hiddenBottom` to the bottom inset of the IME inset when
     * the IME is fully hidden, and our [Int] variable `val shownBottom` to the bottom inset of the
     * IME inset when the IME is fully shown. We initialize our [Int] variable `val startBottom` to
     * `shownBottom` if [isImeShownAtStart] is `true` or to `hiddenBottom` if it is `false`. We
     * initialize our [Int] variable `val endBottom` to `hiddenBottom` if [isImeShownAtStart] is
     * `true` or to `shownBottom` if it is `false`. We use the [Int.coerceIn] extension method to
     * coerce our [Int] parameter [inset] to a value between `hiddenBottom` and `shownBottom` and
     * use this value to initialize our [Int] variable `val coercedBottom`. We initialize our [Int]
     * variable `val consumedDy` to the current bottom inset of the IME minus `coercedBottom`.
     *
     * As a last step we use the [WindowInsetsAnimationControllerCompat.setInsetsAndAlpha] method of
     * `controller` to  update the insets of the IME to an [Insets] whose bottom is `coercedBottom`,
     * using an alpha of 1f to avoid altering the alpha, and using as the fraction of the animation
     * progress `coercedBottom` minus `startBottom` divided by `endBottom` minus `startBottom` as a
     * [Float] (this value is passed to any window insets animation callbacks that are interested).
     *
     * Finally we return `consumedDy` to the caller as the distance "consumed" by the call.
     *
     * @param inset the inset position of the IME to move to in pixels.
     * @return the distance moved by the inset animation, in pixels
     */
    fun insetTo(inset: Int): Int {
        val controller = insetsAnimationController
            ?: throw IllegalStateException(
                "Current WindowInsetsAnimationController is null." +
                        "This should only be called if isAnimationInProgress() returns true"
            )

        val hiddenBottom = controller.hiddenStateInsets.bottom
        val shownBottom = controller.shownStateInsets.bottom
        val startBottom = if (isImeShownAtStart) shownBottom else hiddenBottom
        val endBottom = if (isImeShownAtStart) hiddenBottom else shownBottom

        // We coerce the given inset within the limits of the hidden and shown insets
        val coercedBottom = inset.coerceIn(hiddenBottom, shownBottom)

        val consumedDy = controller.currentInsets.bottom - coercedBottom

        // Finally update the insets in the WindowInsetsAnimationController using
        // setInsetsAndAlpha().
        controller.setInsetsAndAlpha(
            // Here we update the animating insets. This is what controls where the IME is displayed.
            // It is also passed through to views via their WindowInsetsAnimation.Callback.
            Insets.of(0, 0, 0, coercedBottom),
            // This controls the alpha value. We don't want to alter the alpha so use 1f
            1f,
            // Finally we calculate the animation progress fraction. This value is passed through
            // to any WindowInsetsAnimation.Callbacks, but it is not used by the system.
            (coercedBottom - startBottom) / (endBottom - startBottom).toFloat()
        )

        return consumedDy
    }

    /**
     * Convenience function to test if our [WindowInsetsAnimationControllerCompat] field
     * [insetsAnimationController] is not `null` which indicates that an inset animation
     * is in progress.
     *
     * @return `true` if an inset animation is in progress.
     */
    fun isInsetAnimationInProgress(): Boolean {
        return insetsAnimationController != null
    }

    /**
     * Convenience function to test if our [SpringAnimation] field [currentSpringAnimation] is not
     * `null` which indicates that an inset animation is currently finishing.
     *
     * @return `true` if an inset animation is currently finishing.
     */
    fun isInsetAnimationFinishing(): Boolean {
        return currentSpringAnimation != null
    }

    /**
     * Convenience function to test if our [CancellationSignal] field [pendingRequestCancellationSignal]
     * is not `null` which indicates that a request to control an inset animation is in progress.
     *
     * @return `true` if a request to control an inset animation is in progress.
     */
    fun isInsetAnimationRequestPending(): Boolean {
        return pendingRequestCancellationSignal != null
    }

    /**
     * Cancel the current [WindowInsetsAnimationControllerCompat]. We immediately finish the
     * animation by calling the [WindowInsetsAnimationControllerCompat.finish] method of our
     * field [insetsAnimationController] if [insetsAnimationController] is not `null`, call the
     * [CancellationSignal.cancel] method of our field [pendingRequestCancellationSignal] if
     * [pendingRequestCancellationSignal] is not `null` to cancel the request to control started
     * by our [startControlRequest] method, and call the [SpringAnimation.cancel] method of our
     * field [currentSpringAnimation] if [currentSpringAnimation] is not `null` to cancel the
     * current spring animation that may have been started by our [animateImeToVisibility] method.
     *
     * Finally we call our [reset] method to reset all of our internal state.
     */
    fun cancel() {
        insetsAnimationController?.finish(isImeShownAtStart)
        pendingRequestCancellationSignal?.cancel()

        // Cancel the current spring animation
        currentSpringAnimation?.cancel()

        reset()
    }

    /**
     * Finish the current [WindowInsetsAnimationControllerCompat] immediately. We initialize our
     * [WindowInsetsAnimationControllerCompat] variable `val controller` to our field
     * [insetsAnimationController] and if it is `null` we call the [CancellationSignal.cancel]
     * method of our field [pendingRequestCancellationSignal] if [pendingRequestCancellationSignal]
     * is not `null` to cancel the request to control and return.
     *
     * Otherwise
     */
    fun finish() {
        val controller = insetsAnimationController

        if (controller == null) {
            // If we don't currently have a controller, cancel any pending request and return
            pendingRequestCancellationSignal?.cancel()
            return
        }

        val current = controller.currentInsets.bottom
        val shown = controller.shownStateInsets.bottom
        val hidden = controller.hiddenStateInsets.bottom

        when (current) {
            // The current inset matches either the shown/hidden inset, finish() immediately
            shown -> controller.finish(true)
            hidden -> controller.finish(false)
            else -> {
                // Otherwise, we'll look at the current position...
                if (controller.currentFraction >= SCROLL_THRESHOLD) {
                    // If the IME is past the 'threshold' we snap to the toggled state
                    controller.finish(!isImeShownAtStart)
                } else {
                    // ...otherwise, we snap back to the original visibility
                    controller.finish(isImeShownAtStart)
                }
            }
        }
    }

    /**
     * Finish the current [WindowInsetsAnimationControllerCompat]. We finish the animation,
     * animating to the end state if necessary.
     *
     * @param velocityY the velocity of the touch gesture which caused this call to [animateToFinish].
     * Can be `null` if velocity is not available.
     */
    fun animateToFinish(velocityY: Float? = null) {
        val controller = insetsAnimationController

        if (controller == null) {
            // If we don't currently have a controller, cancel any pending request and return
            pendingRequestCancellationSignal?.cancel()
            return
        }

        val current = controller.currentInsets.bottom
        val shown = controller.shownStateInsets.bottom
        val hidden = controller.hiddenStateInsets.bottom

        when {
            // If we have a velocity, we can use it's direction to determine
            // the visibility. Upwards == visible
            velocityY != null -> animateImeToVisibility(
                visible = velocityY > 0,
                velocityY = velocityY
            )
            // The current inset matches either the shown/hidden inset, finish() immediately
            current == shown -> controller.finish(true)
            current == hidden -> controller.finish(false)
            else -> {
                // Otherwise, we'll look at the current position...
                if (controller.currentFraction >= SCROLL_THRESHOLD) {
                    // If the IME is past the 'threshold' we animate it to the toggled state
                    animateImeToVisibility(!isImeShownAtStart)
                } else {
                    // ...otherwise, we animate it back to the original visibility
                    animateImeToVisibility(isImeShownAtStart)
                }
            }
        }
    }

    private fun onRequestReady(controller: WindowInsetsAnimationControllerCompat) {
        // The request is ready, so clear out the pending cancellation signal
        pendingRequestCancellationSignal = null
        // Store the current WindowInsetsAnimationController
        insetsAnimationController = controller

        // Call any pending callback
        pendingRequestOnReady?.invoke(controller)
        pendingRequestOnReady = null
    }

    /**
     * Resets all of our internal state.
     */
    private fun reset() {
        // Clear all of our internal state
        insetsAnimationController = null
        pendingRequestCancellationSignal = null

        isImeShownAtStart = false

        currentSpringAnimation?.cancel()
        currentSpringAnimation = null

        pendingRequestOnReady = null
    }

    /**
     * Animate the IME to a given visibility.
     *
     * @param visible `true` to animate the IME to it's fully shown state, `false` to it's
     * fully hidden state.
     * @param velocityY the velocity of the touch gesture which caused this call. Can be `null`
     * if velocity is not available.
     */
    private fun animateImeToVisibility(
        visible: Boolean,
        velocityY: Float? = null
    ) {
        val controller = insetsAnimationController
            ?: throw IllegalStateException("Controller should not be null")

        currentSpringAnimation = springAnimationOf(
            setter = { insetTo(it.roundToInt()) },
            getter = { controller.currentInsets.bottom.toFloat() },
            finalPosition = when {
                visible -> controller.shownStateInsets.bottom.toFloat()
                else -> controller.hiddenStateInsets.bottom.toFloat()
            }
        ).withSpringForceProperties {
            // Tweak the damping value, to remove any bounciness.
            dampingRatio = SpringForce.DAMPING_RATIO_NO_BOUNCY
            // The stiffness value controls the strength of the spring animation, which
            // controls the speed. Medium (the default) is a good value, but feel free to
            // play around with this value.
            stiffness = SpringForce.STIFFNESS_MEDIUM
        }.apply {
            if (velocityY != null) {
                setStartVelocity(velocityY)
            }
            addEndListener { anim, _, _, _ ->
                if (anim == currentSpringAnimation) {
                    currentSpringAnimation = null
                }
                // Once the animation has ended, finish the controller
                finish()
            }
        }.also { it.start() }
    }
}

/**
 * Scroll threshold for determining whether to animating to the end state, or to the start state.
 * Currently 15% of the total swipe distance distance
 */
private const val SCROLL_THRESHOLD = 0.15f

/**
 * A LinearInterpolator instance we can re-use across listeners.
 */
private val linearInterpolator = LinearInterpolator()
