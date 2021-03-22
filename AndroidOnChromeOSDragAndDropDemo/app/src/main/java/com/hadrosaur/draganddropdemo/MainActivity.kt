/*      Copyright 2018 Google LLC All rights reserved.

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.hadrosaur.draganddropdemo

import android.content.ClipData
import android.content.ClipDescription
import android.graphics.Color
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.Gravity
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.View.OnDragListener
import android.view.View.OnLongClickListener
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * A demo android application demonstrating basic Drag and Drop functionality with Chrome OS in mind.
 * Allows for plain-text items and files from the Chrome OS file manager to be dragged into the app.
 * Has a plain-text item that can be dragged out.
 */
open class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main]. We initialize our
     * [TextView] variable `val dragText` by finding the view with ID [R.id.text_drag] and our
     * [FrameLayout] variable `val targetFrame` by finding the view with ID [R.id.frame_target]. We
     * set the [OnDragListener] of `targetFrame` to an instance of our [DropTargetListener] class,
     * and set the [OnLongClickListener] of `dragText` to an instance of [TextViewLongClickListener].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val dragText = findViewById<TextView>(R.id.text_drag)
        val targetFrame = findViewById<FrameLayout>(R.id.frame_target)

        //Set up drop target listener.
        targetFrame.setOnDragListener(DropTargetListener(this))

        //Set up draggable item listener.
        dragText.setOnLongClickListener(TextViewLongClickListener())
    }

    /**
     * This is the custom [OnDragListener] used for the [FrameLayout] with ID [R.id.frame_target]
     * which is used as the drop target.
     */
    protected inner class DropTargetListener(
        /**
         * The [MainActivity] instance to use for context when constructing our [TextView].
         */
        private val mActivity: AppCompatActivity
        ) : OnDragListener {
        /**
         * Called when a drag event is dispatched to a [View]. This allows listeners to get a chance
         * to override base [View] behavior.
         *
         * @param v The [View] that received the drag event.
         * @param event The [DragEvent] object for the drag event.
         * @return `true` if the drag event was handled successfully, or `false` if the drag event
         * was not handled. Note that `false` will trigger the [View] to call its own `onDrag`
         * handler.
         */
        override fun onDrag(v: View, event: DragEvent): Boolean {
            return when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Limit the types of items this can receive to plain-text and Chrome OS files
                    if (event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)
                        || event.clipDescription.hasMimeType("application/x-arc-uri-list")) {

                        // Greenify background colour so user knows this is a target.
                        v.setBackgroundColor(Color.argb(55, 0, 255, 0))
                        return true
                    }

                    //If the dragged item is of an undesired type, report that this is not a valid target
                    false
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Increase green background colour when item is over top of target.
                    v.setBackgroundColor(Color.argb(150, 0, 255, 0))
                    true
                }
                DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DRAG_EXITED -> {
                    // Less intense green background colour when item not over target.
                    v.setBackgroundColor(Color.argb(55, 0, 255, 0))
                    true
                }
                DragEvent.ACTION_DROP -> {
                    requestDragAndDropPermissions(event) //Allow items from other applications
                    val item = event.clipData.getItemAt(0)
                    when {
                        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) -> {
                            //If this is a text item, simply display it in a new TextView.
                            val frameTarget = v as FrameLayout
                            frameTarget.removeAllViews()
                            val droppedText = TextView(mActivity)
                            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                            params.gravity = Gravity.CENTER
                            droppedText.layoutParams = params
                            droppedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30f)
                            droppedText.text = item.text
                            frameTarget.addView(droppedText)
                        }
                        event.clipDescription.hasMimeType("application/x-arc-uri-list") -> {
                            //If a file, read the first 200 characters and output them in a new TextView.

                            //Note the use of ContentResolver to resolve the ChromeOS content URI.
                            val contentUri = item.uri
                            val parcelFileDescriptor: ParcelFileDescriptor? = try {
                                contentResolver.openFileDescriptor(contentUri, "r")
                            } catch (e: FileNotFoundException) {
                                e.printStackTrace()
                                Log.e("DRAGTEST", "File not found.")
                                return false
                            }
                            val fileDescriptor = parcelFileDescriptor!!.fileDescriptor
                            val bytes = ByteArray(MAX_LENGTH)
                            try {
                                val inputStream = FileInputStream(fileDescriptor)
                                inputStream.use { fileInputStream ->
                                    fileInputStream.read(bytes, 0, MAX_LENGTH)
                                }
                            } catch (ex: Exception) {
                            }
                            val contents = String(bytes)
                            val contentLength = if (contents.length > CHARS_TO_READ) CHARS_TO_READ else 0
                            val frameTarget = v as FrameLayout
                            frameTarget.removeAllViews()
                            val droppedText = TextView(mActivity)
                            val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
                            params.gravity = Gravity.CENTER
                            droppedText.layoutParams = params
                            droppedText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
                            droppedText.text = contents.substring(0, contentLength)
                            frameTarget.addView(droppedText)
                        }
                        else -> {
                            return false
                        }
                    }
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // Restore background colour to transparent.
                    v.setBackgroundColor(Color.argb(0, 255, 255, 255))
                    true
                }
                else -> {
                    Log.e("DragDrop Example", "Unknown action type received by DropTargetListener.")
                    false
                }
            }
        }
    }

    protected inner class TextViewLongClickListener : OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val thisTextView = v as TextView
            val dragContent = "Dragged Text: " + thisTextView.text

            //Set the drag content and type.
            val item = ClipData.Item(dragContent)
            val dragData = ClipData(dragContent, arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN), item)

            //Set the visual look of the dragged object.
            //Can be extended and customized. Default is used here.
            val dragShadow = DragShadowBuilder(v)

            // Starts the drag, note: global flag allows for cross-application drag.
            v.startDragAndDrop(dragData, dragShadow, null, View.DRAG_FLAG_GLOBAL)
            return false
        }
    }
    companion object {
        const val MAX_LENGTH = 5000
        const val CHARS_TO_READ = 200
    }
}