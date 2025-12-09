/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.example.android.appshortcuts

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * This sample demonstrates how to use the Launcher Shortcuts API introduced in Android 7.1 (API 25).
 * This API allows an application to define a set of Intents which are displayed when a user long
 * presses on the app's launcher icon. Examples are given for registering links both statically in
 * XML, as well as dynamically at runtime.
 *
 * Its UI consists of a [Button] labeled "Add New Website" which when clicked calls our [onAddPressed]
 * method which pops up an [AlertDialog] to allow the user to add a new URL to the [List] of
 * [ShortcutInfo] objects which are displayed in our [ListView] and which are accessed when a user
 * long-presses on our app's launcher icon.
 */
class Main : AppCompatActivity(), View.OnClickListener {
    /** Coroutine variables */

    /**
     * [mainJob] allows us to cancel all coroutines started by [Main].
     */
    private var mainJob = Job()

    /**
     * A [CoroutineScope] that keeps track of all coroutines started by [Main]. Because we
     * pass it [mainJob], any coroutine started in this scope can be cancelled by calling
     * `viewModelJob.cancel()`. By default, all coroutines started in [uiScope] will launch in
     * [Dispatchers.Main] which is the main thread on Android. This is a sensible default because
     * most coroutines started by [Main] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + mainJob)

    /**
     * The [MyAdapter] custom [ListAdapter] used to populate our [ListView].
     */
    private lateinit var mAdapter: MyAdapter

    /**
     * Our [ShortcutHelper] which we use to interact with the [ShortcutManager].
     */
    private lateinit var mHelper: ShortcutHelper

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and set our content
     * view to our layout file `R.layout.main` which holds a [Button] which allows the user to add
     * another shortcut (ID `R.id.add`) and a [ListView] which lists the URLs which we use for
     * Launcher Shortcuts.
     *
     * We initialize our [LinearLayout] variable `rootView` to the view with ID `R.id.root_view`
     * then call [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy for applying
     * window insets to `rootView`, with the `listener` argument a lambda that accepts the [View]
     * passed the lambda in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `systemBars` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument. It then gets the insets for the
     * IME (keyboard) using [WindowInsetsCompat.Type.ime]. It then updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `systemBars.left`, the right margin set to
     * `systemBars.right`, the top margin set to `systemBars.top`, and the bottom margin
     * set to the maximum of the system bars bottom inset and the IME bottom inset.
     * Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * We initialize our [ShortcutHelper] field [mHelper] with a new instance, call its
     * [ShortcutHelper.maybeRestoreAllDynamicShortcuts] method to have it restore any dynamic
     * shortcuts our app may have (a no-op since we do not have any ATM), and then call its
     * [ShortcutHelper.refreshShortcuts] method to have it look for shortcuts that have been pushed
     * and refresh them (the refresh part isn't implemented yet). If the `action` of the [Intent]
     * that started this activity is [ACTION_ADD_WEBSITE] (declared in our Manifest) we call our
     * [addWebSite] method to have it pop up an [AlertDialog] that will allow the user to add
     * another URL to our list of shortcuts. We initialize our [ListView] variable `val listView`
     * by finding the [View] with ID `R.id.list`, initialize our [MyAdapter] field [mAdapter] to a
     * new instance that uses our application [Context] to access the [LayoutInflater] system
     * service, and set [mAdapter] to be the adapter for `listView`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val rootView = findViewById<LinearLayout>(R.id.root_view)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemBars.left
                rightMargin = systemBars.right
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom.coerceAtLeast(ime.bottom)
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        mHelper = ShortcutHelper(this)
        mHelper.maybeRestoreAllDynamicShortcuts()
        mHelper.refreshShortcuts( /*force=*/false)
        if (ACTION_ADD_WEBSITE == intent.action) {
            // Invoked via the manifest shortcut.
            addWebSite()
        }
        val listView: ListView = findViewById(R.id.list)
        mAdapter = MyAdapter(this.applicationContext)
        listView.adapter = mAdapter
    }

    /**
     * Called after [onRestoreInstanceState], [onRestart], or [onPause], for your activity to start
     * interacting with the user. This is an indicator that the activity became active and ready to
     * receive input. It is on top of an activity stack. First we call our super's implmentation of
     * `onResume`, then we call our method [refreshList] to have it refresh the dataset held by our
     * [MyAdapter] field [mAdapter] with all mutable shortcuts for this app in order for them to be
     * displayed in our [ListView].
     */
    override fun onResume() {
        super.onResume()
        refreshList()
    }

    /**
     * Handle the add button. We just call our [addWebSite] method to have it pop up an [AlertDialog]
     * that will allow the user to add another URL to our list of shortcuts.
     *
     * @param v the [View] that was clicked.
     */
    @Suppress("UNUSED_PARAMETER", "RedundantSuppression") // Suggested change would make class less reusable
    fun onAddPressed(v: View?) {
        addWebSite()
    }

    /**
     * Pops up an [AlertDialog] that will allow the user to add another URL to our list of shortcuts.
     * First we call the [ShortcutHelper.reportShortcutUsed] method of our [ShortcutHelper] field
     * [mHelper] to have it call the [ShortcutManager.reportShortcutUsed] method to report the user
     * selected the shortcut containing ID [ID_ADD_WEBSITE] (or has completed an action in the app
     * that is equivalent to selecting the shortcut). This allows the launcher to build a prediction
     * model so that it can promote the shortcuts that are likely to be used at the moment. Next we
     * initialize our [EditText] variable `val editUri` with a new instance, set its `hint` to the
     * [String] "http://www.android.com/", and its `inputType` to [EditorInfo.TYPE_TEXT_VARIATION_URI]
     * (a variation of `TYPE_CLASS_TEXT` for entering a URI). We construct an [AlertDialog.Builder],
     * set its title to the [String] "Add new website", its message to the [String] "Type URL of a
     * website", set its custom view to `editUri`, and the listener of the "Add" positive button to
     * a lambda which initializes its [String] variable `val url` to the `text` of `editUri` and
     * then if `url` is not empty calls our [addUriAsync] method to have it add `url` as a web site
     * shortcut using the [ShortcutHelper.addWebSiteShortcut] method of [ShortcutHelper] field
     * [mHelper]. Finally we show the [AlertDialog.Builder] we constructed to the user.
     */
    private fun addWebSite() {
        Log.i(TAG, "addWebSite")

        // This is important.  This allows the launcher to build a prediction model.
        mHelper.reportShortcutUsed(ID_ADD_WEBSITE)
        val editUri = EditText(this)
        editUri.hint = "http://www.android.com/"
        editUri.inputType = EditorInfo.TYPE_TEXT_VARIATION_URI
        AlertDialog.Builder(this)
            .setTitle("Add new website")
            .setMessage("Type URL of a website")
            .setView(editUri)
            .setPositiveButton("Add") { _: DialogInterface?, _: Int ->
                val url = editUri.text.toString().trim { it <= ' ' }
                if (url.isNotEmpty()) {
                    addUriAsync(url)
                }
            }
            .show()
    }

    /**
     * Starts a background task which calls the [ShortcutHelper.addWebSiteShortcut] method of our
     * [ShortcutHelper] field [mHelper] to have it add our [String] parameter [uri] as a new website
     * shortcut. And when that is done it calls our [refreshList] method to have it refresh the
     * dataset held by our [MyAdapter] field [mAdapter] with all mutable shortcuts for this app in
     * order for them to be displayed in our [ListView].
     *
     * @param uri a web site URL which the user wants to add to our short cut list.
     */
    @SuppressLint("StaticFieldLeak") // TODO: Fix static field leak
    private fun addUriAsync(uri: String) {
        uiScope.noParamNoResultAsync(
            doInBackground = {
                mHelper.addWebSiteShortcut(uri)
            },
            onPostExecute = {
                refreshList()
            }
        )

    }

    /**
     * Refreshes the dataset held by our [MyAdapter] field [mAdapter] with all mutable shortcuts for
     * this app in order for them to be displayed in our [ListView]. It does this by calling the
     * [MyAdapter.setShortcuts] method of our [MyAdapter] field [mAdapter] with the [List] of
     * [ShortcutInfo] that the [ShortcutHelper.shortcuts] property of our [ShortcutHelper] field
     * [mHelper] returns when it queries the [ShortcutManager] for all mutable shortcuts for this
     * app.
     */
    private fun refreshList() {
        mAdapter.setShortcuts(mHelper.shortcuts)
    }

    /**
     * [Main] is set as the [View.OnClickListener] for both the "Remove" (ID `R.id.remove`) and the
     * "Disable" (ID `R.id.disable`) buttons in the layout file layout/list_item.xml which is used
     * to display items in our [ListView] of existing shortcuts so this method is called whenever
     * either of these buttons are clicked. We initialize our [ShortcutInfo] variable `val shortcut`
     * by retrieving the `tag` of the parent [View] of [v]. Then we branch on the `id` of [v]:
     *  - `R.id.disable`: if `shortcut` is enabled we call the [ShortcutHelper.disableShortcut] method
     *  of our [ShortcutHelper] field [mHelper] to disable `shortcut`, otherwise we call the
     *  [ShortcutHelper.enableShortcut] method of [mHelper] to enable `shortcut`. In either case we
     *  then call our [refreshList] method to have it refresh the dataset held by our [MyAdapter]
     *  field [mAdapter] with all mutable shortcuts for this app in order for them to be displayed
     *  in our [ListView].
     *  - `R.id.remove`: we call the [ShortcutHelper.removeShortcut] method of [mHelper] to remove
     *  `shortcut`, then call our [refreshList] method to have it refresh the dataset held by our
     *  [MyAdapter] field [mAdapter] with all mutable shortcuts for this app in order for them to
     *  be displayed in our [ListView].
     *
     * @param v the [View] that was clicked.
     */
    override fun onClick(v: View) {
        val shortcut = (v.parent as View).tag as ShortcutInfo
        when (v.id) {
            R.id.disable -> {
                if (shortcut.isEnabled) {
                    mHelper.disableShortcut(shortcut)
                } else {
                    mHelper.enableShortcut(shortcut)
                }
                refreshList()
            }

            R.id.remove -> {
                mHelper.removeShortcut(shortcut)
                refreshList()
            }
        }
    }

    /**
     * Constructs and returns a [String] describing what type of shortcut our [ShortcutInfo] parameter
     * [shortcut] is. First we initialize our [StringBuilder] variable `val sb`, then initialize our
     * [String] variable `var sep` to the emptry [String]. If the [ShortcutInfo.isDynamic] property
     * of our [ShortcutInfo] parameter [shortcut] is `true` we append `sep` to `sb` followed by the
     * [String] "Dynamic" and set `sep` to ", ". If the [ShortcutInfo.isPinned] property of our
     * [ShortcutInfo] parameter [shortcut] is `true` we append `sep` to `sb` followed by the
     * [String] "Pinned" and set `sep` to ", ". If the [ShortcutInfo.isEnabled] property of our
     * [ShortcutInfo] parameter [shortcut] is `false` we append `sep` to `sb` followed by the
     * [String] "Disabled" and set `sep` to ", ". Finally we return the [String] value of `sb` to the
     * caller.
     *
     * @param shortcut the [ShortcutInfo] object for the shortcut we are to describe.
     * @return a [String] formed by concatenating "Dynamic" if the [ShortcutInfo.isDynamic] property
     * of [shortcut] is `true`, followed by "Pinned" if the [ShortcutInfo.isPinned] property of
     * [shortcut] is `true`, followed by "Disabled" if the [ShortcutInfo.isEnabled] property of
     * [shortcut] is `false`.
     */
    private fun getType(shortcut: ShortcutInfo): String {
        val sb = StringBuilder()
        var sep = ""
        if (shortcut.isDynamic) {
            sb.append(sep)
            sb.append("Dynamic")
            sep = ", "
        }
        if (shortcut.isPinned) {
            sb.append(sep)
            sb.append("Pinned")
            sep = ", "
        }
        if (!shortcut.isEnabled) {
            sb.append(sep)
            sb.append("Disabled")
            // sep = ", " // value not needed
        }
        return sb.toString()
    }

    /**
     * The custom [BaseAdapter] that we use to display [ShortcutInfo] objects in our [ListView].
     *
     * @param mContext the application [Context] of [Main] which we use to get a [LayoutInflater]
     * that we can use to inflate item views.
     */
    private inner class MyAdapter(private val mContext: Context) : BaseAdapter() {
        /**
         * The system level [LayoutInflater] we use to inflate item views.
         */
        private val mInflater: LayoutInflater = mContext.getSystemService(LayoutInflater::class.java)

        /**
         * The dataset of [ShortcutInfo] objects we hold for display in our [ListView].
         */
        private var mList = EMPTY_LIST

        /**
         * How many items are in the data set represented by this Adapter. We just return the `size`
         * of our [List] of [ShortcutInfo] field [mList].
         *
         * @return Count of items.
         */
        override fun getCount(): Int {
            return mList.size
        }

        /**
         * Get the data item associated with the specified position in the data set. We return the
         * [ShortcutInfo] at position [position] in our [List] of [ShortcutInfo] field [mList].
         *
         * @param position Position of the item whose data we want within the adapter's
         * data set.
         * @return The data at the specified position.
         */
        override fun getItem(position: Int): Any {
            return mList[position]
        }

        /**
         * Get the row id associated with the specified position in the list. We use the position of
         * the item in our dataset as the row id so we just return our [position] parameter converted
         * to a [Long].
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        /**
         * Indicates whether the item ids are stable across changes to the underlying data. Since our
         * item id is the position of the item in our dataset, and that position can change when items
         * are deleted we have to return `false`.
         *
         * @return `true` if the same id always refers to the same object. We return `false`.
         */
        override fun hasStableIds(): Boolean {
            return false
        }

        /**
         * Indicates whether all the items in this adapter are enabled. If the value returned by
         * this method changes over time, there is no guarantee it will take effect. If `true`, it
         * means all items are selectable and clickable (there is no separator.) We always return
         * `true`.
         *
         * @return `true` if all items are enabled, `false` otherwise.
         */
        override fun areAllItemsEnabled(): Boolean {
            return true
        }

        /**
         * Returns `true` if the item at the specified [position] is not a separator. (A separator is
         * a non-selectable, non-clickable item). The result is unspecified if [position] is invalid.
         * We ignore [position] and always return `true`.
         *
         * @param position Index of the item
         * @return `true` if the item is not a separator
         */
        override fun isEnabled(position: Int): Boolean {
            return true
        }

        /**
         * Called to update the dataset we hold in our [List] of [ShortcutInfo] field [mList]. We
         * set [mList] to our parameter [list], then call the [notifyDataSetChanged] method to notify
         * all of our observers that the underlying data has been changed and any [View] reflecting
         * the data set should refresh itself.
         *
         * @param list the new [List] of [ShortcutInfo] we should use as our dataset [mList].
         */
        fun setShortcuts(list: List<ShortcutInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        /**
         * Get a [View] that displays the data at the specified [position] in the data set. You can
         * either create a [View] manually or inflate it from an XML layout file. When the [View] is
         * inflated, the parent [View] (`GridView`, [ListView]...) will apply default layout
         * parameters unless you use specify a root view and prevent attachment to that root. If our
         * [View] parameter [convertView] is not `null` we initialize our variable `val view` to it,
         * if it is `null` we initialize `view` to the [View] that our [LayoutInflater] field
         * [mInflater] returns when it inflates our item view layout file `R.layout.list_item` using
         * our [ViewGroup] parameter [parent] for its layout params without attaching to it. Then we
         * call our method [bindView] to have it configure `view` to hold and display the [ShortcutInfo]
         * object in position [position] of our dataset [List] of [ShortcutInfo] field [mList]. Finally
         * we return `view` to the caller.
         *
         * @param position The position of the item within the adapter's data set of the item whose
         * view we want.
         * @param convertView The old [View] to reuse, if possible. Note: You should check that this
         * view is non-`null` and of an appropriate type before using. If it is not possible to
         * convert this [View] to display the correct data, this method can create a new [View].
         * Heterogeneous lists can specify their number of view types, so that this [View] is always
         * of the right type (see [getViewTypeCount] and [getItemViewType]).
         * @param parent The parent that this view will eventually be attached to
         * @return A [View] corresponding to the data at the specified position.
         */
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: mInflater.inflate(R.layout.list_item, parent, false)
            bindView(view, position, mList[position])
            return view
        }

        /**
         * Called to configure our [View] parameter [view] to hold and display the [ShortcutInfo]
         * object at position [position] of our dataset [List] of [ShortcutInfo] field [mList].
         * First we set the `tag` property of our [View] parameter [view] to our [ShortcutInfo]
         * parameter [shortcut]. We initialize our [TextView] variable `val line1` by finding the
         * [View] in [view] with ID `R.id.line1` and our [TextView] variable `val line2` by finding
         * the [View] in [view] with ID `R.id.line2`. We set the text of `line1` to the `longLabel`
         * property of [shortcut], and the text of `line2` to the [String] that our [getType] method
         * returns describing the "type" of shortcut that [shortcut] is. We initialize our [Button]
         * variable `val remove` by finding the [View] in [view] with ID `R.id.remove` and our
         * [Button] variable `val disable` by finding the [View] in [view] with ID `R.id.disable`.
         * If the `isEnabled` property of [shortcut] is `true` we set the text of `disable` to the
         * [String] with resource ID `R.string.disable_shortcut` ("Disable"), otherwise we set the
         * text of `disable` to the [String] with resource ID `R.string.enable_shortcut` ("Enable").
         * Finally we set the `OnClickListener` of both `remove` and `disable` to [Main].
         *
         * @param view the [View] we are to configure to hold and display our [ShortcutInfo]
         * parameter [shortcut].
         * @param position the position of the [ShortcutInfo] object in our dataset's [List].
         * @param shortcut the [ShortcutInfo] object that our [View] parameter [view] is to hold and
         * to display.
         */
        @Suppress("UNUSED_PARAMETER", "RedundantSuppression") // Suggested change would make class less reusable
        fun bindView(view: View, position: Int, shortcut: ShortcutInfo) {
            view.tag = shortcut
            val line1 = view.findViewById<View>(R.id.line1) as TextView
            val line2 = view.findViewById<View>(R.id.line2) as TextView
            line1.text = shortcut.longLabel
            line2.text = getType(shortcut)
            val remove = view.findViewById<View>(R.id.remove) as Button
            val disable = view.findViewById<View>(R.id.disable) as Button
            disable.setText(
                if (shortcut.isEnabled) R.string.disable_shortcut else R.string.enable_shortcut
            )
            remove.setOnClickListener(this@Main)
            disable.setOnClickListener(this@Main)
        }

    }

    companion object {
        /**
         * TAG used for logging.
         */
        const val TAG: String = "appshortcuts"

        /**
         * The short cut ID that is reported to the [ShortcutManager] when the user selects the
         * "Add Website" shortcut or completes an action in the app that is equivalent to having
         * selected the shortcut.
         */
        private const val ID_ADD_WEBSITE = "add_website"

        /**
         * The action of the [Intent] that is launched when the user selects the "Add Website"
         * shortcut.
         */
        private const val ACTION_ADD_WEBSITE = "com.example.android.appshortcuts.ADD_WEBSITE"

        /**
         * The empty [List] of [ShortcutInfo] objects used to initialize the [MyAdapter.mList]
         * dataset before the user adds some shortcuts to it.
         */
        private val EMPTY_LIST: List<ShortcutInfo> = ArrayList()
    }
}
