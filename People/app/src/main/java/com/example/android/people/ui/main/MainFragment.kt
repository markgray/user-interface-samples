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

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.android.people.R
import com.example.android.people.data.Contact
import com.example.android.people.databinding.MainFragmentBinding
import com.example.android.people.getNavigationController
import com.example.android.people.ui.viewBindings

/**
 * The main screen of the app, which shows a list of contacts.
 *
 * This fragment is responsible for displaying a list of contacts in a `RecyclerView`.
 * When a contact is clicked, it navigates to the chat screen for that contact.
 */
class MainFragment : Fragment(R.layout.main_fragment) {

    /**
     * The binding for the root view of this fragment.
     * This is a view-binding-backed property for convenient and type-safe access to the views.
     */
    private val binding by viewBindings(MainFragmentBinding::bind)

    /**
     * Called when the fragment is first created. This is where we will set the exit transition for
     * the fragment.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = TransitionInflater.from(context).inflateTransition(R.transition.slide_top)
    }

    /**
     * Called immediately after [onCreateView] has returned, but before
     * any saved state has been restored in to the view. This gives
     * subclasses a chance to initialize themselves once they know
     * their view hierarchy has been completely created.
     *
     * In this method, we set up the `RecyclerView` with its adapter and layout manager,
     * and observe the `contacts` LiveData from the `MainViewModel` to update the UI
     * when the data changes.
     *
     * @param view The View returned by [onCreateView].
     * @param savedInstanceState If non-`null`, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val navigationController = getNavigationController()
        navigationController.updateAppBar(false)
        val viewModel: MainViewModel by viewModels()

        val contactAdapter = ContactAdapter { id: Long ->
            navigationController.openChat(id = id, prepopulateText = null)
        }
        viewModel.contacts.observe(viewLifecycleOwner) { contacts: List<Contact> ->
            contactAdapter.submitList(contacts)
        }
        binding.contacts.run {
            layoutManager = LinearLayoutManager(view.context)
            setHasFixedSize(true)
            adapter = contactAdapter
        }
    }
}
