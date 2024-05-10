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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper
import java.util.ArrayList

/**
 * A [FrameLayout] that allows the user to drag and reposition child views. It is the root [View] of
 * the layout file layout/ztranslation.xml which is used by [ElevationDragFragment].
 *
 * @param context The [Context] the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 * @param defStyleRes A resource identifier of a style resource that
 *        supplies default values for the view, used only if
 *        defStyleAttr is 0 or can not be found in the theme. Can be 0
 *        to not look for defaults.
 */
class DragFrameLayout @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context!!, attrs, defStyleAttr, defStyleRes) {
    /**
     * The list of [View]s that will be draggable.
     */
    @Suppress("JoinDeclarationAndAssignment") // It is better this way
    private val mDragViews: MutableList<View>

    /**
     * The [DragFrameLayoutController] that will be notified on drag.
     */
    private lateinit var mDragFrameLayoutController: DragFrameLayoutController

    /**
     * The [ViewDragHelper] we use to interpret touch event [MotionEvent]s. [ViewDragHelper] is a
     * utility class for writing custom [ViewGroup]s. It offers a number of useful operations and
     * state tracking for allowing a user to drag and reposition views within their parent [ViewGroup]
     */
    private val mDragHelper: ViewDragHelper

    /**
     * Implement this method to intercept all touch screen motion events. This allows you to watch
     * events as they are dispatched to your children, and take ownership of the current gesture at
     * any point. Using this function takes some care, as it has a fairly complicated interaction
     * with [onTouchEvent], and using it requires implementing that method as well as this one in
     * the correct way.  Events will be received in the following order:
     *  - You will receive the down event here.
     *  - The down event will be handled either by a child of this view group, or given to your own
     *  [onTouchEvent] method to handle; this means you should implement [onTouchEvent] to return
     *  `true`, so you will continue to see the rest of the gesture (instead of looking for a parent
     *  view to handle it).  Also, by returning true from [onTouchEvent], you will not receive any
     *  following events in [onInterceptTouchEvent] and all touch processing must happen in
     *  [onTouchEvent]
     *  - For as long as you return `false` from this function, each following event (up to and including
     *  the final up) will be delivered first here and then to the target's [onTouchEvent].
     *  - If you return `true` from here, you will not receive any following events: the target view
     *  will receive the same event but with the action [MotionEvent.ACTION_CANCEL], and all further
     *  events will be delivered to your [onTouchEvent] method and no longer appear here.
     *
     * We initialize our [Int] variable `val action` to the masked action being performed in our
     * [MotionEvent] parameter [ev] (ie. without pointer index information), and if `action` is either
     * [MotionEvent.ACTION_CANCEL] (current gesture has been aborted) or [MotionEvent.ACTION_UP]
     * (pressed gesture has finished) we call the [ViewDragHelper.cancel] method of our field
     * [mDragHelper] to release the capture of our child [View] and return `false` in order to
     * continue to intercept events. For all other `action` values we return the value returned by
     * the [ViewDragHelper.shouldInterceptTouchEvent] method of [mDragHelper] for [ev] (it returns
     * `true` if it determines we should return `true` from [onInterceptTouchEvent] to continue to
     * intercept the touch event stream).
     *
     * @param ev The motion event being dispatched down the hierarchy.
     * @return Return `true` to steal motion events from the children and have them dispatched to
     * this [ViewGroup] through [onTouchEvent]. The current target will receive an
     * [MotionEvent.ACTION_CANCEL] event, and no further messages will be delivered here.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action: Int = ev.actionMasked
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel()
            return false
        }
        return mDragHelper.shouldInterceptTouchEvent(ev)
    }

    /**
     * Implement this method to handle touch screen motion events. We pass our [MotionEvent] parameter
     * to the [ViewDragHelper.processTouchEvent] method of [mDragHelper] to have it process the touch
     * event and dispatch callback events as needed, and return `true` to consume the [MotionEvent].
     *
     * @param ev The motion event.
     * @return `true` if the event was handled, `false` otherwise.
     */
    @SuppressLint("ClickableViewAccessibility") // Elevation is visible only to the sighted?
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mDragHelper.processTouchEvent(ev)
        return true
    }

    /**
     * Adds a new [View] to the list of views in our field [mDragViews] that are draggable within
     * our container.
     *
     * @param dragView the [View] to make draggable
     */
    fun addDragView(dragView: View) {
        mDragViews.add(dragView)
    }

    /**
     * Sets the [DragFrameLayoutController] that will receive the drag events.
     *
     * @param dragFrameLayoutController a [DragFrameLayoutController]
     */
    fun setDragFrameController(dragFrameLayoutController: DragFrameLayoutController) {
        mDragFrameLayoutController = dragFrameLayoutController
    }

    /**
     * A controller that will receive the drag events. Its [onDragDrop] method will be called by the
     * `onViewCaptured` method of the [ViewDragHelper.Callback] of our [mDragHelper] field with `true`
     * and with `false` from its `onViewReleased` method.
     */
    fun interface DragFrameLayoutController {
        /**
         * Called by the `onViewCaptured` method of the [ViewDragHelper.Callback] of our [mDragHelper]
         * field with `true` and with `false` from its `onViewReleased` method.
         *
         * @param captured `true` if the child view has been captured by a [MotionEvent.ACTION_DOWN],
         * and `false` when it is released by a [MotionEvent.ACTION_POINTER_UP] or a
         * [MotionEvent.ACTION_CANCEL].
         */
        fun onDragDrop(captured: Boolean)
    }

    init {
        /**
         * Our list of draggable child views.
         */
        mDragViews = ArrayList()
        /**
         * Create the `ViewDragHelper` and set its callback.
         */
        mDragHelper = ViewDragHelper.create(
            this,  // Parent view to monitor
            1.0f, // Multiplier for how sensitive the helper should be
            object : ViewDragHelper.Callback() {
                /**
                 * Called when the user's input indicates that they want to capture the given child
                 * view with the pointer indicated by [pointerId]. The callback should return `true`
                 * if the user is permitted to drag the given view with the indicated pointer. We
                 * return `true` if [child] is in our list of draggable child views [mDragViews].
                 *
                 * @param child Child the user is attempting to capture
                 * @param pointerId ID of the pointer attempting the capture
                 * @return true if capture should be allowed, false otherwise
                 */
                override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                    return mDragViews.contains(child)
                }

                /**
                 * Called when the captured view's position changes as the result of a drag or settle.
                 * We just call our super's implementation of `onViewPositionChanged`.
                 *
                 * @param changedView View whose position changed
                 * @param left New X coordinate of the left edge of the view
                 * @param top New Y coordinate of the top edge of the view
                 * @param dx Change in X position from the last call
                 * @param dy Change in Y position from the last call
                 */
                @Suppress("RedundantOverride") // Suggested change would make class less reusable
                override fun onViewPositionChanged(
                    changedView: View,
                    left: Int,
                    top: Int,
                    dx: Int,
                    dy: Int
                ) {
                    super.onViewPositionChanged(changedView, left, top, dx, dy)
                }

                /**
                 * Restrict the motion of the dragged child view along the horizontal axis. The
                 * default implementation does not allow horizontal motion; the extending class
                 * must override this method and provide the desired clamping. We just return our
                 * parameter [left] to the caller.
                 *
                 * @param child Child view being dragged
                 * @param left Attempted motion along the X axis
                 * @param dx Proposed change in position for left
                 * @return The new clamped position for left
                 */
                override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                    return left
                }

                /**
                 * Restrict the motion of the dragged child view along the vertical axis. The
                 * default implementation does not allow vertical motion; the extending class
                 * must override this method and provide the desired clamping. We just return our
                 * parameter [top] to the caller.
                 *
                 * @param child Child view being dragged
                 * @param top Attempted motion along the Y axis
                 * @param dy Proposed change in position for top
                 * @return The new clamped position for top
                 */
                override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                    return top
                }

                /**
                 * Called when a child view is captured for dragging or settling. First we call our
                 * super's implementation of `onViewCaptured` then we call the
                 * [DragFrameLayoutController.onDragDrop] method of our field [mDragFrameLayoutController]
                 * with `true` and the [DragFrameLayoutController] that is constructed in the
                 * `onCreateView` override of [ElevationDragFragment] will elevate the floating view
                 * by 50f pixels and log the message "Drag".
                 *
                 * @param capturedChild Child view that was captured
                 * @param activePointerId Pointer id tracking the child capture
                 */
                override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                    super.onViewCaptured(capturedChild, activePointerId)
                    mDragFrameLayoutController.onDragDrop(true)
                }

                /**
                 * Called when the child view is no longer being actively dragged. First we call our
                 * super's implementation of `onViewCaptured` then we call the
                 * [DragFrameLayoutController.onDragDrop] method of our field [mDragFrameLayoutController]
                 * with `false` and the [DragFrameLayoutController] that is constructed in the
                 * `onCreateView` override of [ElevationDragFragment] will elevate the floating view
                 * by 0f pixels and log the message "Drop".
                 *
                 * @param releasedChild The captured child view now being released
                 * @param xvel X velocity of the pointer as it left the screen in pixels per second.
                 * @param yvel Y velocity of the pointer as it left the screen in pixels per second.
                 */
                override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                    super.onViewReleased(releasedChild, xvel, yvel)
                    mDragFrameLayoutController.onDragDrop(false)
                }
            })
    }
}