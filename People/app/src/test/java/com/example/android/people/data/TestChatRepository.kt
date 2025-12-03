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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * This is like [DefaultChatRepository] except:
 * - The initial chat history can be supplied as a constructor parameter.
 * - It does not wait 5 seconds to receive a reply.
 */
class TestChatRepository(private val chats: Map<Long, Chat>) : ChatRepository {

    /**
     * The ID of the currently activated chat. `0` if no chat is activated.
     */
    var activatedId: Long = 0L

    /**
     * The ID of the currently bubbled chat. `0` if no chat is bubbled.
     */
    var bubbleId: Long = 0L

    /**
     * Get all the contacts.
     *
     * @return A [LiveData] object containing a list of all the contacts.
     */
    override fun getContacts(): LiveData<List<Contact>> {
        return MutableLiveData<List<Contact>>().apply {
            value = chats.values.map { it.contact }
        }
    }

    /**
     * Find a contact by its ID.
     *
     * @param id The ID of the contact.
     * @return A [LiveData] of the contact, or `null` if not found.
     */
    override fun findContact(id: Long): LiveData<Contact?> {
        return MutableLiveData<Contact>().apply {
            value = Contact.CONTACTS.find { it.id == id }
        }
    }

    /**
     * Find the messages in the chat with the given ID.
     *
     * @param id The ID of the chat.
     * @return A [LiveData] of the messages in the chat.
     */
    override fun findMessages(id: Long): LiveData<List<Message>> {
        val chat = chats.getValue(id)
        return object : LiveData<List<Message>>() {

            /**
             * The listener for receiving new messages.
             */
            private val listener = { messages: List<Message> ->
                postValue(messages)
            }

            /**
             * When this [LiveData] becomes active, we immediately post the current value of the
             * messages and start listening to new messages.
             */
            override fun onActive() {
                value = chat.messages
                chat.addListener(listener)
            }

            /**
             * When this [LiveData] becomes inactive, we stop listening to new messages.
             */
            override fun onInactive() {
                chat.removeListener(listener)
            }
        }
    }

    /**
     * Posts a message to the chat with the given ID. The user is always the sender. The contact
     * in the chat will reply to the message.
     *
     * @param id The ID of the chat.
     * @param text The text of the message.
     * @param photoUri The URI of the photo to attach.
     * @param photoMimeType The MIME type of the photo.
     */
    override fun sendMessage(id: Long, text: String, photoUri: Uri?, photoMimeType: String?) {
        val chat = chats.getValue(id)
        chat.addMessage(Message.Builder().apply {
            sender = 0L // User
            this.text = text
            timestamp = System.currentTimeMillis()
            this.photo = photoUri
            this.photoMimeType = photoMimeType
        })
        chat.addMessage(chat.contact.reply(text))
    }

    /**
     * A no-op implementation for the test repository. This function is meant to update
     * a notification, but in this test environment, it does nothing.
     *
     * @param id The ID of the chat whose notification should be updated.
     */
    override fun updateNotification(id: Long) {
    }

    /**
     * Sets the given chat as the currently activated one.
     *
     * @param id The ID of the chat to activate.
     */
    override fun activateChat(id: Long) {
        activatedId = id
    }

    /**
     * Deactivates the currently active chat. In this implementation, it resets the
     * activated chat ID to `0`, effectively deactivating any chat.
     *
     * @param id The ID of the chat to deactivate. This parameter is ignored in this
     * implementation, as any call to this method deactivates the current chat.
     */
    override fun deactivateChat(id: Long) {
        activatedId = 0L
    }

    /**
     * Marks the chat with the given ID as a bubble. In this test implementation,
     * it simply updates the `bubbleId` property.
     *
     * @param id The ID of the chat to show as a bubble.
     */
    override fun showAsBubble(id: Long) {
        bubbleId = id
    }

    /**
     * In this test implementation, we assume any chat can be bubbled.
     *
     * @param id The ID of the chat. This parameter is ignored in this implementation.
     * @return Always `true`.
     */
    override fun canBubble(id: Long): Boolean {
        return true
    }
}
