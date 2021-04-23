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

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
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
     * The [CheckBox] in our UI with ID [R.id.release_checkbox] labeled "Release permissions
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

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        if (mImageUri != null) {
            savedInstanceState.putString(IMAGE_URI, mImageUri.toString())
        }
        super.onSaveInstanceState(savedInstanceState)
    }

    private inner class PermissionAwareImageDragListener : ImageDragListener() {
        override fun processLocation(x: Float, y: Float) {
            // Callback is received when the dragged image enters the drop area.
        }

        override fun setImageUri(view: View, event: DragEvent, uri: Uri): Boolean {
            // Read the string from the clip description extras.
            d(TAG, "ClipDescription extra: " + getExtra(event))
            d(TAG, "Setting image source to: $uri")
            mImageUri = uri
            return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
                // Accessing a "content" scheme Uri requires a permission grant.
                val dropPermissions = ActivityCompat
                    .requestDragAndDropPermissions(activity, event)
                d(TAG, "Requesting permissions.")
                if (dropPermissions == null) {
                    // Permission could not be obtained.
                    d(TAG, "Drop permission request failed.")
                    return false
                }
                val result = super.setImageUri(view, event, uri)
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

        override fun onDrag(view: View, event: DragEvent): Boolean {
            // DragTarget is peeking into the MIME types of the dragged event in order to ignore
            // non-image drags completely.
            // DragSource does not do that but rejects non-image content once a drop has happened.
            val clipDescription = event.clipDescription
            return if (clipDescription != null && !clipDescription.hasMimeType("image/*")) {
                false
            } else super.onDrag(view, event)
            // Callback received when image is being dragged.
        }
    }

    /**
     * DragEvents can contain additional data packaged in a [PersistableBundle].
     * Extract the extras from the event and return the String stored for the
     * [.EXTRA_IMAGE_INFO] entry.
     */
    private fun getExtra(event: DragEvent): String? {
        // The extras are contained in the ClipDescription in the DragEvent.
        val clipDescription = event.clipDescription
        if (clipDescription != null) {
            val extras = clipDescription.extras
            if (extras != null) {
                return extras.getString(EXTRA_IMAGE_INFO)
            }
        }
        return null
    }

    companion object {
        private const val IMAGE_URI = "IMAGE_URI"
        const val EXTRA_IMAGE_INFO = "IMAGE_INFO"
        private const val TAG = "DropTargetFragment"
    }
}