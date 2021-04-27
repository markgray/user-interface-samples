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
import android.widget.FrameLayout
import androidx.customview.widget.ViewDragHelper
import java.util.ArrayList

/**
 * A [FrameLayout] that allows the user to drag and reposition child views.
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
     * The [DragFrameLayoutController] that will be notify on drag.
     */
    private lateinit var mDragFrameLayoutController: DragFrameLayoutController
    private val mDragHelper: ViewDragHelper
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel()
            return false
        }
        return mDragHelper.shouldInterceptTouchEvent(ev)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        mDragHelper.processTouchEvent(ev)
        return true
    }

    /**
     * Adds a new [View] to the list of views that are draggable within the container.
     * @param dragView the [View] to make draggable
     */
    fun addDragView(dragView: View) {
        mDragViews.add(dragView)
    }

    /**
     * Sets the [DragFrameLayoutController] that will receive the drag events.
     * @param dragFrameLayoutController a [DragFrameLayoutController]
     */
    fun setDragFrameController(dragFrameLayoutController: DragFrameLayoutController) {
        mDragFrameLayoutController = dragFrameLayoutController
    }

    /**
     * A controller that will receive the drag events.
     */
    interface DragFrameLayoutController {
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