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

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationControllerCompat
import androidx.core.view.WindowInsetsCompat

/**
 * A [LinearLayout] which acts as a [nested scroll parent][NestedScrollingParent3] to automatically
 * control the IME inset and visibility when running on devices with API level 30+.
 *
 * This class tracks scrolling, overscrolling, and flinging gestures on child scrolling views,
 * such as a [androidx.recyclerview.widget.RecyclerView].
 *
 * This class triggers a request to control the IME insets via
 * [SimpleImeAnimationController.startControlRequest] once it detects a scroll in an appropriate
 * direction to [onNestedPreScroll] and [onNestedScroll]. Once in control, the class will inset
 * (move) the IME in/off screen based on the user's scroll position, using
 * [SimpleImeAnimationController.insetBy].
 *
 * The class supports both animating the IME onto screen (from not visible), and animating it
 * off-screen (from visible). This can be customized through the [scrollImeOnScreenWhenNotVisible]
 * and [scrollImeOffScreenWhenVisible] properties.
 *
 * Note: all of the nested scrolling logic could be extracted to a `CoordinatorLayout.Behavior`
 * if desired.
 *
 * @param context The Context the view is running in, through which it can
 * access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 * reference to a style resource that supplies default values for the view.
 * Can be 0 to not look for defaults.
 */
@RequiresApi(Build.VERSION_CODES.R)
class InsetsAnimationLinearLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), NestedScrollingParent3 {

    /**
     * Our [NestedScrollingParentHelper] helper class for implementing nested scrolling parent views
     * compatible with Android platform versions earlier than Android 5.0 Lollipop (API 21).
     * [ViewGroup] subclasses should instantiate a final instance of this class as a field at
     * construction. For each [ViewGroup] method that has a matching method signature in this class,
     * delegate the operation to the helper instance in an overridden method implementation. This
     * implements the standard framework policy for nested scrolling.
     */
    private val nestedScrollingParentHelper = NestedScrollingParentHelper(this)

    /**
     * Our nested scrolling [View]. It is a Direct child of this ViewParent which contains the target
     * which has successfully claimed a nested scroll operation. It is set in our override of the
     * [onNestedScrollAccepted] to the `child` [View] passed to the method.
     */
    private var currentNestedScrollingChild: View? = null

    /**
     * The [SimpleImeAnimationController] which wraps a [WindowInsetsAnimationControllerCompat] to
     * simplify the implementation of common use-cases around the app-driven animation of the IME.
     */
    private val imeAnimController = SimpleImeAnimationController()

    /**
     * The change in location of the nested scrolling view. TODO: expand once you understand better.
     */
    private var dropNextY: Int = 0

    /**
     * The current location of the nested scrolling view [currentNestedScrollingChild] when our
     * [startControlRequest] method is called to start an IME control request. It is used in our
     * [onControllerReady] method to calculate [dropNextY] (the difference in the view's Y in the
     * window. We store that to find the offset at the next nested scroll)
     */
    private val startViewLocation = IntArray(2)

    /**
     * Set to true to allow scrolling the IME off screen (from being visible),
     * by an downwards scroll. Defaults to `true`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var scrollImeOffScreenWhenVisible = true

    /**
     * Set to true to allow scrolling the IME on screen (from not being visible),
     * by an upwards scroll. Defaults to `true`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var scrollImeOnScreenWhenNotVisible = true

    /**
     * React to a descendant view initiating a nestable scroll operation, claiming the nested scroll
     * operation if appropriate. This method will be called in response to a descendant view invoking
     * `ViewCompat.startNestedScroll(View, int)`. Each parent up the view hierarchy will be given an
     * opportunity to respond and claim the nested scrolling operation by returning `true`.
     *
     * This method may be overridden by `ViewParent` implementations to indicate when the view
     * is willing to support a nested scrolling operation that is about to begin. If it returns
     * `true`, this `ViewParent` will become the target view's nested scrolling parent for the
     * duration of the scroll operation in progress. When the nested scroll is finished this
     * `ViewParent` will receive a call to `onStopNestedScroll(View, int)`.
     *
     * We return `true` if the [ViewCompat.SCROLL_AXIS_VERTICAL] bit in our [axes] parameter is set,
     * and our [type] parameter is [ViewCompat.TYPE_TOUCH], otherwise we return `false`. (ie. We
     * only want to track vertical scrolls, which are driven from a direct touch event)
     *
     * @param child Direct child of this `ViewParent` containing target
     * @param target View that initiated the nested scroll
     * @param axes Flags consisting of [ViewCompat.SCROLL_AXIS_HORIZONTAL],
     * [ViewCompat.SCROLL_AXIS_VERTICAL] or both
     * @param type the type of input which caused this scroll event
     * @return `true` if this `ViewParent` accepts the nested scroll operation
     */
    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        // We only want to track vertical scrolls, which are driven from a direct touch event.
        return (axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && type == ViewCompat.TYPE_TOUCH
    }

    /**
     * React to the successful claiming of a nested scroll operation. This method will be called
     * after [onStartNestedScroll] returns `true`. It offers an opportunity for the view and its
     * superclasses to perform initial configuration for the nested scroll. Implementations of this
     * method should always call their superclass's implementation of this method if one is present.
     *
     * We call the `onNestedScrollAccepted` method of our [NestedScrollingParentHelper] field
     * [nestedScrollingParentHelper], then set our [View] field [currentNestedScrollingChild] to
     * our parameter [child].
     *
     * @param child Direct child of this [ViewParent] containing target
     * @param target View that initiated the nested scroll
     * @param axes Flags consisting of [ViewCompat.SCROLL_AXIS_HORIZONTAL],
     * [ViewCompat.SCROLL_AXIS_VERTICAL] or both.
     * @param type the type of input which caused this scroll event. This will be
     * [ViewCompat.TYPE_TOUCH] since that is the only type our [onStartNestedScroll]
     * override returns `true` for.
     */
    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes, type)
        currentNestedScrollingChild = child
    }

    /**
     * React to a nested scroll in progress before the target view consumes a portion of the scroll.
     * When working with nested scrolling often the parent view may want an opportunity to consume
     * the scroll before the nested scrolling child does. An example of this is a drawer that
     * contains a scrollable list. The user will want to be able to scroll the list fully into view
     * before the list itself begins scrolling.
     *
     * [onNestedPreScroll] is called when a nested scrolling child invokes [View.dispatchNestedPreScroll].
     * The implementation should report how any pixels of the scroll reported by dx, dy were consumed
     * in the [consumed] array. Index 0 corresponds to dx and index 1 corresponds to dy. This parameter
     * will never be null. Initial values for `consumed[0]` and `consumed[1]` will always be 0.
     *
     * If the [SimpleImeAnimationController.isInsetAnimationRequestPending] method of our field
     * [imeAnimController] returns `true` We're waiting for a controller to become ready so we
     * consume and no-op the scroll by setting `consumed[0]` to [dx] and `consumed[1]` to [dy]
     * and return.
     *
     * Otherwise we initialize our [Int] variable `var deltaY` to [dy], then if our [Int] field
     * [dropNextY] is not 0 we set `consumed[1]` to [dropNextY], subtract [dropNextY] from `deltaY`
     * and set [dropNextY] to 0.
     *
     * If `deltaY` is less than 0 the user is scrolling down, so we check if we currently have
     * control by calling the [SimpleImeAnimationController.isInsetAnimationInProgress] method of
     * our field [imeAnimController] (it returns true if an inset animation is in progress) and if
     * so we can update the IME insets using the [SimpleImeAnimationController.insetBy] method of
     * [imeAnimController] which we call with minus `deltaY`, and subtract the amount of dy consumed
     * by the inset animation in pixels from `consumed[1]`. If an inset animation is NOT in progress
     * we check if
     *
     * @param target [View] that initiated the nested scroll
     * @param dx Horizontal scroll distance in pixels
     * @param dy Vertical scroll distance in pixels
     * @param consumed Output. The horizontal and vertical scroll distance consumed by this parent
     * @param type the type of input which caused this scroll event
     */
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (imeAnimController.isInsetAnimationRequestPending()) {
            // We're waiting for a controller to become ready. Consume and no-op the scroll
            consumed[0] = dx
            consumed[1] = dy
            return
        }

        var deltaY: Int = dy
        if (dropNextY != 0) {
            consumed[1] = dropNextY
            deltaY -= dropNextY
            dropNextY = 0
        }

        if (deltaY < 0) {
            // If the user is scrolling down...

            if (imeAnimController.isInsetAnimationInProgress()) {
                // If we currently have control, we can update the IME insets using insetBy().
                //
                // The negation on the deltaY and the return value is necessary since nested
                // scrolling uses dy values, from 0 (top) to infinity (bottom), meaning that
                // positive values indicate a downwards motion. IME insets are different, as they
                // treat values from from 0 (bottom) to IME-height (top). Since we're using
                // insetBy() with delta values, we can just pass in a simple negation and let it
                // handle the min/max positions.
                consumed[1] -= imeAnimController.insetBy(-deltaY)
            } else if (scrollImeOffScreenWhenVisible &&
                !imeAnimController.isInsetAnimationRequestPending() &&
                ViewCompat.getRootWindowInsets(this)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            ) {
                // If we're not in control, the IME is currently open, and,
                // 'scroll IME away when visible' is enabled, we start a control request
                startControlRequest()

                // We consume the scroll to stop the list scrolling while we wait for a controller
                consumed[1] = deltaY
            }
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        if (dyUnconsumed > 0) {
            // If the user is scrolling up, and the scrolling view isn't consuming the scroll...

            if (imeAnimController.isInsetAnimationInProgress()) {
                // If we currently have control, we can update the IME insets
                consumed[1] = -imeAnimController.insetBy(-dyUnconsumed)
            } else if (scrollImeOnScreenWhenNotVisible &&
                !imeAnimController.isInsetAnimationRequestPending() &&
                ViewCompat.getRootWindowInsets(this)
                    ?.isVisible(WindowInsets.Type.ime()) == false
            ) {
                // If we don't currently have control, the IME is not shown,
                // the user is scrolling up, and the view can't scroll up any more
                // (i.e. over-scrolling), we can start to control the IME insets
                startControlRequest()

                // We consume the scroll to stop the list scrolling while we wait for a controller
                consumed[1] = dyUnconsumed
            }
        }
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        if (imeAnimController.isInsetAnimationInProgress()) {
            // If we have an IME animation in progress, from the user scrolling, we can
            // animate to the end state using the velocity
            imeAnimController.animateToFinish(velocityY)
            // Indicate that we reacted to the fling
            return true
        } else {
            // Otherwise we may need to start a control request and immediately fling
            // using the velocityY
            val imeVisible = ViewCompat.getRootWindowInsets(this)
                ?.isVisible(WindowInsets.Type.ime()) == true
            when {
                velocityY > 0 && scrollImeOnScreenWhenNotVisible && !imeVisible -> {
                    // If the fling is in a upwards direction, and the IME is not visible,
                    // start an control request with an immediate fling
                    imeAnimController.startAndFling(this, velocityY)
                    // Indicate that we reacted to the fling
                    return true
                }
                velocityY < 0 && scrollImeOffScreenWhenVisible && imeVisible -> {
                    // If the fling is in a downwards direction, and the IME is visible,
                    // start an control request with an immediate fling
                    imeAnimController.startAndFling(this, velocityY)
                    // Indicate that we reacted to the fling
                    return true
                }
            }
        }

        // Otherwise, return false to indicate that we did not
        // react to the fling
        return false
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)

        if (imeAnimController.isInsetAnimationInProgress() &&
            !imeAnimController.isInsetAnimationFinishing()
        ) {
            imeAnimController.animateToFinish()
        }
        reset()
    }

    override fun dispatchWindowInsetsAnimationPrepare(animation: WindowInsetsAnimation) {
        super.dispatchWindowInsetsAnimationPrepare(animation)

        // We suppressed layout in startControlRequest(), so we need to un-suppress it now
        suppressLayoutCompat(false)
    }

    /**
     * This starts a control request.
     */
    private fun startControlRequest() {
        // Suppress layout, so that nothing interrupts or is re-laid out while the IME
        // animation starts. This needs to be done before controlWindowInsetsAnimation()
        suppressLayoutCompat(true)

        // Now record the current location of the nested scrolling view. This allows
        // us to track any changes in the location as the animation prepares and starts
        currentNestedScrollingChild?.getLocationInWindow(startViewLocation)

        // Now we can start the control request
        imeAnimController.startControlRequest(
            view = this,
            onRequestReady = { onControllerReady() }
        )
    }

    private fun onControllerReady() {
        val scrollingChild = currentNestedScrollingChild
        if (scrollingChild != null) {
            // Dispatch an IME insets update now, to trigger any WindowInsetsAnimation.Callbacks
            // in the hierarchy, allowing them to setup for the animation
            imeAnimController.insetBy(0)

            // Now calculate the difference in the view's Y in the window. We store that to
            // find the offset at the next nested scroll
            val location = tempIntArray2
            scrollingChild.getLocationInWindow(location)
            dropNextY = location[1] - startViewLocation[1]
        }
    }

    /**
     * Resets all of our internal state.
     */
    private fun reset() {
        // Clear all of our internal state
        dropNextY = 0
        startViewLocation.fill(0)
        // Just to make sure we do not suppress layout forever
        suppressLayoutCompat(false)
    }

    /**
     * Overrides of necessary nested scroll APIs.
     *
     * The nested scrolling APIs have had a number of revisions, with each revision extending the
     * previous revision. The latest,
     * [androidx.core.view.NestedScrollingParent3], extends
     * [androidx.core.view.NestedScrollingParent2], which extends
     * [androidx.core.view.NestedScrollingParent].
     *
     * These classes are all Java interfaces containing various method overloads, usually to add
     * extra parameters. AndroidX Core, the library which bundles the interfaces, is built with
     * JDK 7, meaning it can not use default methods to automatically proxy the methods from the
     * older revisions to the new versions. This means that we need to do that proxying
     * ourselves, as below.
     */

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
        onNestedScrollAccepted(child, target, axes, ViewCompat.TYPE_TOUCH)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int
    ) {
        onNestedScroll(
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed,
            type,
            tempIntArray2
        )
    }

    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }
}

private val tempIntArray2 = IntArray(2)
