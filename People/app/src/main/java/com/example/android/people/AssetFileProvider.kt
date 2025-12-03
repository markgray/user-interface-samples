/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.example.android.people

import android.content.ContentProvider
import android.content.ContentValues
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.android.people.data.Contact

/**
 * A simple [ContentProvider] that can provide files from this app's assets.
 *
 * The authority of this provider is `com.example.android.people.provider`.
 *
 * It can provide two kinds of files:
 *
 *  - content://com.example.android.people.provider/icon/<id>
 *  This returns the icon of the contact with the given `id`.
 *  - content://com.example.android.people.provider/photo/<filename>
 *  This returns the photo specified by the `filename`.
 *
 * All the other operations are not supported.
 */
class AssetFileProvider : ContentProvider() {

    /**
     * We don't need to do any initialization, so we just return `true` directly.
     *
     * @return `true` to indicate that the provider was successfully loaded.
     */
    override fun onCreate(): Boolean {
        return true
    }

    /**
     * The MIME type of the content.
     *
     * We only handle `icon` Uris, which are JPEGs. For all other Uris, we return a generic
     * binary stream MIME type.
     *
     * @param uri The Uri to query.
     * @return A MIME type for the content, or `null` if the URL is invalid.
     */
    override fun getType(uri: Uri): String? {
        val segments = uri.pathSegments
        return when (segments[0]) {
            "icon" -> MimeTypeMap.getSingleton().getMimeTypeFromExtension("jpg")
            else -> "application/octet-stream"
        }
    }

    /**
     * Opens a file from the app's assets.
     *
     * The `uri` is expected to be in one of two formats:
     *  - `content://<authority>/icon/<id>`: Returns the icon for the contact with the specified `id`.
     *  - `content://<authority>/photo/<filename>`: Returns the photo with the specified `filename`.
     *
     * The `mode` is ignored.
     *
     * @param uri The Uri to open.
     * @param mode The access mode, which is ignored.
     * @return An [AssetFileDescriptor] for the file, or `null` if the Uri is invalid or the file
     * cannot be found.
     */
    override fun openAssetFile(uri: Uri, mode: String): AssetFileDescriptor? {
        val segments = uri.pathSegments
        return when (segments[0]) {
            "icon" -> {
                val id = segments[1].toLong()
                Contact.CONTACTS.find { it.id == id }?.let { contact ->
                    context?.resources?.assets?.openFd(contact.icon)
                }
            }

            "photo" -> {
                val filename = segments[1]
                context?.resources?.assets?.openFd(filename)
            }

            else -> null
        }
    }

    /**
     * This provider doesn't support querying.
     *
     * @param uri The Uri to query.
     * @param projection The list of columns to return.
     * @param selection A selection criteria to apply when filtering rows.
     * @param selectionArgs The values for the selection criteria.
     * @param sortOrder The sort order for the returned rows.
     * @throws UnsupportedOperationException
     */
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        throw UnsupportedOperationException("No query")
    }

    /**
     * This provider is read-only.
     *
     * @param uri The Uri to insert.
     * @param values The values to insert.
     * @throws UnsupportedOperationException
     */
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw UnsupportedOperationException("No insert")
    }

    /**
     * This provider is read-only.
     *
     * @param uri The URI to update.
     * @param values The new column values.
     * @param selection The selection criteria to apply.
     * @param selectionArgs The values for the selection criteria.
     * @return The number of rows updated.
     * @throws UnsupportedOperationException
     */
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw UnsupportedOperationException("No update")
    }

    /**
     * This provider is read-only.
     *
     * @param uri The URI to delete.
     * @param selection The selection criteria to apply.
     * @param selectionArgs The values for the selection criteria.
     * @return The number of rows deleted.
     * @throws UnsupportedOperationException
     */
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw UnsupportedOperationException("No delete")
    }
}
