/*
 * Copyright 2018 The Android Open Source Project
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

package com.example.android.sliceviewer.ui.list

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.slice.Slice
import androidx.slice.widget.SliceView
import com.example.android.sliceviewer.R
import com.example.android.sliceviewer.R.layout
import com.example.android.sliceviewer.util.bind
import com.example.android.sliceviewer.util.convertToSliceViewerScheme

/**
 * [ListAdapter] that provides views that display slice [Uri] values in the [RecyclerView] that is
 * part of the UI of [SliceViewerActivity]
 *
 * @param lifecycleOwner the [LifecycleOwner] we are associated with (`this` in the `onCreate`
 * override of [SliceViewerActivity]
 * @param selectedMode the slice view mode selected by the options menu, one of [SliceView.MODE_LARGE]
 * [SliceView.MODE_SMALL], or [SliceView.MODE_SHORTCUT].
 */
class SliceAdapter(
    val lifecycleOwner: LifecycleOwner,
    val selectedMode: LiveData<Int>
) : ListAdapter<Uri, SliceViewHolder>(
    SlicesDiff
) {
    /**
     * Called when RecyclerView needs a new [SliceViewHolder] of the given type to represent
     * an item. This new ViewHolder should be constructed with a new View that can represent
     * the items of the given type. You can either create a new View manually or inflate it
     * from an XML layout file.
     *
     * The new [SliceViewHolder] will be used to display items of the adapter using [onBindViewHolder].
     * Since it will be re-used to display different items in the data set, it is a good idea to cache
     * references to sub views of the [View] to avoid unnecessary [View.findViewById] calls.
     *
     * We initialize our [View] variable `val itemView` to the [View] that the [LayoutInflater] from
     * the [Context] of our [ViewGroup] parameter [parent] inflates from the layout file with resource
     * ID [layout.slice_row] using [parent] for the layout params without attaching to it. This [View]
     * has a `ConstraintLayout` as its root view which holds a vertical `LinearLayout` with two
     * [TextView]s, and below the `LinearLayout` is a `FrameLayout` holding a [SliceView].
     *
     * We then return a new instance of [SliceViewHolder] constructed from `itemView`, [selectedMode],
     * and [lifecycleOwner].
     *
     * @param parent   The [ViewGroup] into which the new [View] will be added after it is bound to
     * an adapter position.
     * @param viewType The view type of the new View.
     * @return A new [SliceViewHolder] that holds a [View] of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SliceViewHolder {
        val itemView: View = LayoutInflater.from(parent.context)
            .inflate(layout.slice_row, parent, false)
        return SliceViewHolder(itemView, selectedMode, lifecycleOwner)
    }

    /**
     * Called by [RecyclerView] to display the data at the specified position. This method should
     * update the contents of the [ViewHolder.itemView] to reflect the item at the given position.
     * We call the [SliceViewHolder.bind] method of our parameter [holder] with the [Uri] that the
     * [ListAdapter.getItem] method returns for the item in its dataset at position [position].
     *
     * @param holder   The [SliceViewHolder] which should be updated to represent the contents of
     * the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: SliceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

/**
 * This is the ViewHolder which holds the [View] that is used by our [RecyclerView] to display the
 * slice [Uri]s in the dataset of its [SliceAdapter].
 *
 * @param view the [ViewGroup] that will display all the information pertaining to the slice [Uri]
 * @param selectedMode the [LiveData] wrapped [Int] flag for the [SliceView] display mode, one of
 * [SliceView.MODE_SHORTCUT], [SliceView.MODE_SMALL] or [SliceView.MODE_LARGE].
 * @param lifecycleOwner the [LifecycleOwner] which controls the observer of our [LiveData] wrapped
 * fields. It is `this` [SliceViewerActivity] when our constructor is called from its `onCreate`
 * override.
 */
class SliceViewHolder(
    view: View,
    private val selectedMode: LiveData<Int>,
    private val lifecycleOwner: LifecycleOwner
) : ViewHolder(view) {

    /**
     * The [Context] that our [View] field `view` is running in
     */
    private val context: Context = view.context

    /**
     * The [SliceView] with ID [R.id.slice] in `view`, used to display the [Slice]
     */
    private val sliceView: SliceView = view.findViewById(R.id.slice)

    /**
     * The [TextView] with ID [R.id.uri_value] in `view`, used to display the string value of the
     * slice [Uri] we are bound to.
     */
    private val uriValue: TextView = view.findViewById(R.id.uri_value)

    /**
     * The [ViewGroup] with ID [R.id.uri_group] in `view`, it is the vertical `LinearLayout` at the
     * top of `view` which holds a static [TextView] with the text "URI" and the [TextView] holding
     * the string value of the slice [Uri] we are bound to (ID [R.id.uri_value], ie. our [uriValue]
     * field). Its [View.OnClickListener] is set to a lambda which launches an activity using an
     * [Intent] which uses a [Uri] formed by adding a "slice-" prefix to the scheme of the [Uri] we
     * are bound to.
     */
    private val uriGroup: ViewGroup = view.findViewById(R.id.uri_group)

    // Context, LifecycleOwner, onSliceActionListener, OnClickListener, scrollable, OnLongClickListener,
    fun bind(uri: Uri) {
        sliceView.bind(
            context = context,
            lifecycleOwner = lifecycleOwner,
            uri = uri,
            scrollable = false
        )

        uriGroup.setOnClickListener {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri.convertToSliceViewerScheme()))
        }
        selectedMode.observe(lifecycleOwner, {
            sliceView.mode = it ?: SliceView.MODE_LARGE
        })
        uriValue.text = uri.toString()
    }
}

object SlicesDiff : DiffUtil.ItemCallback<Uri>() {
    override fun areItemsTheSame(oldItem: Uri, newItem: Uri) = oldItem === newItem

    override fun areContentsTheSame(oldItem: Uri, newItem: Uri) = oldItem == newItem
}