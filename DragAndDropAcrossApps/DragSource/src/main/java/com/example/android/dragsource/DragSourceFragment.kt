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
import android.view.View.OnDragListener
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
 * There is also one [android.widget.EditText] widget that can be a drag source (no extra
 * setup is necessary).
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

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. It is recommended to **only** inflate the layout in this
     * method and move logic that operates on the returned View to [onViewCreated].
     *
     * We use our [LayoutInflater] parameter [inflater] to inflate the layout file with resource ID
     * [R.layout.fragment_dragsource], using our [ViewGroup] parameter [container] to generate its
     * `LayoutParams` without attaching to it and use the [View] returned to initialize our variable
     * `val view`.
     *
     * Next we set up the two image views in our UI for global drag and drop with a permission grant
     * by:
     *  - Initializing our [Uri] variable `var imageUri` to the [Uri] returned by our [getFileUri]
     *  for the drawable with ID [R.drawable.image1] and the target name "image1.png"
     *  - Initializing our [ImageView] variable `var imageView` by finding the [View] in our UI with
     *  ID [R.id.image_one].
     *  - Calling our method [setUpDraggableImage] with `imageView` and `imageUri` to configure them
     *  for drag and drop use.
     *  - Setting our [Uri] variable `imageUri` to the [Uri] returned by our [getFileUri] for the
     *  drawable with ID [R.drawable.image2] and the target name "image2.png"
     *  - Setting our [ImageView] variable `imageView` by finding the [View] in our UI with ID
     *  [R.id.image_two].
     *  - Calling our method [setUpDraggableImage] with `imageView` and `imageUri` to configure them
     *  for drag and drop use.
     *
     * Next we set up the local drop target area by:
     *  - Initializing our [ImageView] variable `val localImageTarget` by finding the [View] in our
     *  UI with ID [R.id.local_target]
     *  - Setting its [OnDragListener] to an anonymous [ImageDragListener] whose `setImageUri`
     *  override will set our [Uri] field [mLocalImageUri] to the [Uri] passed it then if the [View]
     *  passed it is an [ImageView], set the content of the [View] to the [Uri] passed it.
     *
     * If our [Bundle] parameter [savedInstanceState] is not `null`, we set our [String] variable
     * `val uriString` to the [String] stored in [savedInstanceState] under the key [IMAGE_URI],
     * and if `uriString` is not `null` we restore our [Uri] field [mLocalImageUri] to the [Uri]
     * that the [Uri.parse] method parses from `uriString` and set the content of the [ImageView]
     * `localImageTarget` to the [Uri] field [mLocalImageUri].
     *
     * Finally whether or not [savedInstanceState] is `null` we return `view` to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate any views
     * in the fragment,
     * @param container If non-`null`, this is the parent view that the fragment's UI will
     * be attached to. The fragment should not add the view itself, but this can be used to
     * generate the `LayoutParams` of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed
     * from a previous saved state as given here.
     * @return Return the [View] for the fragment's UI, or `null`.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dragsource, container, false)

        // Set up two image views for global drag and drop with a permission grant.
        var imageUri: Uri? = getFileUri(R.drawable.image1, "image1.png")
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

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be
     * reconstructed in a new instance of its process is restarted. If a new instance of
     * the fragment later needs to be created, the data you place in the [Bundle] here
     * will be available in the [Bundle] given to [onCreate], [onCreateView], and
     * [onActivityCreated].
     *
     * If our [Uri] field [mLocalImageUri] is not `null` we store a string representation of it in
     * [savedInstanceState] under the key [IMAGE_URI]. Finally we call our super's implementation of
     * `onSaveInstanceState`.
     *
     * @param savedInstanceState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mLocalImageUri != null) {
            savedInstanceState.putString(IMAGE_URI, mLocalImageUri.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Called to configure the [ImageView] parameter [imageView] to be dragged and dropped with the
     * [Uri] parameter [imageUri] used as the [Uri] of the [ClipData] that is dropped.
     *
     * First we initialize our [OnDragStartListener] variable `val listener` to an instance whose
     * lambda override of the [OnDragStartListener.onDragStart] method:
     *  - Initializes its [DragShadowBuilder] variable `val shadowBuilder` to an anonymous instance
     *  whose `onProvideShadowMetrics` override calls its super's implementation of `onProvideShadowMetrics`
     *  alls the `getTouchPosition` of the [DragStartHelper] passed to the [OnDragStartListener] have
     *  it compute the position of the touch event that started the drag operation based on its
     *  [Point] parameter `shadowTouchPoint` (this will notify the [DragStartHelper] where the view
     *  was touched) and then log where it was touched.
     *  - Sets up the `val flags` for the drag event by or'ing the flag [View.DRAG_FLAG_GLOBAL]
     *  (enables drag and drop across apps ie. global) and [View.DRAG_FLAG_GLOBAL_URI_READ] (require
     *  read permissions for the URI).
     *  - Initializes its [ClipDescription] variable `val clipDescription` with an instance whose
     *  Label to show to the user describing this clip is the empty [String], and whose array of
     *  MIME types this data is available as consisting of the MIME type of [Uri] parameter [imageUri]
     *  as determined by the `getType` method of a ContentResolver instance for our application's
     *  package (this will be read out by the target app and logged).
     *  - Initializes its [PersistableBundle] variable `val extras` to a new instance with an
     *  initial capacity of 1, then stores a string in `extras` under the key [EXTRA_IMAGE_INFO]
     *  consisting of the [String] created by the string template "Drag Started at ${Date()}", and
     *  then sets the `extras` property of `clipDescription` to `extras`.
     *  - Initializes its [ClipData] variable `val clipData` to a new instance whose ClipDescription
     *  describing the clip contents is `clipDescription` and whose contents of the first item in
     *  the clip is the [ClipData.Item] constructed for the [Uri] parameter [imageUri].
     *  - After logging the message "Created ClipDescription. Starting drag and drop." the lambda
     *  then calls the [View.startDragAndDrop] method of `view` (the [View] being dragged) with
     *  `clipData` as the [ClipData] object pointing to the data to be transferred by the drag and
     *  drop operation, `shadowBuilder` as the [View.DragShadowBuilder] object for building the drag
     *  shadow, `null` as the "myLocalState", and `flags` as the Flags that control the drag and
     *  drop operation.
     *
     * Having constructed and configured our [OnDragStartListener] variable `listener` we next
     * initialize our [DragStartHelper] variable `val helper` to a new instance which uses our
     * [ImageView] parameter [imageView] as the [View] the helper is associated with, and our
     * variable `listener` as the [OnDragStartListener] to be invoked when a drag start gesture
     * is detected. We call the [DragStartHelper.attach] method of `helper` to attach the helper
     * to its view and then log the message "DragStartHelper attached to view."
     *
     * @param imageView the [ImageView] we are to create and configure a [DragStartHelper] for.
     * @param imageUri the [Uri] which will be used as the [ClipData.Item] of the [ClipData] which
     * will be passed to the [View.startDragAndDrop] method of the view over which a drag start
     * gesture has been detected by our [OnDragStartListener].
     */
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
            val clipDescription = ClipDescription(
                "",
                arrayOf(context!!.contentResolver.getType(imageUri!!))
            )
            // Extras are stored within a PersistableBundle.
            val extras = PersistableBundle(1)
            // Add a String that the target app will display.
            extras.putString(EXTRA_IMAGE_INFO, "Drag Started at ${Date()}")
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

        /**
         * TAG used for logging
         */
        private const val TAG = "DragSourceFragment"
        private const val CONTENT_AUTHORITY = "com.example.android.dragsource.fileprovider"
    }
}