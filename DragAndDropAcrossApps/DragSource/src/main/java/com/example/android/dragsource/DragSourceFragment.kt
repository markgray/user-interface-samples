/*
 * Copyright 2015, The Android Open Source Project
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
import android.content.ClipDescription
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.core.view.DragStartHelper
import androidx.core.view.DragStartHelper.OnDragStartListener
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date

/**
 * This sample demonstrates data can be moved between views within the app or between different
 * apps via drag and drop.
 *
 * This is the source app for the drag and drop sample. This app contains several
 * [android.widget.ImageView] widgets which can be a drag source. Images can be dropped
 * to a drop target area within the same app or in the DropTarget app (a separate app in this
 * sample).
 *
 *
 * There is also one [android.widget.EditText] widget that can be a drag source (no extra
 * setup is necessary).
 *
 *
 * To enable cross application drag and drop, the [android.view.View.DRAG_FLAG_GLOBAL]
 * permission needs to be passed to the [android.view.View.startDragAndDrop] method. If a Uri
 * requiring permission grants is being sent, then the
 * [android.view.View.DRAG_FLAG_GLOBAL_URI_READ] and/or the
 * [android.view.View.DRAG_FLAG_GLOBAL_URI_WRITE] flags must be used also.
 */
class DragSourceFragment : Fragment() {
    /**
     * Uri of the ImageView source when set.
     */
    private var mLocalImageUri: Uri? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dragsource, container, false)

        // Set up two image views for global drag and drop with a permission grant.
        var imageUri = getFileUri(R.drawable.image1, "image1.png")
        var imageView = view.findViewById<View>(R.id.image_one) as ImageView
        setUpDraggableImage(imageView, imageUri)
        imageView.setImageURI(imageUri)
        imageUri = getFileUri(R.drawable.image2, "image2.png")
        imageView = view.findViewById<View>(R.id.image_two) as ImageView
        setUpDraggableImage(imageView, imageUri)
        imageView.setImageURI(imageUri)

        // Set up the local drop target area.
        val localImageTarget = view.findViewById<View>(R.id.local_target) as ImageView
        localImageTarget.setOnDragListener(object : ImageDragListener() {
            override fun setImageUri(view: View, event: DragEvent, uri: Uri): Boolean {
                mLocalImageUri = uri
                Log.d(TAG, "Setting local image to: $uri")
                return super.setImageUri(view, event, uri)
            }
        })
        if (savedInstanceState != null) {
            val uriString = savedInstanceState.getString(IMAGE_URI)
            if (uriString != null) {
                mLocalImageUri = Uri.parse(uriString)
                Log.d(TAG, "Restoring local image to: $mLocalImageUri")
                localImageTarget.setImageURI(mLocalImageUri)
            }
        }
        return view
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mLocalImageUri != null) {
            savedInstanceState.putString(IMAGE_URI, mLocalImageUri.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    private fun setUpDraggableImage(imageView: ImageView, imageUri: Uri?) {

        // Set up a listener that starts the drag and drop event with flags and extra data.
        val listener = OnDragStartListener { view, helper ->
            Log.d(TAG, "Drag start event received from helper.")

            // Use a DragShadowBuilder
            val shadowBuilder: DragShadowBuilder = object : DragShadowBuilder(view) {
                override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
                    super.onProvideShadowMetrics(shadowSize, shadowTouchPoint)
                    // Notify the DragStartHelper of point where the view was touched.
                    helper.getTouchPosition(shadowTouchPoint)
                    Log.d(TAG, "View was touched at: $shadowTouchPoint")
                }
            }

            // Set up the flags for the drag event.
            // Enable drag and drop across apps (global)
            // and require read permissions for this URI.
            val flags = View.DRAG_FLAG_GLOBAL or View.DRAG_FLAG_GLOBAL_URI_READ

            // Add an optional clip description that that contains an extra String that is
            // read out by the target app.
            val clipDescription = ClipDescription("", arrayOf(
                context!!.contentResolver.getType(imageUri!!)))
            // Extras are stored within a PersistableBundle.
            val extras = PersistableBundle(1)
            // Add a String that the target app will display.
            extras.putString(EXTRA_IMAGE_INFO,
                "Drag Started at " + Date())
            clipDescription.extras = extras

            // The ClipData object describes the object that is being dragged and dropped.
            val clipData = ClipData(clipDescription, ClipData.Item(imageUri))
            Log.d(TAG, "Created ClipDescription. Starting drag and drop.")
            // Start the drag and drop event.
            view.startDragAndDrop(clipData, shadowBuilder, null, flags)
        }

        // Use the DragStartHelper to detect drag and drop events and use the OnDragStartListener
        // defined above to start the event when it has been detected.
        val helper = DragStartHelper(imageView, listener)
        helper.attach()
        Log.d(TAG, "DragStartHelper attached to view.")
    }

    /**
     * Copy a drawable resource into local storage and makes it available via the
     * [FileProvider].
     *
     * @see FileProvider
     * @see FileProvider.getUriForFile
     */
    private fun getFileUri(sourceResourceId: Int, targetName: String): Uri? {
        // Create the images/ sub directory if it does not exist yet.
        val filePath = File(context!!.filesDir, "images")
        if (!filePath.exists() && !filePath.mkdir()) {
            return null
        }

        // Copy a drawable from resources to the internal directory.
        val newFile = File(filePath, targetName)
        if (!newFile.exists()) {
            copyImageResourceToFile(sourceResourceId, newFile)
        }

        // Make the file accessible via the FileProvider and retrieve its URI.
        return FileProvider.getUriForFile(context!!, CONTENT_AUTHORITY, newFile)
    }

    /**
     * Copy a PNG resource drawable to a {@File}.
     */
    private fun copyImageResourceToFile(resourceId: Int, filePath: File) {
        val image = BitmapFactory.decodeResource(resources, resourceId)
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(filePath)
            image.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        /**
         * Name of saved data that stores the dropped image URI on the local ImageView when set.
         */
        private const val IMAGE_URI = "IMAGE_URI"

        /**
         * Name of the parameter for a [ClipData] extra that stores a text describing the dragged
         * image.
         */
        const val EXTRA_IMAGE_INFO = "IMAGE_INFO"
        private const val TAG = "DragSourceFragment"
        private const val CONTENT_AUTHORITY = "com.example.android.dragsource.fileprovider"
    }
}