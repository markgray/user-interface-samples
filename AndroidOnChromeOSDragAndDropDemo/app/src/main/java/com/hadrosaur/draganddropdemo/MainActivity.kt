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

import android.annotation.SuppressLint
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
         * to override base [View] behavior. We branch on the action value of our [DragEvent] parameter
         * [event]:
         *  - [DragEvent.ACTION_DRAG_STARTED] Signals the start of a drag and drop operation. If the
         *  [ClipDescription] of [event] has the MIME type [ClipDescription.MIMETYPE_TEXT_PLAIN] or
         *  has the MIME type "application/x-arc-uri-list" we set the background color of our [View]
         *  parameter [v] to a light green and return `true` to report the drag event was handled
         *  successfully. For any other MIME type we return `false` to have [v] call its own `onDrag`
         *  handler.
         *  - [DragEvent.ACTION_DRAG_ENTERED] Signals to a [View] that the drag point has entered the
         *  bounding box of the [View]. We increase the green background color and return `true` to
         *  report that the drag event was handled successfully.
         *  - [DragEvent.ACTION_DRAG_LOCATION] Sent to a [View] after ACTION_DRAG_ENTERED while the
         *  drag shadow is still within the [View] object's bounding box, but not within a descendant
         *  view that can accept the data. We just return `true` to report that the drag event was
         *  handled successfully.
         *  - [DragEvent.ACTION_DRAG_EXITED] Signals that the user has moved the drag shadow out of
         *  the bounding box of the [View] or into a descendant [View] that can accept the data. We
         *  set the background color to a less intense green when item is not over the target, and
         *  return `true` to report that the drag event was handled successfully.
         *  - [DragEvent.ACTION_DROP] Signals to a [View] that the user has released the drag shadow,
         *  and the drag point is within the bounding box of the [View] and not within a descendant
         *  [View] that can accept the data. We call the [requestDragAndDropPermissions] method with
         *  [event] to create a `DragAndDropPermissions` object bound to this activity and controlling
         *  the access permissions for content URIs associated with the [event]. Then we initialize
         *  our [ClipData.Item] variable `val item` to the item at index 0 of the [ClipData] object
         *  of [event]. Then we branch on the MIME type of the [ClipDescription] of [event]:
         *      - [ClipDescription.MIMETYPE_TEXT_PLAIN] The item is a text item so we simply display
         *      it in a new [TextView] which we add to our [FrameLayout] parameter [v].
         *      - "application/x-arc-uri-list" if the item is a file we read the first 200 characters
         *      and output them in a new [TextView] which we add to our [FrameLayout] parameter [v].
         *      - For these two MIME types we return `true` to report that the drag event was handled
         *      successfully, for all other MIME types we return `false` to have [v] call its own
         *      `onDrag` handler.
         *  - [DragEvent.ACTION_DRAG_ENDED] Signals to a View that the drag and drop operation has
         *  concluded. We restore the background color of [v] to transparent and return `true` to
         *  report that the drag event was handled successfully.
         *  - For all other action values of our [DragEvent] parameter [event] we log the fact that
         *  we received an unknown action type and return `false` to have [v] call its own `onDrag`
         *  handler.
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
                    val item: ClipData.Item = event.clipData.getItemAt(0)
                    when {
                        event.clipDescription.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) -> {
                            //If this is a text item, simply display it in a new TextView.
                            val frameTarget = v as FrameLayout
                            frameTarget.removeAllViews()
                            val droppedText = TextView(mActivity)
                            val params = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
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

                            @SuppressLint("Recycle") // TODO: Recycle ParcelFileDescriptor
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
                                Log.e("MainActivity", "Caught exception: $ex")
                            }
                            val contents = String(bytes)
                            val contentLength = if (contents.length > CHARS_TO_READ) CHARS_TO_READ else 0
                            val frameTarget = v as FrameLayout
                            frameTarget.removeAllViews()
                            val droppedText = TextView(mActivity)
                            val params = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.WRAP_CONTENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            )
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
                    Log.e(
                        "DragDrop Example",
                        "Unknown action type received by DropTargetListener."
                    )
                    false
                }
            }
        }
    }

    /**
     * Custom [OnLongClickListener] that we use for the [TextView] with ID [R.id.text_drag].
     */
    protected class TextViewLongClickListener : OnLongClickListener {
        /**
         * Called when a [View] has been clicked and held. First we initialize our [TextView] variable
         * `val thisTextView` by casting our [View] parameter [v] to a [TextView]. Then we initialize
         * our [String] variable `val dragContent` to the [String] formed by concatenating the text
         * of `thisTextView` to the [String] "Dragged Text:". We initialize our [ClipData.Item]
         * variable to a new instance holding `dragContent`, and initialize our [ClipData] variable
         * `val dragData` to an instance that uses `dragContent` as the label to show to the user
         * describing the clip, [ClipDescription.MIMETYPE_TEXT_PLAIN] as the MIME type (MIME type
         * for a clip holding plain text) and `item` as the contents of the first item in the clip.
         * We initialize our [DragShadowBuilder] variable `val dragShadow` to an instance based on [v]
         * using the default: the resulting drag shadow will have the same appearance and dimensions
         * as [v], with the touch point over the center of the [View]. Then we start the drag, with
         * `dragData` as the [ClipData] object pointing to the data to be transferred by the drag
         * and drop operation, `dragShadow` as the [DragShadowBuilder] object for building the drag
         * shadow, with `null` for the `myLocalState` flags passed to the target, and the flag
         * [View.DRAG_FLAG_GLOBAL] indicating that a drag can cross window boundaries. Finally we
         * return `false` to report that we did not consume the long click.
         *
         * @param v The [View] that was clicked and held.
         * @return `true` if the callback consumed the long click, `false` otherwise.
         */
        override fun onLongClick(v: View): Boolean {
            val thisTextView = v as TextView
            val dragContent: String = "Dragged Text: " + thisTextView.text

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
        /**
         * Maximum number of bytes to read from file descriptor that is dropped on us
         */
        const val MAX_LENGTH: Int = 5000

        /**
         * Number of characters to read and display from the string created from the bytes read
         * from the dropped file descriptor we have read.
         */
        const val CHARS_TO_READ: Int = 200
    }
}