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

package com.example.android.people.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.people.data.Chat
import com.example.android.people.data.Contact
import com.example.android.people.data.TestChatRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Unit tests for [MainViewModel].
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class MainViewModelTest {

    /**
     * A JUnit rule that swaps the background executor used by the Architecture Components with a
     * different one which executes each task synchronously.
     *
     * This is necessary for testing `LiveData` because it ensures that any background operations
     * happen on the same thread, allowing test results to be synchronous and repeatable.
     */
    @get:Rule
    val instantTaskExecutorRule: InstantTaskExecutorRule = InstantTaskExecutorRule()

    /**
     * A dummy list of contacts used for testing purposes.
     */
    private val dummyContacts = Contact.CONTACTS

    /**
     * Creates an instance of [MainViewModel] for testing.
     *
     * This function initializes the ViewModel on the main thread because `MainViewModel` uses
     * `SavedStateHandle`, which requires it to be created on the main thread. It uses a
     * [TestChatRepository] pre-populated with dummy contacts.
     *
     * @return A new instance of [MainViewModel].
     */
    private fun createViewModel(): MainViewModel {
        var viewModel: MainViewModel? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            viewModel = MainViewModel(
                ApplicationProvider.getApplicationContext(),
                TestChatRepository(dummyContacts.associate { contact ->
                    contact.id to Chat(contact)
                })
            )
        }
        return viewModel!!
    }

    /**
     * Verifies that the [MainViewModel] is populated with a list of contacts upon creation.
     *
     * This test creates a [MainViewModel] instance and checks if the `contacts` LiveData
     * holds the expected list of dummy contacts, ensuring the ViewModel correctly loads
     * and exposes the contact data from the repository.
     */
    @Test
    fun hasListOfContacts() {
        val viewModel = createViewModel()
        val contacts = viewModel.contacts.value
        assertThat(contacts).isEqualTo(dummyContacts)
    }

}
