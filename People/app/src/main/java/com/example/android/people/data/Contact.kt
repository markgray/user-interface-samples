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
import androidx.core.net.toUri

/**
 * A contact in the chat app.
 *
 * @param id The unique ID for the contact.
 * @param name The name of the contact.
 * @param icon The resource name of the icon for the contact.
 */
abstract class Contact(
    /**
     * The unique ID for the contact.
     */
    val id: Long,
    /**
     * The name of the contact.
     */
    val name: String,
    /**
     * The resource file name of the icon for the contact.
     */
    val icon: String
) {

    companion object {
        /**
         * The list of sample contacts.
         */
        val CONTACTS: List<Contact> = listOf(
            object : Contact(1L, "Cat", "cat.jpg") {
                override fun reply(text: String) = buildReply().apply { this.text = "Meow" }
            },
            object : Contact(2L, "Dog", "dog.jpg") {
                override fun reply(text: String) = buildReply().apply { this.text = "Woof woof!!" }
            },
            object : Contact(3L, "Parrot", "parrot.jpg") {
                override fun reply(text: String) = buildReply().apply { this.text = text }
            },
            object : Contact(4L, "Sheep", "sheep.jpg") {
                override fun reply(text: String) = buildReply().apply {
                    this.text = "Look at me!"
                    photo = "content://com.example.android.people/photo/sheep_full.jpg".toUri()
                    photoMimeType = "image/jpeg"
                }
            }
        )
    }

    /**
     * The URI for the icon of this contact.
     */
    val iconUri: Uri = "content://com.example.android.people/icon/$id".toUri()

    /**
     * The ID of the shortcut. This is required when you publish a shortcut.
     */
    val shortcutId: String = "contact_$id"

    /**
     * Builds a reply to this contact.
     *
     * @return A [Message.Builder] with the sender and the timestamp pre-filled.
     */
    fun buildReply(): Message.Builder = Message.Builder().apply {
        sender = this@Contact.id
        timestamp = System.currentTimeMillis()
    }

    /**
     * Replies to this contact with the given text.
     *
     * @param text The text to reply with.
     * @return A [Message.Builder] containing the reply.
     */
    abstract fun reply(text: String): Message.Builder

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * This method implements an equivalence relation on non-null object references:
     *  - It is reflexive: for any non-null reference value x, x.equals(x) should return true.
     *  - It is symmetric: for any non-null reference values x and y, x.equals(y) should return
     *  true if and only if y.equals(x) returns true.
     *  - It is transitive: for any non-null reference values x, y, and z, if x.equals(y)
     *  returns true and y.equals(z) returns true, then x.equals(z) should return true.
     *  - It is consistent: for any non-null reference values x and y, multiple invocations of
     *  x.equals(y) consistently return true or consistently return false, provided no
     *  information used in equals comparisons on the objects is modified.
     *  - For any non-null reference value x, x.equals(null) should return false.
     *
     * Two [Contact] objects are considered equal if they have the same [id], [name], and [icon].
     *
     * @param other The reference object with which to compare.
     * @return `true` if this object is the same as the [other] argument; `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Contact

        if (id != other.id) return false
        if (name != other.name) return false
        @Suppress("RedundantIf", "RedundantSuppression")
        if (icon != other.icon) return false

        return true
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash
     * tables such as those provided by `HashMap`.
     *
     * The hash code for a [Contact] object is generated using its [id], [name], and [icon]
     * properties. This ensures that two [Contact] objects that are equal according to the
     * [equals] method will have the same hash code.
     *
     * @return a hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + icon.hashCode()
        return result
    }
}
