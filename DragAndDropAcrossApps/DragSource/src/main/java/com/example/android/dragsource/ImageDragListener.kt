/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.dragsource

import android.content.ClipData
import android.net.Uri
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.ImageView

/**
 * [OnDragListener] for [ImageView]'s. Sets colors of the target when [DragEvent]'s fire. When a
 * drop is received, the [Uri] backing the first [android.content.ClipData.Item] in the [DragEvent]
 * is set as the image resource of the [ImageView].
 */
open class ImageDragListener : OnDragListener {
    /**
     * Called when [DragEvent] parameter [event] is dispatched to the [View] parameter [view]. This
     * allows listeners to get a chance to override base [View] behavior. We branch on the `action`
     * property of our [DragEvent] parameter [event]:
     *  - [DragEvent.ACTION_DRAG_STARTED] (Signals the start of a drag and drop operation) we call
     *  our method [setTargetColor] to have it set the background color of our [View] parameter
     *  [view] to [COLOR_ACTIVE] (a dark gray) and return `true` to report that the drag event was
     *  handled successfully.
     *  - [DragEvent.ACTION_DRAG_ENTERED] (Signals that the drag point has entered the bounding box
     *  of the [View]) we call our method [setTargetColor] to have it set the background color of
     *  our [View] parameter [view] to [COLOR_HOVER] (an even darker gray) and return `true` to
     *  report that the drag event was handled successfully.
     *  - [DragEvent.ACTION_DRAG_LOCATION] (Sent to a [View] after `ACTION_DRAG_ENTERED` while the
     *  drag shadow is still within the [View] object's bounding box, but not within a descendant
     *  [View] that can accept the data) we call our method [processLocation] with the `x` and `y`
     *  location of our [DragEvent] parameter [event] (it does nothing), and return `true` to report
     *  that the drag event was handled successfully.
     *  - [DragEvent.ACTION_DRAG_EXITED] (Signals that the user has moved the drag shadow out of the
     *  bounding box of the [View] or into a descendant [View] that can accept the data) we call our
     *  method [setTargetColor] to have it set the background color of our [View] parameter [view]
     *  to [COLOR_ACTIVE] (a dark gray) and return `true` to report that the drag event was handled
     *  successfully.
     *  - [DragEvent.ACTION_DROP] (Signals that the user has released the drag shadow, and the drag
     *  point is within the bounding box of the [View] and not within a descendant [View] that can
     *  accept the data) we return the value returned by our method [processDrop] after it retrieves
     *  the [ClipData] from the [DragEvent] parameter [event] and displays the [Uri] it contains in
     *  our [View] parameter [view] (if it can it will return `true` and if it cannot it will return
     *  `false`).
     *  - [DragEvent.ACTION_DRAG_ENDED] (Signals that the drag and drop operation has concluded) we
     *  call our method [setTargetColor] to have it set the background color of our [View] parameter
     *  [view] to [COLOR_INACTIVE] (a very light gray) and return `true` to report that the drag
     *  event was handled successfully.
     *  - All other `action` values do nothing and return `false` to trigger the [View] to call its
     *  own `onDragEvent` handler.
     *
     * @param view The [View] that received the drag event.
     * @param event The [DragEvent] object for the drag event.
     * @return `true` if the drag event was handled successfully, or `false` if the drag event was
     * not handled. Note that `false` will trigger the [View] to call its `onDragEvent` handler.
     */
    override fun onDrag(view: View, event: DragEvent): Boolean {
        // Change the color of the target for all events.
        // For the drop action, set the view to the dropped image.
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                setTargetColor(view, COLOR_ACTIVE)
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                setTargetColor(view, COLOR_HOVER)
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> {
                processLocation(event.x, event.y)
                return true
            }
            DragEvent.ACTION_DRAG_EXITED -> {
                setTargetColor(view, COLOR_ACTIVE)
                return true
            }
            DragEvent.ACTION_DROP -> return processDrop(view, event)
            DragEvent.ACTION_DRAG_ENDED -> {
                setTargetColor(view, COLOR_INACTIVE)
                return true
            }
            else -> {
            }
        }
        return false
    }

    private fun setTargetColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    private fun processDrop(view: View, event: DragEvent): Boolean {
        val clipData = event.clipData
        if (clipData == null || clipData.itemCount == 0) {
            return false
        }
        val item = clipData.getItemAt(0) ?: return false
        val uri = item.uri ?: return false
        return setImageUri(view, event, uri)
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun processLocation(x: Float, y: Float) {}

    protected open fun setImageUri(view: View?, event: DragEvent?, uri: Uri?): Boolean {
        if (view !is ImageView) {
            return false
        }
        view.setImageURI(uri)
        return true
    }

    companion object {
        private const val COLOR_INACTIVE = -0x777778
        private const val COLOR_ACTIVE = -0x333334
        private const val COLOR_HOVER = -0x111112
    }
}