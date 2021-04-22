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

import android.net.Uri
import android.view.DragEvent
import android.view.View
import android.view.View.OnDragListener
import android.widget.ImageView

/**
 * [OnDragListener] for [ImageView]'s.
 * Sets colors of the target when [DragEvent]'s fire. When a drop is received, the [Uri] backing
 * the first [android.content.ClipData.Item] in the [DragEvent] is set as the image
 * resource of the [ImageView].
 */
open class ImageDragListener : OnDragListener {
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