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
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
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
    @Suppress("MemberVisibilityCanBePrivate") // I like to use kdoc [] references
    var scrollImeOffScreenWhenVisible: Boolean = true

    /**
     * Set to true to allow scrolling the IME on screen (from not being visible),
     * by an upwards scroll. Defaults to `true`.
     */
    @Suppress("MemberVisibilityCanBePrivate") // I like to use kdoc [] references
    var scrollImeOnScreenWhenNotVisible: Boolean = true

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
     * we check if:
     *  - scroll IME away when visible' is enabled ([scrollImeOffScreenWhenVisible] is `true`
     *  - we're not in control (the [SimpleImeAnimationController.isInsetAnimationRequestPending]
     *  method of [imeAnimController] returns `false`)
     *  - AND the IME is currently open (the [WindowInsetsCompat] from the top of the view hierarchy
     *  reports that the Bit mask of `WindowInsetsCompat.Types.ime` is visible).
     *
     * If all of the above are `true` we start a control request by calling our [startControlRequest]
     * method, and consume the scroll to stop the list scrolling while we wait for a controller by
     * setting `consumed[1]` to `deltaY`.
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

    /**
     * React to a nested scroll in progress. This method will be called when the [ViewParent]'s
     * current nested scrolling child view dispatches a nested scroll event. To receive calls to
     * this method the [ViewParent] must have previously returned `true` for a call to
     * [onStartNestedScroll].
     *
     * Both the consumed and unconsumed portions of the scroll distance are reported to the
     * [ViewParent]. An implementation may choose to use the consumed portion to match or chase
     * scroll position of multiple child elements, for example. The unconsumed portion may be used
     * to allow continuous dragging of multiple scrolling or draggable elements, such as scrolling
     * a list within a vertical drawer where the drawer begins dragging once the edge of inner
     * scrolling content is reached.
     *
     * This method is called when a nested scrolling child invokes
     * `NestedScrollingChild3.dispatchNestedScroll` or one of methods it overloads.
     *
     * An implementation must report how many pixels of the the x and y scroll distances were
     * consumed by this nested scrolling parent by adding the consumed distances to the [consumed]
     * parameter. If this [View] also implements `NestedScrollingChild3`, [consumed] should also be
     * passed up to it's nested scrolling parent so that the parent may also add any scroll distance
     * it consumes. Index 0 corresponds to dx and index 1 corresponds to dy.
     *
     * If our [Int] parameter [dyUnconsumed] is greater than 0 then the user is scrolling up, and
     * the scrolling view isn't consuming the scroll so we check whether we currently have control
     * by calling the [SimpleImeAnimationController.isInsetAnimationInProgress] method of
     * [imeAnimController] and if it returns `true` we set `consumed[1]` to minus the value returned
     * by the [SimpleImeAnimationController.insetBy] when it is passed minus [dyUnconsumed] as the
     * value to update the inset position of the IME to (it returns the amount of [dyUnconsumed]
     * consumed by the inset animation). If an inset animation is NOT in progress we check if:
     *  - scroll IME away when visible' is enabled ([scrollImeOffScreenWhenVisible] is `true`
     *  - we're not in control (the [SimpleImeAnimationController.isInsetAnimationRequestPending]
     *  method of [imeAnimController] returns `false`)
     *  - AND the IME is currently open (the [WindowInsetsCompat] from the top of the view hierarchy
     *  reports that the Bit mask of `WindowInsetsCompat.Types.ime` is visible).
     *
     * If all the above  are `true` we start a control request by calling our [startControlRequest]
     * method, and consume the scroll to stop the list scrolling while we wait for a controller by
     * setting `consumed[1]` to `dyUnconsumed`.
     *
     * @param target The descendant view controlling the nested scroll
     * @param dxConsumed Horizontal scroll distance in pixels already consumed by target
     * @param dyConsumed Vertical scroll distance in pixels already consumed by target
     * @param dxUnconsumed Horizontal scroll distance in pixels not consumed by target
     * @param dyUnconsumed Vertical scroll distance in pixels not consumed by target
     * @param type the type of input which caused this scroll event
     * @param consumed Output. Upon this method returning, will contain the scroll distances
     * consumed by this nested scrolling parent and the scroll distances consumed by any other
     * parent up the view hierarchy
     */
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
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == false
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

    /**
     * Request a fling from a nested scroll. This method signifies that a nested scrolling child has
     * detected suitable conditions for a fling. Generally this means that a touch scroll has ended
     * with a `VelocityTracker velocity` in the direction of scrolling that meets or exceeds the
     * `ViewConfiguration.getScaledMinimumFlingVelocity` minimum fling velocity along a scrollable
     * axis. If a nested scrolling child view would normally fling but it is at the edge of its own
     * content, it can use this method to delegate the fling to its nested scrolling parent instead.
     * The parent may optionally consume the fling or observe a child fling.
     *
     * If the [SimpleImeAnimationController.isInsetAnimationInProgress] method of [imeAnimController]
     * returns `true` we have an IME animation in progress from the user scrolling, so we can animate
     * to the end state using the velocity by calling the `animateToFinish` method of [imeAnimController]
     * with the Vertical velocity in pixels per second argument [velocityY] and return `true` to
     * indicate that we reacted to the fling.
     *
     * Otherwise we may need to start a control request and immediately fling using [velocityY], so
     * we initialize our [Boolean] variable `val imeVisible` to `true` if the [WindowInsetsCompat]
     * returned by the [getRootWindowInsets] method of `this` [View] reports that the IME window
     * inset is visible. Then when [velocityY] is greater than 0 (the fling is in a upwards direction)
     * and [scrollImeOnScreenWhenNotVisible] is `true` (allows scrolling the IME on screen by an
     * upwards scroll), and `imeVisible` is `false` (the IME is not visible yet) we start a control
     * request by calling the [SimpleImeAnimationController.startAndFling] method of [imeAnimController]
     * to have it start a control request for `this` [View] and immediately fling to a finish using
     * [velocityY] once ready, then we return `true` to indicate that we reacted to the fling.
     *
     * If [velocityY] is less than 0 (the fling is in a downwards direction), and
     * [scrollImeOnScreenWhenNotVisible] is `true` (allows scrolling the IME on screen by an upwards
     * scroll), and `imeVisible` is `true` (the IME is visible) we also start a control request by
     * calling the [SimpleImeAnimationController.startAndFling] method of [imeAnimController]
     * to have it start a control request for `this` [View] and immediately fling to a finish using
     * [velocityY] once ready, then we return `true` to indicate that we reacted to the fling.
     *
     * If none of the above situations are in effect we return `false` to indicate that we did not
     * react to the fling.
     *
     * @param target View that initiated the nested scroll
     * @param velocityX Horizontal velocity in pixels per second
     * @param velocityY Vertical velocity in pixels per second
     * @param consumed true if the child consumed the fling, false otherwise
     * @return true if this parent consumed or otherwise reacted to the fling
     */
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
            val imeVisible: Boolean = ViewCompat.getRootWindowInsets(this)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
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

    /**
     * React to a nested scroll operation ending. Perform cleanup after a nested scrolling operation.
     * This method will be called when a nested scroll stops, for example when a nested touch scroll
     * ends with a [MotionEvent.ACTION_UP] or [MotionEvent.ACTION_CANCEL] event. Implementations of
     * this method should always call their superclass's implementation of this method if one is
     * present.
     *
     * First we call the `onStopNestedScroll` method of our [NestedScrollingParentHelper] field
     * [nestedScrollingParentHelper]. Then if the [SimpleImeAnimationController.isInsetAnimationInProgress]
     * method of [imeAnimController] indicates that an inset animation is in progress and its
     * [SimpleImeAnimationController.isInsetAnimationFinishing] method indicates that an inset
     * animation is NOT currently finishing, we call its [SimpleImeAnimationController.animateToFinish]
     * method to finish the animation, animating to the end state if necessary.
     *
     * Finally we call our [reset] method to have it clear all of our internal state.
     *
     * @param target [View] that initiated the nested scroll
     * @param type the type of input which caused this scroll event
     */
    override fun onStopNestedScroll(target: View, type: Int) {
        nestedScrollingParentHelper.onStopNestedScroll(target, type)

        if (imeAnimController.isInsetAnimationInProgress() &&
            !imeAnimController.isInsetAnimationFinishing()
        ) {
            imeAnimController.animateToFinish()
        }
        reset()
    }

    /**
     * Dispatches [WindowInsetsAnimation.Callback.onPrepare] when Window Insets animation is being
     * prepared (which is called when an insets animation is about to start and before the views
     * have been laid out in the end state of the animation). First we call our super's implementation
     * of `dispatchWindowInsetsAnimationPrepare`, then since we suppressed layout in [startControlRequest]
     * we need to un-suppress it now by calling [suppressLayoutCompat] with `false`.
     *
     * @param animation current animation
     */
    override fun dispatchWindowInsetsAnimationPrepare(animation: WindowInsetsAnimation) {
        super.dispatchWindowInsetsAnimationPrepare(animation)

        // We suppressed layout in startControlRequest(), so we need to un-suppress it now
        suppressLayoutCompat(false)
    }

    /**
     * This starts a control request. First we call the [suppressLayoutCompat] method with `true`
     * to suppress layout so that nothing interrupts or is re-laid out while the IME animation
     * starts. Then we call the [getLocationInWindow] method of our nested scrolling [View] field
     * [currentNestedScrollingChild] to have it compute the coordinates of that view in its window
     * and store them in our [IntArray] field [startViewLocation] (this allows us to track any
     * changes in its location as the animation prepares and starts). Then we call the
     * [SimpleImeAnimationController.startControlRequest] method of [imeAnimController] using `this`
     * [View] as the view which is triggering this request, and a lambda that calls our [onControllerReady]
     * method as its `onRequestReady` listener that will be called when the request is ready and the
     * animation can proceed.
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

    /**
     * This is the callback that the [SimpleImeAnimationController.startControlRequest] method calls
     * when the request it starts is ready and the animation can proceed. First we initialize our
     * varible `val scrollingChild` to our nested scrolling [View] field [currentNestedScrollingChild].
     * Then if `scrollingChild` is not `null` we call the [SimpleImeAnimationController.insetBy] method
     * of [imeAnimController] with 0 in order to have it dispatch an IME insets update now, to trigger
     * any [WindowInsetsAnimation.Callback]s in the hierarchy, allowing them to setup for the animation.
     * We initialize our [IntArray] variable `val location` to our field [tempIntArray2] then call the
     * [getLocationInWindow] method of `scrollingChild` to have it compute the coordinates of that
     * view in its window and store them in `location`. We then set our [Int] field [dropNextY] to
     * `location[1]` (the current Y coordinate) minus the Y coordinate in our field [startViewLocation]
     * (this is the difference in the view's Y in the window since [startControlRequest] was called).
     * This is then used in our [onNestedPreScroll] override to calculate how much of the scroll to
     * consume before the nested scrolling child sees the scroll.
     */
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
     * Resets all of our internal state. We set our [Int] field [dropNextY] to 0, fill our [IntArray]
     * field [startViewLocation] with two 0's, then call the [suppressLayoutCompat] method with `false`
     * to un-suppress layout just to make sure we do not suppress layout forever.
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

    /**
     * React to a nested scroll in progress. This method will be called when the ViewParent's
     * current nested scrolling child view dispatches a nested scroll event. To receive calls to
     * this method the ViewParent must have previously returned `true` for a call to
     * [onStartNestedScroll].
     *
     * Both the consumed and unconsumed portions of the scroll distance are reported to the
     * ViewParent. An implementation may choose to use the consumed portion to match or chase scroll
     * position of multiple child elements, for example. The unconsumed portion may be used to
     * allow continuous dragging of multiple scrolling or draggable elements, such as scrolling
     * a list within a vertical drawer where the drawer begins dragging once the edge of inner
     * scrolling content is reached.
     *
     * @param target The descendent view controlling the nested scroll
     * @param dxConsumed Horizontal scroll distance in pixels already consumed by target
     * @param dyConsumed Vertical scroll distance in pixels already consumed by target
     * @param dxUnconsumed Horizontal scroll distance in pixels not consumed by target
     * @param dyUnconsumed Vertical scroll distance in pixels not consumed by target
     * @param type the type of input which cause this scroll event
     */
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

    /**
     * React to a nested scroll operation ending. Perform cleanup after a nested scrolling operation.
     * This method will be called when a nested scroll stops, for example when a nested touch
     * scroll ends with a [MotionEvent.ACTION_UP] or [MotionEvent.ACTION_CANCEL] event.
     *
     * @param target View that initiated the nested scroll
     */
    override fun onStopNestedScroll(target: View) {
        onStopNestedScroll(target, ViewCompat.TYPE_TOUCH)
    }
}

/**
 * The temporary [IntArray] we use to store the location of our nested scrolling child.
 */
private val tempIntArray2 = IntArray(2)
