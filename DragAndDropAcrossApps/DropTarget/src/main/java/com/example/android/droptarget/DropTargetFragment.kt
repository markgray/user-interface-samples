/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.droptarget

import android.app.Activity
import android.content.ClipData
import android.content.ClipDescription
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnDragListener
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestDragAndDropPermissions
import androidx.core.view.DragAndDropPermissionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.android.common.logger.Log.d

/**
 * This sample demonstrates data can be moved between views in different applications via
 * drag and drop.
 *
 * This is the Target app for the drag and drop process. This app uses a
 * [android.widget.ImageView] as the drop target. Images are dragged onto
 * this view from the DragSource app that is also part of this sample.
 *
 * There is also a [android.widget.EditText] widget that can accept dropped text (no
 * extra setup is necessary).
 * To access content URIs requiring permissions, the target app needs to request the
 * [android.view.DragAndDropPermissions] from the Activity via
 * [ActivityCompat.requestDragAndDropPermissions]. This permission will
 * stay either as long as the activity is alive, or until the release() method is called on the
 * [android.view.DragAndDropPermissions] object.
 */
class DropTargetFragment : Fragment() {
    /**
     * The [Uri] of the image that the user has dragged from the `DragSource` app and dropped on us.
     */
    private var mImageUri: Uri? = null

    /**
     * The [CheckBox] in our UI with ID `R.id.release_checkbox` labeled "Release permissions
     * immediately after a drop" which when checked causes us to release the permissions on the
     * [Uri] dropped on us immediately upon drop, otherwise we hold on to them so we can restore
     * the image after the activity has been resized.
     */
    private var mReleasePermissionCheckBox: CheckBox? = null

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and [onActivityCreated]. It is recommended to **only** inflate the layout in this
     * method and move logic that operates on the returned View to [onViewCreated].
     *
     * We initialize our [View] variable `val rootView` to the [View] returned by our [LayoutInflater]
     * parameter [inflater] when its [LayoutInflater.inflate] method inflates our layout file whose
     * resource ID is `R.layout.fragment_droptarget` using our [ViewGroup] parameter [container] for
     * its `LayoutParams` without attaching to it, initialize our [ImageView] variable `val imageView`
     * by finding the [View] in `rootView` with ID `R.id.image_target`, and initialize our
     * [ImageDragListener] variable `val imageDragListener` to a new instance then set the
     * [OnDragListener] of `imageView` to `imageDragListener`.
     *
     * If our [Bundle] parameter [savedInstanceState] is not `null` we are being re-constructed after
     * having been stopped so we want to restore the application state if an image was being displayed.
     * To do this we initialize our [String] variable `val uriString` by retrieving the [String] that
     * was stored in [savedInstanceState] under the key [IMAGE_URI], and if `uriString` is not `null`
     * we initialize our [Uri] field [mImageUri] by using the [Uri.parse] method to parse `uriString`
     * into a [Uri] then we call the `setImageURI` method of `imageView` to have the contents of
     * `imageView` to [mImageUri].
     *
     * Finally we initialize our [CheckBox] field [mReleasePermissionCheckBox] by finding the [View]
     * in `rootView` with ID `R.id.release_checkbox` and return `rootView` to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment.
     * @param container If non-`null`, this is the parent view that the fragment's UI will be
     * attached to. The fragment should not add the view itself, but this can be used to generate
     * the `LayoutParams` of the view.
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed from a
     * previous saved state as given here.
     * @return Return the [View] for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_droptarget, container, false)
        val imageView = rootView.findViewById<View>(R.id.image_target) as ImageView
        val imageDragListener: ImageDragListener = PermissionAwareImageDragListener()
        imageView.setOnDragListener(imageDragListener)

        // Restore the application state if an image was being displayed.
        if (savedInstanceState != null) {
            val uriString = savedInstanceState.getString(IMAGE_URI)
            if (uriString != null) {
                mImageUri = Uri.parse(uriString)
                imageView.setImageURI(mImageUri)
            }
        }
        mReleasePermissionCheckBox = rootView.findViewById<View>(R.id.release_checkbox) as CheckBox
        return rootView
    }

    /**
     * Called to ask the fragment to save its current dynamic state, so it can later be reconstructed
     * in a new instance of its process is restarted. If a new instance of the fragment later needs
     * to be created, the data you place in the [Bundle] here will be available in the [Bundle] given
     * to [onCreate], [onCreateView], and [onActivityCreated].
     *
     * If our [Uri] field [mImageUri] is not `null` we store the [String] version of [mImageUri] under
     * the key [IMAGE_URI] in our [Bundle] parameter [savedInstanceState]. In any case we then call
     * our super's implementation of `onSaveInstanceState`.
     *
     * @param savedInstanceState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mImageUri != null) {
            savedInstanceState.putString(IMAGE_URI, mImageUri.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    /**
     * Custom implementation of our [ImageDragListener] class (which is a custom [OnDragListener])
     * which is aware that we need to request `DragAndDropPermissions` to read the image contained
     * in the [ClipData] of the [DragEvent] dropped on us. It overrides the [processLocation],
     * [setImageUri], and [onDrag] methods of [ImageDragListener].
     */
    private inner class PermissionAwareImageDragListener : ImageDragListener() {
        /**
         * Called when our super's [onDrag] override receives a [DragEvent] whose `action` property
         * is [DragEvent.ACTION_DRAG_LOCATION] which is sent to a [View] after `ACTION_DRAG_ENTERED`
         * while the drag shadow is still within the [View] object's bounding box, but not within
         * a descendant view that can accept the data.  We ignore.
         *
         * @param x the `x` coordinate of the [DragEvent]
         * @param y the `y` coordinate of the [DragEvent]
         */
        override fun processLocation(x: Float, y: Float) {
            // Callback is received when the dragged image enters the drop area.
        }

        /**
         * This override of our super's implementation of `setImageUri` asks for read permissions
         * for its [Uri] parameter [uri] if the `scheme` of [uri] is [ContentResolver.SCHEME_CONTENT]
         * then calls our super's `setImageUri` method to do the rest of the work, and if no permissions
         * are required (all other schemes) just calls our super's `setImageUri` method.
         *
         * First we use our [getExtra] method to read the string from the clip description extras of
         * our [DragEvent] parameter [event] and log them, then we log the message: "Setting image
         * source to:" concatenated to the [String] value of [uri]. We set our [Uri] field [mImageUri]
         * to [uri] and branch on whether the `scheme` property of [uri] is [ContentResolver.SCHEME_CONTENT]
         * or not:
         *  - Scheme is [ContentResolver.SCHEME_CONTENT] - we initialize our [DragAndDropPermissionsCompat]
         *  variable `val dropPermissions` to the value returned by the [requestDragAndDropPermissions]
         *  method that is bound to our [FragmentActivity] and controlling the access permissions for
         *  content URIs associated with our [DragEvent] parameter [event] and if that is `null` we
         *  log the fact that "Drop permission request failed" and return `false`. Otherwise we set
         *  our [Boolean] variable `val result` to the value returned by our super's implementation
         *  of `setImageUri`. If our [CheckBox] field [mReleasePermissionCheckBox] (labeled "Release
         *  permissions immediately after a drop") is checked we call the `release` method of
         *  `dropPermissions` to revoke the permission grant explicitly and log that we did so. In
         *  either case we return `result` to the caller.
         *  - Scheme is **not** [ContentResolver.SCHEME_CONTENT] All other schemes (such as
         *  "android.resource") do not require a permission grant so we just return the value
         *  that our super's implementation of `setImageUri` returns to the caller.
         *
         * @param view The [View] that received the [DragEvent.ACTION_DROP] drag event.
         * @param event The [DragEvent] object for the [DragEvent.ACTION_DROP] drag event
         * @param uri the raw [Uri] contained in [ClipData.Item] at index 0 of the [ClipData] property
         * of the [DragEvent]
         */
        override fun setImageUri(view: View?, event: DragEvent?, uri: Uri?): Boolean {
            // Read the string from the clip description extras.
            d(TAG, "ClipDescription extra: " + getExtra(event!!))
            d(TAG, "Setting image source to: $uri")
            mImageUri = uri
            return if (ContentResolver.SCHEME_CONTENT == uri!!.scheme) {
                // Accessing a "content" scheme Uri requires a permission grant.
                val dropPermissions = requestDragAndDropPermissions(activity as Activity, event)
                d(TAG, "Requesting permissions.")
                if (dropPermissions == null) {
                    // Permission could not be obtained.
                    d(TAG, "Drop permission request failed.")
                    return false
                }
                val result: Boolean = super.setImageUri(view, event, uri)
                if (mReleasePermissionCheckBox!!.isChecked) {
                    /* Release the permissions if you are done with the URI.
                     Note that you may need to hold onto the permission until later if other
                     operations are performed on the content. For instance, releasing the
                     permissions here will prevent onCreateView from properly restoring the
                     ImageView state.
                     If permissions are not explicitly released, the permission grant will be
                     revoked when the activity is destroyed.
                     */
                    dropPermissions.release()
                    d(TAG, "Permissions released.")
                }
                result
            } else {
                // Other schemes (such as "android.resource") do not require a permission grant.
                super.setImageUri(view, event, uri)
            }
        }

        /**
         * Called when a drag event is dispatched to a view. This allows listeners to get a chance
         * to override base [View] behavior.
         *
         * We initialize our [ClipDescription] variable `val clipDescription` to the [ClipDescription]
         * object contained in the [ClipData] object sent to the system as part of the call to
         * `startDragAndDrop`. If `clipDescription` is `null` its mime type is not for an "image"
         * we return `false` to the caller so that the [View] parameter [view] will call its own
         * `onDragEvent` handler. Otherwise we return the value returned by our super's implementation
         * of `onDrag`.
         *
         * @param view The [View] that received the drag event.
         * @param event The [DragEvent] object for the drag event.
         * @return `true` if the drag event was handled successfully, or `false` if the drag event
         * was not handled. Note that `false` will trigger the [View] to call its `onDragEvent`
         * handler.
         */
        override fun onDrag(view: View, event: DragEvent): Boolean {
            // DragTarget is peeking into the MIME types of the dragged event in order to ignore
            // non-image drags completely.
            // DragSource does not do that but rejects non-image content once a drop has happened.
            val clipDescription: ClipDescription? = event.clipDescription
            return if (clipDescription != null && !clipDescription.hasMimeType("image/*")) {
                false
            } else super.onDrag(view, event)
            // Callback received when image is being dragged.
        }
    }

    /**
     * DragEvents can contain additional data packaged in a [PersistableBundle]. Extract the extras
     * from the event and return the [String] stored for the [EXTRA_IMAGE_INFO] entry. We initialize
     * our [ClipDescription] variable `val clipDescription` to the [ClipDescription] of our [DragEvent]
     * parameter [event] and if that is not `null` we initialize our [PersistableBundle] variable
     * `val extras` to the extended data stored in `clipDescription`, and if that is not `null` we
     * return the [String] stored under the key [EXTRA_IMAGE_INFO] in `extras` to the caller. If
     * either of these were `null` we return `null` to the caller.
     *
     * @param event the [DragEvent] that our [PermissionAwareImageDragListener.setImageUri] method
     * received.
     * @return the [String] stored in the [PersistableBundle] extra of the [ClipDescription] of the
     * [DragEvent] parameter [event] under the key [EXTRA_IMAGE_INFO].
     */
    private fun getExtra(event: DragEvent): String? {
        // The extras are contained in the ClipDescription in the DragEvent.
        val clipDescription: ClipDescription? = event.clipDescription
        if (clipDescription != null) {
            val extras: PersistableBundle? = clipDescription.extras
            if (extras != null) {
                return extras.getString(EXTRA_IMAGE_INFO)
            }
        }
        return null
    }

    companion object {
        /**
         * Key that our [onSaveInstanceState] override uses to store the [String] version of the [Uri]
         * field [mImageUri] under in its [Bundle] parameter `savedInstanceState`, and which our
         * [onCreateView] override uses to fetch it from its [Bundle] parameter when our fragment is
         * restarted.
         */
        private const val IMAGE_URI = "IMAGE_URI"

        /**
         * The key under which the `DragSource` app stores a [String] in the [PersistableBundle]
         * extras of the `ClipDescription` of the [DragEvent] which has been dropped on us and which
         * our [getExtra] method retrieves in order for our `setImageUri` method to log it.
         */
        const val EXTRA_IMAGE_INFO: String = "IMAGE_INFO"

        /**
         * TAG used for logging.
         */
        private const val TAG = "DropTargetFragment"
    }
}
