/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.example.android.people.data

import android.net.Uri

/**
 * Represents a message in a chat.
 *
 * @param id The unique ID of the message.
 * @param sender The ID of the sender. `0L` for the current user.
 * @param text The text content of the message.
 * @param photoUri The URI of the photo attached to the message, if any.
 * @param photoMimeType The MIME type of the attached photo.
 * @param timestamp The time the message was sent, in milliseconds.
 */
data class Message(
    /**
     * The unique ID of the message.
     */
    val id: Long,
    /**
     * The ID of the sender. `0L` for the current user.
     */
    val sender: Long,
    /**
     * The text content of the message.
     */
    val text: String,
    /**
     * The URI of the photo attached to the message, if any.
     */
    val photoUri: Uri?,
    /**
     * The MIME type of the attached photo.
     */
    val photoMimeType: String?,
    /**
     * The time the message was sent, in milliseconds.
     */
    val timestamp: Long
) {

    /**
     * `true` if the message is from a contact, `false` if it's from the current user.
     */
    val isIncoming: Boolean
        get() = sender != 0L

    /**
     * The builder for [Message].
     */
    class Builder {
        /**
         * The unique ID of the message.
         */
        var id: Long? = null

        /**
         * The ID of the sender. `0L` for the current user.
         */
        var sender: Long? = null

        /**
         * The text content of the message.
         */
        var text: String? = null

        /**
         * The URI of the photo attached to the message, if any.
         */
        var photo: Uri? = null

        /**
         * The MIME type of the attached photo.
         */
        var photoMimeType: String? = null

        /**
         * The time the message was sent, in milliseconds.
         */
        var timestamp: Long? = null

        /**
         * Builds a [Message] object.
         * @return The built [Message].
         * @throws IllegalStateException If any of the required fields (`id`, `sender`, `text`, `timestamp`) are not set.
         */
        fun build(): Message = Message(
            id = id!!,
            sender = sender!!,
            text = text!!,
            photoUri = photo,
            photoMimeType = photoMimeType,
            timestamp = timestamp!!
        )
    }
}
