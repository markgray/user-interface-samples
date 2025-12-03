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

package com.example.android.people.ui.chat

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.people.data.Chat
import com.example.android.people.data.Contact
import com.example.android.people.data.TestChatRepository
import com.example.android.people.observedValue
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * A test for [ChatViewModel]. This is an instrumented test because it uses Android classes
 * like [ApplicationProvider].
 *
 * We use a [TestChatRepository] to provide dummy data to the ViewModel.
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class ChatViewModelTest {

    /**
     * A JUnit rule that swaps the background executor used by the Architecture Components with a
     * different one which executes each task synchronously.
     *
     * This is needed because LiveData uses a background thread by default, but in tests, we want
     * to run them synchronously and get the result instantly.
     */
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * The list of dummy contacts used in this test.
     */
    private val dummyContacts = Contact.CONTACTS

    /**
     * The view model for the chat screen. This is the class under test.
     */
    private lateinit var viewModel: ChatViewModel

    /**
     * The repository that is used to retrieve and store chat data. This is a test-specific
     * implementation of the repository.
     */
    private lateinit var repository: TestChatRepository

    /**
     * Creates and initializes the [ChatViewModel] and its dependencies. This method is called
     * before each test is executed, as annotated by `@Before`. It sets up a [TestChatRepository]
     * with dummy data and then instantiates the [ChatViewModel] on the main thread, passing the
     * repository and application context to it.
     */
    @Before
    fun createViewModel() {
        repository = TestChatRepository(dummyContacts.associate { contact ->
            contact.id to Chat(contact)
        })
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            viewModel = ChatViewModel(ApplicationProvider.getApplicationContext(), repository)
        }
    }

    /**
     * Verifies that the ViewModel correctly loads a contact and their messages when a chat ID is set.
     * It sets a specific chat ID, marks the chat as foreground, and then asserts that:
     *  1. The `contact` LiveData is updated with the correct contact from the dummy data.
     *  2. The `messages` LiveData is populated with the expected number of messages for that contact.
     *  3. The repository's `activatedId` is correctly set to the specified chat ID, indicating
     *  that the correct chat has been activated.
     */
    @Test
    fun hasContactAndMessages() {
        viewModel.setChatId(1L)
        viewModel.foreground = true
        assertThat(viewModel.contact.observedValue()).isEqualTo(dummyContacts.find { it.id == 1L })
        assertThat(viewModel.messages.observedValue()).hasSize(2)
        assertThat(repository.activatedId).isEqualTo(1L)
    }

    /**
     * Verifies that sending a message updates the message list and triggers a reply.
     * It sets a chat ID, sends a message "a", and then asserts that:
     *  1. The total number of messages increases by two (the sent message and the reply).
     *  2. The sent message "a" is present at the expected position in the message list.
     *  3. The reply "Meow" is present as the last message in the list.
     */
    @Test
    fun sendAndReceiveReply() {
        viewModel.setChatId(1L)
        viewModel.send("a")
        val messages = viewModel.messages.observedValue()
        assertThat(messages).hasSize(4)
        assertThat(messages[2].text).isEqualTo("a")
        assertThat(messages[3].text).isEqualTo("Meow")
    }

    /**
     * Verifies that the `showAsBubble` function correctly updates the repository.
     * It sets a chat ID, asserts that the initial bubble ID in the repository is 0,
     * then calls `showAsBubble()` on the ViewModel. Finally, it asserts that the
     * repository's bubble ID has been updated to the chat ID that was set, confirming
     * that the ViewModel has correctly requested the chat to be displayed as a bubble.
     */
    @Test
    fun showAsBubble() {
        viewModel.setChatId(1L)
        assertThat(repository.bubbleId).isEqualTo(0L)
        viewModel.showAsBubble()
        assertThat(repository.bubbleId).isEqualTo(1L)
    }

}
