/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.samples.insetsanimation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [RecyclerView.Adapter] which displays a fake conversation.
 */
internal class ConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * Called when [RecyclerView] needs a new [RecyclerView.ViewHolder] of the given type to
     * represent an item. We initialize our [LayoutInflater] variable `val inflater` to the
     * [LayoutInflater] from context of our [ViewGroup] parameter [parent]. Then we initialize
     * our [View] variable `val view` to the [View] that `inflater` inflates from the layout file
     * `R.layout.message_bubble_self` when our [viewType] parameter is [ITEM_TYPE_MESSAGE_SELF],
     * or else to the [View] it inflates from the layout file `R.layout.message_bubble_other` --
     * in both cases using [parent] for its layout parmeters without attaching to it. Finally we
     * return a [MessageHolder] constructed using [View] to the caller.
     *
     * @param parent The [ViewGroup] into which the new View will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = when (viewType) {
            ITEM_TYPE_MESSAGE_SELF -> {
                inflater.inflate(R.layout.message_bubble_self, parent, false)
            }

            else -> {
                inflater.inflate(R.layout.message_bubble_other, parent, false)
            }
        }
        return MessageHolder(view)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified position. This method should
     * update the contents of the `ViewHolder.itemView` to reflect the item at the given position.
     * We don't actually do any binding so this is a no-op in our case.
     *
     * @param holder The `ViewHolder` which should be updated to represent the contents of the
     * item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // We don't actually do any binding
    }

    /**
     * Return the view type of the item at [position] for the purposes of view recycling. We alternate
     * between [ITEM_TYPE_MESSAGE_OTHER] and [ITEM_TYPE_MESSAGE_SELF] depending on whether [position]
     * is even or odd.
     *
     * @param position position to query
     * @return integer value identifying the type of the view needed to represent the item at
     * [position]. Type codes need not be contiguous.
     */
    override fun getItemViewType(position: Int): Int {
        // We alternate to mimic a real conversation
        return if (position % 2 == 0) ITEM_TYPE_MESSAGE_OTHER else ITEM_TYPE_MESSAGE_SELF
    }

    /**
     * Returns the total number of items in the data set held by the adapter. We just return our
     * constant [NUMBER_MESSAGES] (50).
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int = NUMBER_MESSAGES

    companion object {
        /**
         * The view type that uses the layout file `R.layout.message_bubble_self`
         */
        const val ITEM_TYPE_MESSAGE_SELF = 0

        /**
         * The view type that uses the layout file `R.layout.message_bubble_other`
         */
        const val ITEM_TYPE_MESSAGE_OTHER = 1

        /**
         * The total number of items in this adapter.
         */
        const val NUMBER_MESSAGES = 50
    }
}

/**
 * The custom [RecyclerView.ViewHolder] that our `onCreateViewHolder` override returns.
 */
private class MessageHolder(view: View) : RecyclerView.ViewHolder(view)
