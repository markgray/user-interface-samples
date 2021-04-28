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
    @SuppressLint("ClickableViewAccessibility")
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
     * A controller that will receive the drag events.
     */
    fun interface DragFrameLayoutController {
        fun onDragDrop(captured: Boolean)
    }

    init {
        mDragViews = ArrayList()
        /**
         * Create the `ViewDragHelper` and set its callback.
         */
        mDragHelper = ViewDragHelper.create(
            this,
            1.0f,
            object : ViewDragHelper.Callback() {
            override fun tryCaptureView(child: View, pointerId: Int): Boolean {
                return mDragViews.contains(child)
            }

            @Suppress("RedundantOverride")
            override fun onViewPositionChanged(
                changedView: View,
                left: Int,
                top: Int,
                dx: Int,
                dy: Int
            ) {
                super.onViewPositionChanged(changedView, left, top, dx, dy)
            }

            override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
                return left
            }

            override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
                return top
            }

            override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
                super.onViewCaptured(capturedChild, activePointerId)
                mDragFrameLayoutController.onDragDrop(true)
            }

            override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
                super.onViewReleased(releasedChild, xvel, yvel)
                mDragFrameLayoutController.onDragDrop(false)
            }
        })
    }
}