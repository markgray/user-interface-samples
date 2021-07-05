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

@file:Suppress("RemoveRedundantQualifierName")

package com.example.android.sliceviewer.ui.single

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.slice.Slice
import androidx.slice.widget.SliceView
import com.example.android.sliceviewer.R
import com.example.android.sliceviewer.domain.UriDataSource
import com.example.android.sliceviewer.ui.ViewModelFactory
import com.example.android.sliceviewer.ui.list.SliceViewerActivity
import com.example.android.sliceviewer.util.bind
import com.example.android.sliceviewer.util.convertToOriginalScheme
import com.example.android.sliceviewer.util.hasSupportedSliceScheme

/**
 * Example use of [SliceView]. This activity is launched when a [Uri] displayed in the `RecyclerView`
 * of [SliceViewerActivity] is clicked (that is when the `ViewGroup` holding the [TextView] that is
 * displaying the string value of the slice [Uri] is clicked, not when the [SliceView] beneath that
 * `ViewGroup` is clicked. When the [SliceView] is clicked the /hello `Slice` does nothing, but the
 * /test `Slice` launches `MainActivity`, and that same behavior of the [SliceView] occurs here as
 * well).
 */
class SingleSliceViewerActivity : AppCompatActivity() {

    /**
     * The [SingleSliceViewModel] view model we use to store data that needs to survive activity
     * restart, in our case the [UriDataSource] used to reference our persistent store of slice
     * [Uri]s, and the [MutableLiveData] wrapped [Int] property [SingleSliceViewModel.selectedMode]
     * that stores the display mode of our [SliceView] that is selected by the options menu, one of
     * [SliceView.MODE_LARGE], [SliceView.MODE_SMALL], or [SliceView.MODE_SHORTCUT].
     */
    private lateinit var viewModel: SingleSliceViewModel

    /**
     * The [SliceView] in our UI with ID [R.id.slice], displays our [Slice]
     */
    private lateinit var sliceView: SliceView

    /**
     * The [TextView] in our UI with ID [R.id.uri_value], displays the string value of the slice
     * [Uri] we were passed in the [Intent] that launched us.
     */
    private lateinit var uriValue: TextView

    /**
     * The [SubMenu] in our options menu which allows the user to select the display mode of our
     * [SliceView]. It is added to the options [Menu] programmatically in our [onCreateOptionsMenu]
     * override, and has [MenuItem]s that allow the user to choose one of [SliceView.MODE_LARGE],
     * [SliceView.MODE_SMALL], or [SliceView.MODE_SHORTCUT]. Its current selection is used to update
     * the [SingleSliceViewModel.selectedMode] property of our [viewModel] field.
     */
    private lateinit var typeMenu: SubMenu

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_single_slice_viewer] which
     * consists of a `ConstraintLayout` root view holding a [TextView] for the label "URI", above a
     * [TextView] holding the string value of the slice [Uri] we were launched to display, with a
     * `ScrollView` holding the [SliceView] we display the [Slice] in below that.
     *
     * We initialize our [ViewModelFactory] variable `val viewModelFactory` to the application's
     * singleton instance, then initialize our [SingleSliceViewModel] field [viewModel] by using
     * the [ViewModelProvider.get] method of a [ViewModelProvider] which will create ViewModels
     * via the Factory `viewModelFactory` and retain them in a store of `this` [ViewModelStoreOwner]
     * to return an existing [SingleSliceViewModel] or create a new one.
     *
     * We initialize our [SliceView] field [sliceView] by finding the view with ID [R.id.slice], and
     * our [TextView] field [uriValue] by finding the view with ID [R.id.uri_value]. If the [Intent]
     * that started this activity has a non-`null` data [Uri], and our [Uri.hasSupportedSliceScheme]
     * extension function determines that that [Uri] has a supported slice scheme we initialize our
     * [Uri] variable `val sliceUri` to the [Uri] that our [Uri.convertToOriginalScheme] creates from
     * our data [Uri], use the [SingleSliceViewModel.addSlice] method of [viewModel] to save it to
     * the persistent [Set] of Slices, and call our [bindSlice] method with `sliceUri` to have it do
     * what needs to be done to have our [SliceView] field [sliceView] display the slice referenced
     * by the [Uri].
     *
     * If our [Intent] does not contain a data [Uri] for a [Slice] we log the message "No Slice URI
     * found, sending to SliceViewerActivity", [Toast] this message as well, and re-launch the
     * [SliceViewerActivity] activity with the [Intent] flag [Intent.FLAG_ACTIVITY_CLEAR_TOP] (when
     * this flag is set and the activity being launched is already running in the current task, then
     * instead of launching a new instance of that activity, all of the other activities on top of
     * it will be closed and this [Intent] will be delivered to the (now on top) old activity as a
     * new [Intent]).
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_slice_viewer)
        val viewModelFactory: ViewModelFactory? = ViewModelFactory.getInstance(application)
        viewModel = ViewModelProvider(this, viewModelFactory!!)
            .get(SingleSliceViewModel::class.java)

        sliceView = findViewById(R.id.slice)
        uriValue = findViewById(R.id.uri_value)

        // If a URI was passed in has a supported slice scheme, present the Slice and save it to the
        // persistent list of Slices
        if (intent.data != null && (intent.data as Uri).hasSupportedSliceScheme()) {
            val sliceUri: Uri = (intent.data as Uri).convertToOriginalScheme()
            viewModel.addSlice(sliceUri)
            bindSlice(sliceUri)
        } else {
            // No Slice found, fall back to main page.
            val msg = "No Slice URI found, sending to SliceViewerActivity"
            Log.w(TAG, msg)
            Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
            startActivity(Intent(this, SliceViewerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        typeMenu = menu.addSubMenu(R.string.slice_mode_title).apply {
            setIcon(R.drawable.ic_large)
            item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            add(R.string.shortcut_mode)
            add(R.string.small_mode)
            add(R.string.large_mode)
        }

        @Suppress("RedundantSamConstructor")
        viewModel.selectedMode.observe(this, Observer {
            when (it) {
                SliceView.MODE_SHORTCUT -> typeMenu.setIcon(R.drawable.ic_shortcut)
                SliceView.MODE_SMALL -> typeMenu.setIcon(R.drawable.ic_small)
                SliceView.MODE_LARGE -> typeMenu.setIcon(R.drawable.ic_large)
            }
        })
        super.onCreateOptionsMenu(menu)
        return true
    }

    private fun bindSlice(uri: Uri) {
        sliceView.bind(
            context = this,
            lifecycleOwner = this,
            uri = uri,
            scrollable = true
        )
        @Suppress("RedundantSamConstructor")
        viewModel.selectedMode.observe(this, Observer {
            sliceView.mode = it ?: SliceView.MODE_LARGE
        })
        uriValue.text = uri.toString()
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
        const val TAG = "SingleSliceViewer"
    }
}