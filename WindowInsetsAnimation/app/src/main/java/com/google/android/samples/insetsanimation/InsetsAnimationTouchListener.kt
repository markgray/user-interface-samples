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
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets.Type
import androidx.core.view.ViewCompat
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * A [View.OnTouchListener] which can be set on a scrolling view, to control the IME inset
 * and visibility. When set on a view, it will track drag gestures and trigger a request to
 * control the IME insets via [SimpleImeAnimationController.startControlRequest] once the
 * user is dragging the view.
 *
 * Once in control, the listener will inset the IME in/off screen based on the user's scroll
 * position, using [SimpleImeAnimationController.insetBy].
 *
 * This class should not be used in conjunction with scrolling views, such as
 * [androidx.recyclerview.widget.RecyclerView]. For these views, prefer to use
 * [InsetsAnimationLinearLayout] which uses the much richer nested scrolling APIs to detect and
 * consume scrolling, overscrolling, and flinging interactions.
 *
 * The class supports both animating the IME onto screen (from not visible), and animating it
 * off-screen (from visible). This can be customized through the [scrollImeOnScreenWhenNotVisible]
 * and [scrollImeOffScreenWhenVisible] constructor parameters.
 *
 * This class is not actually used in the sample, but is left here as an example of how to
 * implement a [View.OnTouchListener] with [SimpleImeAnimationController].
 *
 * @param scrollImeOffScreenWhenVisible Whether the IME should be scrolled off screen (from being
 * visible), by an downwards scroll. Defaults to `true`.
 * @param scrollImeOnScreenWhenNotVisible Whether the IME should be scrolled on screen (from not
 * being visible), by an downwards scroll. Defaults to `true`.
 */
class InsetsAnimationTouchListener(
    private val scrollImeOffScreenWhenVisible: Boolean = true,
    private val scrollImeOnScreenWhenNotVisible: Boolean = true
) : View.OnTouchListener {
    /**
     * `true` if we are currently handling the touch gesture. We to `true` in our [onTouch] override
     * if the gesture is mostly vertical, and larger than the touch slop.
     */
    private var isHandling = false

    /**
     * X coordinate of the [MotionEvent] that triggered the call to our [onTouch] override for the
     * actions [MotionEvent.ACTION_DOWN] and [MotionEvent.ACTION_MOVE], and set to 0 by our [reset]
     * method which is called for actions [MotionEvent.ACTION_UP] and [MotionEvent.ACTION_CANCEL].
     */
    private var lastTouchX = 0f

    /**
     * Y coordinate of the [MotionEvent] that triggered the call to our [onTouch] override for the
     * actions [MotionEvent.ACTION_DOWN] and [MotionEvent.ACTION_MOVE], and set to 0 by our [reset]
     * method which is called for actions [MotionEvent.ACTION_UP] and [MotionEvent.ACTION_CANCEL].
     * It is used to compute the change in Y of [MotionEvent.ACTION_MOVE] actions in our [onTouch]
     * override, and that change between events is used to make decisions about the IME position.
     */
    private var lastTouchY = 0f

    /**
     * The top Y coordinate in its window of the [View] in the touch event that was reported to our
     * [onTouch] override. It is used for [MotionEvent.ACTION_MOVE] actions to detect the difference
     * between the current bounds of the [View] as the `WindowInsetsAnimation` progresses in order
     * to account for that change in our touch handling.
     */
    private var lastWindowY = 0

    /**
     * The [Rect] we use when we call our [View.copyBoundsInWindow] extension function to contain
     * the [View]'s position and bounds in its window. It is always used in our [onTouch] override
     * for the [View] whose touch is being reported.
     */
    private val bounds = Rect()

    /**
     * The [SimpleImeAnimationController] we use to animate our IME on and off the screen.
     */
    private val simpleController = SimpleImeAnimationController()

    /**
     * The [VelocityTracker] helper we use for tracking the velocity of touch events. We call its
     * [VelocityTracker.addMovement] method to add the movement of each [MotionEvent] received by
     * our [onTouch] override, as well adding the movement of a [MotionEvent] which is offset to
     * account for any animation of the IME that may have occurred, then we use its method
     * [VelocityTracker.computeCurrentVelocity] to calculate the current velocity when we receive
     * a [MotionEvent.ACTION_UP] action in order to use its `yVelocity` in a call to the method
     * [SimpleImeAnimationController.animateToFinish] of [simpleController] to finish the current
     * animation of the IME.
     */
    private var velocityTracker: VelocityTracker? = null

    /**
     * Called when a touch event is dispatched to a [View]. This allows listeners to get a chance to
     * respond before the target view. First off, if our [VelocityTracker] field [velocityTracker]
     * is `null` we initialize it to the new [VelocityTracker] instance returned by the method
     * [VelocityTracker.obtain] (this will happen when we receive the first [MotionEvent.ACTION_DOWN]
     * action for a new user gesture since it is set to `null` in our [reset] method whenever a
     * gesture ends). We then branch on the action type of the [MotionEvent] parameter [event]:
     *  - [MotionEvent.ACTION_DOWN] A pressed gesture has started, the motion contains the initial
     *  starting location. We call the [VelocityTracker.addMovement] method of [velocityTracker] to
     *  add the user's movement contained in [event] to the tracker (ie. the starting position and
     *  starting time). We initialize our [Float] field [lastTouchX] to the X coordinate of [event]
     *  and our [Float] field [lastTouchY] to the Y coordinate of [event]. We call our extension
     *  function [View.copyBoundsInWindow] on our [View] parameter [v] to have it update the [Rect]
     *  field [bounds] with the [View]'s position and bounds in its window, then initialize our
     *  [Int] field [lastWindowY] to the top Y coordinate of [bounds].
     *  - [MotionEvent.ACTION_MOVE] A change has happened during a press gesture (between ACTION_DOWN
     *  and ACTION_UP). The motion contains the most recent point, as well as any intermediate points
     *  since the last down or move event. We call our extension function [View.copyBoundsInWindow]
     *  on our [View] parameter [v] to have it update the [Rect] field [bounds] with the [View]'s
     *  position and bounds in its window, then initialize our [Int] variable `val windowOffsetY` to
     *  the top Y coordinate of [bounds] minus our [Int] field [lastWindowY]. This is the movement
     *  of the IME that may have occurred due to the progression of the `WindowInsetsAnimation`. We
     *  then make a copy of the [MotionEvent] parameter [event] for our variable `val vtev`, offset
     *  it by this calculated `windowOffsetY` using the [MotionEvent.offsetLocation] method, and then
     *  call the [VelocityTracker.addMovement] method of [velocityTracker] to have it add `vtev` to
     *  the movement in progress. Next we initialize our [Float] variable `val dx` to the X coordinate
     *  of `vtev` minus [lastTouchX] and our [Float] variable `val dy` to the Y coordinate of `vtev`
     *  minus [lastTouchY] (these are the changes in position of the window since the previous touch
     *  gesture). If [isHandling] is `false` we're not currently handling the touch gesture, so we
     *  check if we should start handling, by seeing if the gesture is majorly vertical (the absolute
     *  value of `dy` is greater than absolute value of `dx`), and the absolute value of `dy` is
     *  larger than the touch slop) and we save this [Boolean] value in [isHandling]. Next we check
     *  if [isHandling] is now `true` and if so we check if [simpleController] has an inset animation
     *  in progress and if so we update the IME insets to 'scroll' the IME in by `dy`. If no inset
     *  animation is in progress we check that [simpleController] has no animation pending and our
     *  [shouldStartRequest] method determines if given `dy` and the visibility of the IME support
     *  a IME animation request and if so we call the [SimpleImeAnimationController.startControlRequest]
     *  method of [simpleController] to start the inset animation of the IME. As a last step when
     *  [isHandling] is `true` we record the event's Y coordinate in [lastTouchY], its X coordinate
     *  in [lastTouchX], and view's Y window position (the `top` of our [Rect] field [bounds]) in
     *  [lastWindowY], for the next touch event.
     *  - [MotionEvent.ACTION_UP] A pressed gesture has finished, the motion contains the final
     *  release location as well as any intermediate points since the last down or move event. We
     *  call the [VelocityTracker.addMovement] method of [velocityTracker] to add the final movement
     *  to the tracker, then we call its [VelocityTracker.computeCurrentVelocity] method to have it
     *  compute the current velocity based on the points that have been collected in pixels per
     *  second, and then initialize our [Float] variable `val velocityY` to the Y component of the
     *  velocity is calculated. We then call our [SimpleImeAnimationController.animateToFinish]
     *  method of [velocityTracker] to have it finish the animation, and finally call our [reset]
     *  method to reset our touch handling state.
     *  - [MotionEvent.ACTION_CANCEL] The current gesture has been aborted. We call the
     *  [SimpleImeAnimationController.cancel] method of [simpleController] then call our [reset]
     *  method to reset our touch handling state.
     *
     * Finally we return `false` to indicate that we did not consume the event.
     *
     * @param v The [View] the touch event has been dispatched to.
     * @param event The [MotionEvent] object containing full information about the event.
     * @return `true` if the listener has consumed the event, `false` otherwise.
     */
    @SuppressLint("NewApi", "ClickableViewAccessibility", "WrongConstant")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (velocityTracker == null) {
            // Obtain a VelocityTracker if we don't have one yet
            velocityTracker = VelocityTracker.obtain()
        }

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                velocityTracker?.addMovement(event)

                lastTouchX = event.x
                lastTouchY = event.y

                v.copyBoundsInWindow(bounds)
                lastWindowY = bounds.top
            }
            MotionEvent.ACTION_MOVE -> {
                // Since the view is likely to be translated/moved as the WindowInsetsAnimation
                // progresses, we need to make sure we account for that change in our touch
                // handling. We do that by keeping track of the view's Y position in the window,
                // and detecting the difference between the current bounds.
                v.copyBoundsInWindow(bounds)
                val windowOffsetY: Int = bounds.top - lastWindowY

                // We then make a copy of the MotionEvent, and offset it with the calculated
                // windowOffsetY. We can then pass it to the VelocityTracker.
                val vtev = MotionEvent.obtain(event)
                vtev.offsetLocation(0f, windowOffsetY.toFloat())
                velocityTracker?.addMovement(vtev)

                val dx: Float = vtev.x - lastTouchX
                val dy: Float = vtev.y - lastTouchY

                if (!isHandling) {
                    // If we're not currently handling the touch gesture, lets check if we should
                    // start handling, by seeing if the gesture is majorly vertical, and
                    // larger than the touch slop
                    isHandling = dy.absoluteValue > dx.absoluteValue &&
                        dy.absoluteValue >= ViewConfiguration.get(v.context).scaledTouchSlop
                }

                if (isHandling) {
                    if (simpleController.isInsetAnimationInProgress()) {
                        // If we currently have control, we can update the IME insets to 'scroll'
                        // the IME in
                        simpleController.insetBy(dy.roundToInt())
                    } else if (
                        !simpleController.isInsetAnimationRequestPending() &&
                        shouldStartRequest(
                            dy = dy,
                            imeVisible = ViewCompat.getRootWindowInsets(v)
                                ?.isVisible(Type.ime()) == true
                        )
                    ) {
                        // If we don't currently have control (and a request isn't pending),
                        // the IME is not shown, the user is scrolling up, and the view can't
                        // scroll up any more (i.e. over-scrolling), we can start to control
                        // the IME insets
                        simpleController.startControlRequest(v)
                    }

                    // Lastly we record the event X, Y, and view's Y window position, for the
                    // next touch event
                    lastTouchY = event.y
                    lastTouchX = event.x
                    lastWindowY = bounds.top
                }
            }
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)

                // Calculate the current velocityY, over 1000 milliseconds
                velocityTracker?.computeCurrentVelocity(1000)
                val velocityY: Float? = velocityTracker?.yVelocity

                // If we received a ACTION_UP event, end any current WindowInsetsAnimation passing
                // in the calculated Y velocity
                simpleController.animateToFinish(velocityY)

                // Reset our touch handling state
                reset()
            }
            MotionEvent.ACTION_CANCEL -> {
                // If we received a ACTION_CANCEL event, cancel any current WindowInsetsAnimation
                simpleController.cancel()
                // Reset our touch handling state
                reset()
            }
        }

        return false
    }

    /**
     * Resets all of our internal state.
     */
    private fun reset() {
        // Clear all of our internal state
        isHandling = false
        lastTouchX = 0f
        lastTouchY = 0f
        lastWindowY = 0
        bounds.setEmpty()

        velocityTracker?.recycle()
        velocityTracker = null
    }

    /**
     * Returns true if the given [dy], [IME visibility][imeVisible], and constructor options
     * support a IME animation request.
     *  - If [dy] is less than 0 (the user is scrolling up), the [imeVisible] parameter is `false`
     *  (the IME is not currently visible) and [scrollImeOnScreenWhenNotVisible] is `true` we return
     *  `true`
     *  - If [dy] is greater than 0 (the user is scrolling down), the [imeVisible] parameter is
     *  `true` (the IME is currently visible) and [scrollImeOffScreenWhenVisible] is `true` we
     *  return `true`
     *
     * For all other cases we return `false`.
     *
     * @param dy the change in the Y coordinate which has occurred.
     * @param imeVisible `true` if the IME is currently on screen.
     * @return `true` if we decide that an IME animation request is called for.
     */
    private fun shouldStartRequest(dy: Float, imeVisible: Boolean) = when {
        // If the user is scroll up, return true if scrollImeOnScreenWhenNotVisible is true, and
        // the IME is not currently visible
        dy < 0 -> !imeVisible && scrollImeOnScreenWhenNotVisible
        // If the user is scroll down, start the request if scrollImeOffScreenWhenVisible is true,
        // and the IME is currently visible
        dy > 0 -> imeVisible && scrollImeOffScreenWhenVisible
        // Otherwise, return false
        else -> false
    }
}
