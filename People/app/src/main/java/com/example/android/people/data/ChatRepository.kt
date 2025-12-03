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

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * A repository for chat-related data.
 */
interface ChatRepository {
    /**
     * Fetches all the contacts.
     */
    fun getContacts(): LiveData<List<Contact>>

    /**
     * Finds the contact with the given ID.
     *
     * @param id The ID of the contact.
     */
    fun findContact(id: Long): LiveData<Contact?>

    /**
     * Finds all the messages in the chat with the given ID.
     *
     * @param id The ID of the chat.
     */
    fun findMessages(id: Long): LiveData<List<Message>>

    /**
     * Sends a message to the chat with the given ID.
     *
     * @param id The ID of the chat.
     * @param text The text of the message.
     * @param photoUri The optional URI of a photo to attach.
     * @param photoMimeType The MIME type of the photo.
     */
    fun sendMessage(id: Long, text: String, photoUri: Uri?, photoMimeType: String?)

    /**
     * Updates the notification for the chat with the given ID.
     *
     * @param id The ID of the chat.
     */
    fun updateNotification(id: Long)

    /**
     * Activates the chat with the given ID. This is typically used to indicate that the user
     * is actively looking at the chat.
     *
     * @param id The ID of the chat.
     */
    fun activateChat(id: Long)

    /**
     * Deactivates the chat with the given ID. This is typically used to indicate that the user
     * is no longer actively looking at the chat.
     *
     * @param id The ID of the chat.
     */
    fun deactivateChat(id: Long)

    /**
     * Shows the chat with the given ID as a bubble.
     *
     * @param id The ID of the chat.
     */
    fun showAsBubble(id: Long)

    /**
     * Returns whether the chat with the given ID can be shown as a bubble.
     *
     * @param id The ID of the chat.
     */
    fun canBubble(id: Long): Boolean
}

/**
 * The default implementation of [ChatRepository].
 *
 * @param notificationHelper A helper for showing notifications.
 * @param executor An executor for running background tasks.
 */
class DefaultChatRepository internal constructor(
    private val notificationHelper: NotificationHelper,
    private val executor: Executor
) : ChatRepository {

    companion object {
        /**
         * The singleton instance of the repository.
         */
        private var instance: DefaultChatRepository? = null

        /**
         * Returns a singleton instance of [DefaultChatRepository].
         *
         * @param context The application context.
         */
        fun getInstance(context: Context): DefaultChatRepository {
            return instance ?: synchronized(this) {
                instance ?: DefaultChatRepository(
                    NotificationHelper(context),
                    Executors.newFixedThreadPool(4)
                ).also {
                    instance = it
                }
            }
        }
    }

    /**
     * The ID of the currently opened chat. `0L` if no chat is open.
     */
    private var currentChat: Long = 0L

    /**
     * All the chats, keyed by the contact ID.
     */
    private val chats = Contact.CONTACTS.associate { contact: Contact ->
        contact.id to Chat(contact)
    }

    init {
        notificationHelper.setUpNotificationChannels()
    }

    /**
     * Fetches all the contacts.
     *
     * @return A [LiveData] of all the contacts.
     */
    @MainThread
    override fun getContacts(): LiveData<List<Contact>> {
        return MutableLiveData<List<Contact>>().apply {
            postValue(Contact.CONTACTS)
        }
    }

    /**
     * Finds the contact with the given ID.
     *
     * @param id The ID of the contact.
     * @return A [LiveData] of the contact.
     */
    @MainThread
    override fun findContact(id: Long): LiveData<Contact?> {
        return MutableLiveData<Contact>().apply {
            postValue(Contact.CONTACTS.find { it.id == id })
        }
    }

    /**
     * Finds all the messages in the chat with the given ID.
     *
     * @param id The ID of the chat.
     * @return A [LiveData] of all the messages.
     */
    @MainThread
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
             * Called when the number of active observers change from 0 to 1. This callback can be
             * used to know that this [LiveData] is being used thus should be kept up to date.
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
     * Sends a message to the chat with the given ID.
     *
     * @param id The ID of the chat.
     * @param text The text of the message.
     * @param photoUri The optional URI of a photo to attach.
     * @param photoMimeType The MIME type of the photo.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    @MainThread
    override fun sendMessage(id: Long, text: String, photoUri: Uri?, photoMimeType: String?) {
        val chat = chats.getValue(id)
        chat.addMessage(Message.Builder().apply {
            sender = 0L // User
            this.text = text
            timestamp = System.currentTimeMillis()
            this.photo = photoUri
            this.photoMimeType = photoMimeType
        })
        executor.execute {
            // The animal is typing...
            Thread.sleep(5000L)
            // Receive a reply.
            chat.addMessage(chat.contact.reply(text))
            // Show notification if the chat is not on the foreground.
            if (chat.contact.id != currentChat) {
                notificationHelper.showNotification(chat, false)
            }
        }
    }

    /**
     * Updates the notification for the chat with the given ID.
     *
     * @param id The ID of the chat.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun updateNotification(id: Long) {
        val chat = chats.getValue(id)
        notificationHelper.showNotification(chat, fromUser = false, update = true)
    }

    /**
     * Activates the chat with the given ID. This is typically used to indicate that the user
     * is actively looking at the chat.
     *
     * @param id The ID of the chat.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun activateChat(id: Long) {
        val chat = chats.getValue(id)
        currentChat = id
        @Suppress("SENSELESS_COMPARISON") // Suggested change would make class less reusable
        val isPrepopulatedMsgs =
            chat.messages.size == 2 && chat.messages[0] != null && chat.messages[1] != null
        notificationHelper.updateNotification(chat, id, isPrepopulatedMsgs)
    }

    /**
     * Deactivates the chat with the given ID. This is typically used to indicate that the user
     * is no longer actively looking at the chat.
     *
     * @param id The ID of the chat.
     */
    override fun deactivateChat(id: Long) {
        if (currentChat == id) {
            currentChat = 0
        }
    }

    /**
     * Shows the chat with the given ID as a bubble.
     *
     * @param id The ID of the chat.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun showAsBubble(id: Long) {
        val chat = chats.getValue(id)
        executor.execute {
            notificationHelper.showNotification(chat, true)
        }
    }

    /**
     * Returns whether the chat with the given ID can be shown as a bubble.
     *
     * @param id The ID of the chat.
     */
    override fun canBubble(id: Long): Boolean {
        val chat = chats.getValue(id)
        return notificationHelper.canBubble(chat.contact)
    }
}
