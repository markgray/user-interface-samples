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
package com.example.android.droptarget

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

    /**
     * Convenience function to set the background color of [View] parameter [view] to the [Int] color
     * parameter [color].
     *
     * @param view the [View] whose background color we are to set to the color [color].
     * @param color the color we are to use as the background color for [View] parameter [view]
     */
    private fun setTargetColor(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    /**
     * Retrieves the [ClipData] object sent to the system as part of the call to `startDragAndDrop`
     * from the [DragEvent] and if the [ClipData.Item] at index 0 contains a [Uri] calls our method
     * [setImageUri] to have it set the content of the [View] parameter [view] to the that [Uri].
     * Called only when the `action` property of the [DragEvent] parameter [event] is a
     * [DragEvent.ACTION_DROP].
     *
     * We initialize our [ClipData] variable `val clipData` to the `clipData` property of [DragEvent]
     * parameter [event], and if `clipData` is `null` or the item count of `clipData` is 0 we return
     * `false` to report that we did not handle the event. Otherwise we:
     *  - initialize our [ClipData.Item] variable `val item` to the item at index 0 in `clipData`
     *  and if that is `null` return `false` to report that we did not handle the event.
     *  - initialize our [Uri] variable `val uri` to the raw [Uri] contained in `item` and if that
     *  is `null` return `false` to report that we did not handle the event.
     *
     * If we have gotten this far without failing to handle the event we return the value returned
     * by our [setImageUri] method when it tries to set the content of [View] parameter [view] to
     * the [Uri] variable `uri` ([view] must be an [ImageView] for this to succeed of course).
     *
     * @param view The [View] that received the [DragEvent.ACTION_DROP] drag event.
     * @param event The [DragEvent] object for the [DragEvent.ACTION_DROP] drag event
     * @return `true` if the drag event was handled successfully, or `false` if the drag event was
     * not handled.
     */
    private fun processDrop(view: View, event: DragEvent): Boolean {
        val clipData = event.clipData
        if (clipData == null || clipData.itemCount == 0) {
            return false
        }
        val item = clipData.getItemAt(0) ?: return false
        val uri = item.uri ?: return false
        return setImageUri(view, event, uri)
    }

    /**
     * Called when our [onDrag] override receives a [DragEvent] whose `action` property is
     * [DragEvent.ACTION_DRAG_LOCATION]. We ignore.
     *
     * @param x the `x` coordinate of the [DragEvent]
     * @param y the `y` coordinate of the [DragEvent]
     */
    protected open fun processLocation(x: Float, y: Float) {}

    /**
     * If its [View] parameter [view] is not an [ImageView] we return `false` to report that we did
     * not handle the event. Otherwise we call the [ImageView.setImageURI] method of [view] to have
     * it set its content to the [Uri] parameter [uri] and return `true` to report that the drag
     * event was handled successfully,
     *
     * @param view The [View] that received the [DragEvent.ACTION_DROP] drag event.
     * @param event The [DragEvent] object for the [DragEvent.ACTION_DROP] drag event
     * @param uri the raw [Uri] contained in [ClipData.Item] at index 0 of the [ClipData] property
     * of the [DragEvent]
     */
    protected open fun setImageUri(view: View?, event: DragEvent?, uri: Uri?): Boolean {
        if (view !is ImageView) {
            return false
        }
        view.setImageURI(uri)
        return true
    }

    companion object {
        /**
         * Color used for the background color of the [View] when our [onDrag] override receives a
         * [DragEvent.ACTION_DRAG_ENDED] action [DragEvent] (a very light gray)
         */
        private const val COLOR_INACTIVE = -0x777778

        /**
         * Color used for the background color of the [View] when our [onDrag] override receives a
         * [DragEvent.ACTION_DRAG_STARTED] or a [DragEvent.ACTION_DRAG_EXITED] action [DragEvent]
         * (a dark gray)
         */
        private const val COLOR_ACTIVE = -0x333334

        /**
         * Color used for the background color of the [View] when our [onDrag] override receives a
         * [DragEvent.ACTION_DRAG_ENTERED] action [DragEvent] (a very dark gray)
         */
        private const val COLOR_HOVER = -0x111112
    }
}