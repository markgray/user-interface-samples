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

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.android.people.data.ChatRepository
import com.example.android.people.data.Contact
import com.example.android.people.data.DefaultChatRepository

/**
 * The ViewModel for the screen that displays a list of contacts.
 *
 * @param application The application.
 * @property repository The repository for the contacts.
 */
class MainViewModel @JvmOverloads constructor(
    application: Application,
    repository: ChatRepository = DefaultChatRepository.getInstance(application)
) : AndroidViewModel(application) {

    /**
     * All the contacts.
     */
    val contacts: LiveData<List<Contact>> = repository.getContacts()
}
