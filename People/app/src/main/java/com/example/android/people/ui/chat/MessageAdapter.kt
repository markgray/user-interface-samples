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

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.android.people.R
import com.example.android.people.data.Message
import com.example.android.people.databinding.MessageItemBinding

/**
 * An adapter to show a list of [Message]s.
 *
 * @param context The context.
 * @property onPhotoClicked The callback when a photo in a message is clicked.
 */
class MessageAdapter(
    context: Context,
    private val onPhotoClicked: (photo: Uri) -> Unit
) : ListAdapter<Message, MessageViewHolder>(DIFF_CALLBACK) {

    /**
     * The colors of the chat bubbles.
     */
    private val tint = object {
        val incoming: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.incoming)
        )
        val outgoing: ColorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(context, R.color.outgoing)
        )
    }

    /**
     * The padding for the chat bubbles.
     */
    private val padding = object {
        /**
         * The vertical padding for a chat bubble.
         */
        val vertical: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_vertical
        )

        /**
         * The horizontal padding for a chat bubble on the side that is shorter.
         */
        val horizontalShort: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_short
        )

        /**
         * The horizontal padding for the side of a chat bubble on the side that is longer.
         */
        val horizontalLong: Int = context.resources.getDimensionPixelSize(
            R.dimen.message_padding_horizontal_long
        )
    }

    /**
     * The size of the photo in a message.
     */
    private val photoSize = context.resources.getDimensionPixelSize(R.dimen.photo_size)

    init {
        setHasStableIds(true)
    }

    /**
     * We need to provide a stable ID for each item, so that the framework can do appropriate
     * animations. We use the ID of the [Message] for this.
     *
     * @param position The position of the item in the list.
     * @return The stable ID of the item.
     */
    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    /**
     * Creates a new ViewHolder and initializes some private fields to be used by RecyclerView.
     * We also set a click listener on the ViewHolder's `message` view. When a message with a
     * photo is clicked, we invoke the `onPhotoClicked` callback.
     *
     * @param parent The parent view.
     * @param viewType The type of the view.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageViewHolder(parent)
        holder.binding.message.setOnClickListener {
            val photo = it.getTag(R.id.tag_photo) as Uri?
            if (photo != null) {
                onPhotoClicked(photo)
            }
        }
        return holder
    }

    /**
     * Displays a [Message] at the specified `position`.
     *
     * It sets the text of the message, and distinguishes between incoming and outgoing messages.
     * An incoming message is aligned to the start, and an outgoing message to the end. It also sets
     * the background tint and padding of the message bubble accordingly.
     *
     * If the message contains a photo, it is loaded via Glide and displayed as a compound
     * drawable at the bottom of the text. We also store the photo's URI in a tag on the view
     * so that it can be retrieved by the click listener in `onCreateViewHolder`.
     *
     * @param holder The [MessageViewHolder] which should be updated to represent the contents of
     * the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message: Message = getItem(position)
        val lp = holder.binding.message.layoutParams as FrameLayout.LayoutParams
        if (message.isIncoming) {
            holder.binding.message.run {
                setBackgroundResource(R.drawable.message_incoming)
                ViewCompat.setBackgroundTintList(this, tint.incoming)
                setPadding(
                    padding.horizontalLong, padding.vertical,
                    padding.horizontalShort, padding.vertical
                )
                layoutParams = lp.apply {
                    gravity = Gravity.START
                }
            }
        } else {
            holder.binding.message.run {
                setBackgroundResource(R.drawable.message_outgoing)
                ViewCompat.setBackgroundTintList(this, tint.outgoing)
                setPadding(
                    padding.horizontalShort, padding.vertical,
                    padding.horizontalLong, padding.vertical
                )
                layoutParams = lp.apply {
                    gravity = Gravity.END
                }
            }
        }
        if (message.photoUri != null) {
            holder.binding.message.setTag(R.id.tag_photo, message.photoUri)
            Glide.with(holder.binding.message)
                .load(message.photoUri)
                .into(
                    /* target = */ CompoundBottomTarget(
                        view = holder.binding.message,
                        width = photoSize,
                        height = photoSize
                    )
                )
        } else {
            holder.binding.message.setTag(R.id.tag_photo, null)
            holder.binding.message.setCompoundDrawables(null, null, null, null)
        }
        holder.binding.message.text = message.text
    }
}

/**
 * A [DiffUtil.ItemCallback] for the [ListAdapter] to calculate the difference between two
 * non-`null` items in a list. This is used to efficiently update the list of [Message]s.
 */
private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Message>() {

    /**
     * Called by the DiffUtil to decide whether two object represent the same item.
     *
     * For example, if your items have unique ids, this method should check their id equality.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return `true` if the two items represent the same object or `false` if they are different.
     */
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    /**
     * Called by the DiffUtil when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item have changed.
     *
     * DiffUtil uses this method to check equality instead of `Object.equals()`.
     *
     * @param oldItem The item in the old list.
     * @param newItem The item in the new list.
     * @return True if the contents of the items are the same or false if they are different.
     */
    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

}

/**
 * The ViewHolder for a [Message].
 *
 * @param parent The parent view.
 */
class MessageViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
) {
    /**
     * The binding for the view of this item.
     */
    val binding: MessageItemBinding = MessageItemBinding.bind(itemView)
}

/**
 * A [CustomTarget] that sets a drawable as the bottom compound drawable of a [TextView].
 * This is used by Glide to load a photo into a [Message].
 *
 * @param view The [TextView] to set the drawable on.
 * @param width The width of the drawable.
 * @param height The height of the drawable.
 */
private class CompoundBottomTarget(
    private val view: TextView,
    width: Int,
    height: Int
) : CustomTarget<Drawable>(width, height) {

    /**
     * Called when the resource is ready. We set the [resource] as the bottom compound
     * drawable of the [view].
     *
     * @param resource The loaded resource.
     * @param transition A [Transition] object that may be used to animate the resource.
     */
    override fun onResourceReady(
        resource: Drawable,
        transition: Transition<in Drawable>?
    ) {
        view.setCompoundDrawablesWithIntrinsicBounds(
            /* left = */ null,
            /* top = */ null,
            /* right = */ null,
            /* bottom = */ resource
        )
    }

    /**
     * A lifecycle callback that gets called when a load is cancelled and its resources are freed.
     * We set the bottom compound drawable of the [view] to the [placeholder] drawable.
     *
     * @param placeholder The placeholder drawable to be used, if any.
     */
    override fun onLoadCleared(placeholder: Drawable?) {
        view.setCompoundDrawablesWithIntrinsicBounds(
            /* left = */ null,
            /* top = */ null,
            /* right = */ null,
            /* bottom = */ placeholder
        )
    }
}
