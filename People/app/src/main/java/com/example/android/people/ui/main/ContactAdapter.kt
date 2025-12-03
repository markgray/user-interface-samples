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

import android.graphics.drawable.Icon
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.people.R
import com.example.android.people.data.Contact
import com.example.android.people.databinding.ChatItemBinding

/**
 * A [ListAdapter] for the chat screen.
 *
 * @param onChatClicked A function to invoke when a contact is clicked. The ID of the contact is
 * passed as an argument.
 */
class ContactAdapter(
    private val onChatClicked: (id: Long) -> Unit
) : ListAdapter<Contact, ContactViewHolder>(DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    /**
     * The stable ID for the item at [position].
     *
     * @param position The position of the item in the list.
     * @return The stable ID of the item at [position].
     */
    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    /**
     * Creates a new [ContactViewHolder] and sets a click listener on its item view.
     *
     * @param parent The parent view.
     * @param viewType The type of the view.
     * @return A new [ContactViewHolder].
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val holder = ContactViewHolder(parent)
        holder.itemView.setOnClickListener {
            onChatClicked(holder.itemId)
        }
        return holder
    }

    /**
     * Binds the contact at the given [position] to the [holder].
     *
     * @param holder The [ContactViewHolder] to bind the data to.
     * @param position The position of the contact in the list.
     */
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact: Contact = getItem(position)
        holder.binding.icon.setImageIcon(Icon.createWithAdaptiveBitmapContentUri(contact.iconUri))
        holder.binding.name.text = contact.name
    }
}

/**
 * A [DiffUtil.ItemCallback] for the [ContactAdapter].
 *
 * This callback is used by [ListAdapter] to calculate the difference between two non-null
 * items in a list. It helps optimize the `RecyclerView`'s performance by only updating the
 * views that have changed.
 *
 * The `areItemsTheSame` method checks if two `Contact` objects represent the same item by
 * comparing their unique `id`s. The `areContentsTheSame` method checks if the data within
 * the `Contact` objects has changed.
 */
private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Contact>() {
    /**
     * Called to check whether two objects represent the same item.
     *
     * This method is used by the DiffUtil to determine if an item has been added, removed,
     * or moved. For the `Contact` class, two items are considered the same if they
     * have the same `id`.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return `true` if the two items have the same ID, `false` otherwise.
     */
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Called to check whether two items have the same data.
     *
     * This method is used by DiffUtil to detect if the contents of an item have changed.
     * This is used to update the item's view if the data has changed, without having to
     * redraw the entire list. For the `Contact` data class, the `equals` method is
     * automatically generated to compare all properties.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return `true` if the contents of the two items are the same, `false` otherwise.
     */
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
        return oldItem == newItem
    }
}

/**
 * The ViewHolder for a [Contact].
 *
 * @param parent The parent ViewGroup.
 */
class ContactViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater
        .from(parent.context)
        .inflate(
            /* resource = */ R.layout.chat_item,
            /* root = */ parent,
            /* attachToRoot = */ false
        )
) {
    /**
     * The binding for the chat item layout.
     *
     * This property provides direct access to the views within the `chat_item.xml` layout
     * file, such as the contact's icon and name. It is initialized by binding the
     * inflated `itemView` of the ViewHolder.
     */
    val binding: ChatItemBinding = ChatItemBinding.bind(itemView)
}
