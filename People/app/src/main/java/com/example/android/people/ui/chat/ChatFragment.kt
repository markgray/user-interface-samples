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

import android.content.Intent
import android.content.LocusId
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.android.people.R
import com.example.android.people.VoiceCallActivity
import com.example.android.people.data.Contact
import com.example.android.people.databinding.ChatFragmentBinding
import com.example.android.people.getNavigationController
import com.example.android.people.ui.viewBindings

/**
 * The chat screen. This is used in the full app (MainActivity) as well as in the expanded Bubble
 * (BubbleActivity).
 */
class ChatFragment : Fragment(R.layout.chat_fragment) {

    companion object {
        /**
         * The argument name for the contact ID.
         */
        private const val ARG_ID = "id"

        /**
         * The argument name for a boolean indicating whether this chat is in the foreground.
         */
        private const val ARG_FOREGROUND = "foreground"

        /**
         * The argument name for the prepopulated text.
         */
        private const val ARG_PREPOPULATE_TEXT = "prepopulate_text"

        /**
         * Creates a new instance of [ChatFragment].
         *
         * @param id The ID of the contact to chat with.
         * @param foreground Whether the fragment is started in the foreground.
         * @param prepopulateText The text to prepopulate in the text input field.
         * @return A new instance of [ChatFragment].
         */
        fun newInstance(
            id: Long,
            foreground: Boolean,
            prepopulateText: String? = null
        ): ChatFragment =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_ID, id)
                    putBoolean(ARG_FOREGROUND, foreground)
                    putString(ARG_PREPOPULATE_TEXT, prepopulateText)
                }
            }
    }

    /**
     * The [ViewModel] for this fragment.
     */
    private val viewModel: ChatViewModel by viewModels()

    /**
     * Scoped to the lifecycle of the fragment's view (between `onCreateView` and `onDestroyView`),
     * this property provides access to the views in the layout file `chat_fragment.xml`.
     * The view binding is created by calling the `ChatFragmentBinding.bind` method, which
     * takes the fragment's root view as an argument.
     */
    private val binding by viewBindings(ChatFragmentBinding::bind)

    /**
     * Called when the fragment is first created. We set the enter transition for this fragment to
     * be a slide from the bottom.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.slide_bottom)
    }

    /**
     * Our  [MenuProvider]
     */
    private val menuProvider: MenuProvider = object : MenuProvider {
        /**
         * Initialize the contents of the Fragment host's standard options menu. We use our
         * [MenuInflater] parameter [menuInflater] to inflate our menu layout file
         * `R.menu.chat` into our [Menu] parameter [menu].
         *
         * @param menu The options menu in which you place your items.
         * @param menuInflater a [MenuInflater] you can use to inflate an XML menu file with.
         */
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.chat, menu)
            menu.findItem(R.id.action_show_as_bubble)?.let { item: MenuItem ->
                viewModel.showAsBubbleVisible.observe(viewLifecycleOwner) { visible: Boolean ->
                    item.isVisible = visible
                }
            }
        }

        /**
         * Called by the [MenuHost] when a [MenuItem] is selected from the menu. We do not implement
         * anything yet, this will be done in `MarsRealEstateFinal`.
         *
         * @param menuItem the menu item that was selected
         * @return `true` if the given menu item is handled by this menu provider, `false` otherwise.
         */
        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return when (menuItem.itemId) {
                R.id.action_show_as_bubble -> {
                    viewModel.showAsBubble()
                    if (isAdded) {
                        parentFragmentManager.popBackStack()
                    }
                    true
                }

                else -> return false
            }
        }
    }

    /**
     * Called immediately after `onCreateView` has returned, but before any saved state has been
     * restored in to the view. This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.
     *
     * We initialize the UI, including setting up the menu, RecyclerView for messages,
     * and observers for LiveData from the [ChatViewModel]. We also set up listeners for
     * UI interactions like sending messages and making voice calls.
     *
     * @param view The View returned by `onCreateView`.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
        val id = arguments?.getLong(ARG_ID)
        if (id == null) {
            parentFragmentManager.popBackStack()
            return
        }
        val prepopulateText = arguments?.getString(ARG_PREPOPULATE_TEXT)
        val navigationController = getNavigationController()

        viewModel.setChatId(id = id)

        val messageAdapter = MessageAdapter(view.context) { uri: Uri ->
            navigationController.openPhoto(photo = uri)
        }
        val linearLayoutManager = LinearLayoutManager(view.context).apply {
            stackFromEnd = true
        }
        binding.messages.run {
            layoutManager = linearLayoutManager
            adapter = messageAdapter
        }

        viewModel.contact.observe(viewLifecycleOwner) { contact: Contact? ->
            if (contact == null) {
                Toast.makeText(view.context, "Contact not found", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                requireActivity().setLocusContext(LocusId(contact.shortcutId), null)
                navigationController.updateAppBar { name: TextView, icon: ImageView ->
                    name.text = contact.name
                    icon.setImageIcon(Icon.createWithAdaptiveBitmapContentUri(contact.iconUri))
                    startPostponedEnterTransition()
                }
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            messageAdapter.submitList(messages)
            linearLayoutManager.scrollToPosition(messages.size - 1)
        }

        if (prepopulateText != null) {
            binding.input.setText(prepopulateText)
        }

        binding.input.setOnImageAddedListener { contentUri, mimeType, label ->
            viewModel.setPhoto(contentUri, mimeType)
            if (binding.input.text.isNullOrBlank()) {
                binding.input.setText(label)
            }
        }

        viewModel.photo.observe(viewLifecycleOwner) { uri ->
            if (uri == null) {
                binding.photo.visibility = View.GONE
            } else {
                binding.photo.visibility = View.VISIBLE
                Glide.with(binding.photo).load(uri).into(binding.photo)
            }
        }

        binding.voiceCall.setOnClickListener {
            voiceCall()
        }
        binding.send.setOnClickListener {
            send()
        }
        binding.input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                send()
                true
            } else {
                false
            }
        }
    }

    /**
     * Called when the fragment is visible to the user. This is where we update the
     * [ChatViewModel] with the foreground state of the chat. The foreground state is
     * determined by the `ARG_FOREGROUND` argument passed to the fragment.
     */
    override fun onStart() {
        super.onStart()
        val foreground = arguments?.getBoolean(ARG_FOREGROUND) == true
        viewModel.foreground = foreground
    }

    /**
     * Called when the fragment is no longer visible to the user. We update the
     * [ChatViewModel] to indicate that the chat is no longer in the foreground.
     */
    override fun onStop() {
        super.onStop()
        viewModel.foreground = false
    }

    /**
     * Initiates a voice call with the contact. This is triggered when the user clicks the
     * voice call button.
     *
     * It retrieves the current contact from the [viewModel]. If the contact is available,
     * it creates an [Intent] to start the [VoiceCallActivity], passing the contact's name and
     * icon URI as extras.
     */
    private fun voiceCall() {
        val contact = viewModel.contact.value ?: return
        startActivity(
            Intent(requireActivity(), VoiceCallActivity::class.java)
                .putExtra(VoiceCallActivity.EXTRA_NAME, contact.name)
                .putExtra(VoiceCallActivity.EXTRA_ICON_URI, contact.iconUri)
        )
    }

    /**
     * Sends the text input by the user as a message.
     *
     * This function reads the text from the input field. If the text is not empty,
     * it calls the [ChatViewModel.send] method to process and send the message.
     * After sending, it clears the input field. This action can be triggered by
     * tapping the send button or by the IME's send action.
     */
    private fun send() {
        binding.input.text?.let { text ->
            if (text.isNotEmpty()) {
                viewModel.send(text.toString())
                text.clear()
            }
        }
    }
}
