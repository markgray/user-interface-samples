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

/**
 * A listener for changes in a chat thread.
 */
typealias ChatThreadListener = (List<Message>) -> Unit

/**
 * A chat thread.
 *
 * @param contact The contact that the user is chatting with.
 */
class Chat(
    /**
     * The contact that the user is chatting with.
     */
    val contact: Contact
) {

    /**
     * The listeners for changes in the chat thread.
     */
    private val listeners = mutableListOf<ChatThreadListener>()

    /**
     * The mutable list of messages in this chat thread.
     * The first two messages are seed data.
     */
    private val _messages = mutableListOf(
        Message(1L, contact.id, "Send me a message", null, null, System.currentTimeMillis()),
        Message(2L, contact.id, "I will reply in 5 seconds", null, null, System.currentTimeMillis())
    )

    /**
     * The messages in this chat thread.
     */
    val messages: List<Message>
        get() = _messages

    /**
     * Adds a listener to this chat thread.
     *
     * @param listener The listener to add.
     */
    fun addListener(listener: ChatThreadListener) {
        listeners.add(listener)
    }

    /**
     * Removes a listener from this chat thread.
     *
     * @param listener The listener to remove.
     */
    fun removeListener(listener: ChatThreadListener) {
        listeners.remove(listener)
    }

    /**
     * Adds a message to this chat thread.
     *
     * @param builder The builder for the message to add.
     */
    fun addMessage(builder: Message.Builder) {
        builder.id = _messages.last().id + 1
        _messages.add(builder.build())
        listeners.forEach { listener -> listener(_messages) }
    }
}
