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
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.slice.Slice
import androidx.slice.widget.SliceView
import com.example.android.sliceviewer.R
import com.example.android.sliceviewer.domain.LocalUriDataSource
import com.example.android.sliceviewer.domain.UriDataSource
import com.example.android.sliceviewer.ui.ViewModelFactory

/**
 * Example use of SliceView. Uses a search bar to select/auto-complete a slice uri which is
 * then displayed in the selected mode with SliceView.
 */
class SliceViewerActivity : AppCompatActivity() {

    /**
     * The [SearchView] in our layout file with ID [R.id.search_view] which allows the user to enter
     * a [Slice] URI that he wants to view.
     */
    private lateinit var searchView: SearchView

    /**
     * The [SliceAdapter] which feeds views created from its dataset of [Slice] URIs that are then
     * displayed in our [RecyclerView].
     */
    private lateinit var sliceAdapter: SliceAdapter

    /**
     * The [SliceViewModel] which provides methods to read, write, and remove the [Slice] URIs that
     * are stored by [UriDataSource] in our shared preferences file.
     */
    private lateinit var viewModel: SliceViewModel

    /**
     * The [SubMenu] of our options menu with the title "Mode" that allows the user to select from
     * the 3 different modes for presenting a slice:
     *  - [SliceView.MODE_SHORTCUT] slice should be presented as a tappable icon.
     *  - [SliceView.MODE_SMALL] slice should be presented in small format, only top-level
     *  information and actions from the slice are shown.
     *  - [SliceView.MODE_LARGE] slice should be presented in large format, as much or all of the
     *  slice contents are shown
     */
    private lateinit var typeMenu: SubMenu

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_slice_viewer]. It consists
     * of a [LinearLayout] root view, which holds a [FrameLayout] which contains a [CardView] holding
     * a [Toolbar] holding a [SearchView]. Below this in the [LinearLayout] is a [RecyclerView] which
     * holds view holders for each of the slices that have been searched for in the [SearchView].
     * Each of these slices can be swiped to remove them from the [RecyclerView], and they are
     * preserved in the SharedPreferences of the app by the CRUD local data source [LocalUriDataSource].
     *
     * Next we initialize our [ViewModelFactory] variable `val viewModelFactory` to the singleton
     * instance of our [ViewModelFactory] (creating it if need be). We use the [setSupportActionBar]
     * method to set the [Toolbar] in our UI with ID [R.id.search_toolbar] as the `ActionBar` for
     * our Activity window. We initialize our [SliceViewModel] field [viewModel] by creating the
     * [ViewModelProvider], which will create ViewModels via the Factory `viewModelFactory` and
     * retain them in a store of `this` ViewModelStoreOwner and calling its [ViewModelProvider.get]
     * method to retrieve an existing [SliceViewModel] or create a new one in the scope it is
     * associated with.
     *
     * Next we initialize our [SearchView] field [searchView] by finding the view with the ID
     * [R.id.search_view] immediately using the [apply] extension function on it to:
     *  - Set its [SearchView.OnQueryTextListener] to an anonymous instance whose `onQueryTextChange`
     *  override always returns `false` when the query text is changed by the user to have the default
     *  action performed, and overrides its `onQueryTextSubmit` to add the [Slice] that the [Uri]
     *  entered references to our [viewModel], submit the updated list of slices to [sliceAdapter]
     *  for it to diff and display, clears the [searchView] for the next query, and returns `false`
     *  to let the [SearchView] perform the default action.
     *  - Set its [View.OnClickListener] to a lambda which sets the `isIconified` property of
     *  [searchView] to `false` to expand the [SearchView].
     *  - Set its [View.OnFocusChangeListener] to a lambda which will, if the [SearchView] has lost
     *  focus, request to hide the soft input window.
     *  - Set the hint text to display in the query text field to the [String] "Enter Slice URIs."
     *
     * Next we initialize our [SliceAdapter] field [sliceAdapter] with a new instance which uses
     * `this` as its [LifecycleOwner], and the [SliceViewModel.selectedMode] field of [viewModel]
     * as the "mode" to display each of the slices at (one of:
     *  - [SliceView.MODE_SHORTCUT] slice should be presented as a tappable icon.
     *  - [SliceView.MODE_SMALL] slice should be presented in small format, only top-level
     *  information and actions from the slice are shown.
     *  - [SliceView.MODE_LARGE] slice should be presented in large format, as much or all of the
     *  slice contents are shown.
     *
     * Next we find the [RecyclerView] in our UI with ID [R.id.slice_list] immediately using the
     * [apply] extension function on it to:
     *  - Set its adapter to [sliceAdapter]
     *  - Construct an [ItemTouchHelper] which uses an instance of our [SwipeCallback] custom
     *  [ItemTouchHelper.SimpleCallback] as its call back, and attach the [ItemTouchHelper] to
     *  the [RecyclerView] (`this` in the lambda).
     *
     * Finally we call the [ListAdapter.submitList] method of [sliceAdapter] to have it diff the
     * list of [Slice]s in the [SliceViewModel.slices] field of [viewModel] and display them in the
     * [RecyclerView].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_slice_viewer)
        val viewModelFactory: ViewModelFactory? = ViewModelFactory.getInstance(application)
        setSupportActionBar(findViewById(R.id.search_toolbar))
        viewModel = ViewModelProvider(this, viewModelFactory!!)
            .get(SliceViewModel::class.java)

        searchView = findViewById<SearchView>(R.id.search_view).apply {
            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?) = false
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.addSlice(Uri.parse(query))
                    sliceAdapter.submitList(viewModel.slices)
                    searchView.setQuery("", false)
                    searchView.clearFocus()
                    return false
                }
            })
            setOnClickListener {
                searchView.isIconified = false
            }
            setOnFocusChangeListener { v: View, hasFocus: Boolean ->
                if (!hasFocus) {
                    (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                        .hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
            queryHint = getString(R.string.uri_input_hint)
        }

        sliceAdapter = SliceAdapter(
            lifecycleOwner = this,
            selectedMode = viewModel.selectedMode
        )

        findViewById<RecyclerView>(R.id.slice_list).apply {
            adapter = sliceAdapter
            ItemTouchHelper(SwipeCallback()).attachToRecyclerView(this)
        }
        sliceAdapter.submitList(viewModel.slices)
    }

    /**
     * This custom [ItemTouchHelper.SimpleCallback] is used as the callback of the [ItemTouchHelper]
     * that is attached to our [RecyclerView]. It allows the user to remove a [Slice] from the
     * [RecyclerView] by swiping it to one side or the other.
     */
    inner class SwipeCallback :
        ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
        /**
         * Called when [ItemTouchHelper] wants to move the dragged item from its old position to the
         * new position. If this method returns `true`, ItemTouchHelper assumes [viewHolder] has been
         * moved to the adapter position of [target] ViewHolder. We always return `false` since we do
         * not support dragging our [RecyclerView.ViewHolder] items to new locations.
         *
         * @param recyclerView The [RecyclerView] to which [ItemTouchHelper] is attached to.
         * @param viewHolder   The [RecyclerView.ViewHolder] which is being dragged by the user.
         * @param target       The [RecyclerView.ViewHolder] over which the currently active item
         * is being dragged.
         * @return `true` if the [viewHolder] has been moved to the adapter position of [target].
         */
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ) = false

        /**
         * Called when a [RecyclerView.ViewHolder] is swiped by the user. We call the method
         * [SliceViewModel.removeFromPosition] method of [viewModel] to have its [UriDataSource]
         * remove the item at the Adapter position of [viewHolder] from its dataset, then call the
         * [SliceAdapter.submitList] method of [sliceAdapter] to have it diff the update list of
         * [Slice]s in the [SliceViewModel.slices] field of [viewModel] and display them in the
         * [RecyclerView].
         *
         * @param viewHolder The [RecyclerView.ViewHolder] which has been swiped by the user.
         * @param direction  The direction to which the ViewHolder is swiped. We ignore.
         */
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            viewModel.removeFromPosition(viewHolder.bindingAdapterPosition)
            sliceAdapter.submitList(viewModel.slices)
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in to [menu]. This is only called once, the first time the options menu is displayed.
     * To update the menu every time it is displayed, see [onPrepareOptionsMenu].
     *
     * @param menu The options [Menu] in which you place your items.
     * @return You must return `true` for the menu to be displayed, if you return `false` it will
     * not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        typeMenu = menu.addSubMenu(R.string.slice_mode_title).apply {
            setIcon(R.drawable.ic_large)
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add(R.string.shortcut_mode)
            add(R.string.small_mode)
            add(R.string.large_mode)
        }

        viewModel.selectedMode.observe(this, {
            when (it) {
                SliceView.MODE_SHORTCUT -> typeMenu.setIcon(R.drawable.ic_shortcut)
                SliceView.MODE_SMALL -> typeMenu.setIcon(R.drawable.ic_small)
                SliceView.MODE_LARGE -> typeMenu.setIcon(R.drawable.ic_large)
            }
        })
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title.toString()) {
            getString(R.string.shortcut_mode) ->
                viewModel.selectedMode.value = SliceView.MODE_SHORTCUT
            getString(R.string.small_mode) ->
                viewModel.selectedMode.value = SliceView.MODE_SMALL
            getString(R.string.large_mode) ->
                viewModel.selectedMode.value = SliceView.MODE_LARGE
        }
        return true
    }

    companion object {
        const val TAG = "SliceViewer"
    }
}