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
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.ArrayList

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
    /**
     * The [MyAdapter] custom [ListAdapter] used to populate our [ListView].
     */
    private lateinit var mAdapter: MyAdapter

    /**
     * Our [ShortcutHelper] which we use to interact with the [ShortcutManager].
     */
    private lateinit var mHelper: ShortcutHelper

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.main] which holds a [Button] which
     * allows the user to add another shortcut (ID [R.id.add]) and a [ListView] which lists the URLs
     * which we use for Launcher Shortcuts. We initialize our [ShortcutHelper] field [mHelper] with
     * a new instance, call its [ShortcutHelper.maybeRestoreAllDynamicShortcuts] method to have it
     * restore any dynamic shortcuts our app may have (a no-op since we do not have any ATM), and
     * then call its [ShortcutHelper.refreshShortcuts] method to have it look for shortcuts that
     * have been pushed and refresh them (the refresh part isn't implemented yet). If the `action`
     * of the [Intent] that started this activity is [ACTION_ADD_WEBSITE] (declared in our Manifest)
     * we call our [addWebSite] method to have it pop up an [AlertDialog] that will allow the user
     * to add another URL to our list of shortcuts. We initialize our [ListView] variable `val listView`
     * by finding the [View] with ID [R.id.list], initialize our [MyAdapter] field [mAdapter] to a
     * new instance that uses our application [Context] to access the [LayoutInflater] system service,
     * and set [mAdapter] to be the adapter for `listView`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
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
    @Suppress("UNUSED_PARAMETER")
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
    @SuppressLint("StaticFieldLeak")
    private fun addUriAsync(uri: String) {
        object : CoroutinesAsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg params: Void?): Void? {
                mHelper.addWebSiteShortcut(uri)
                return null
            }

            override fun onPostExecute(result: Void?) {
                refreshList()
            }
        }.execute()
    }

    private fun refreshList() {
        mAdapter.setShortcuts(mHelper.shortcuts)
    }

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
            @Suppress("UNUSED_VALUE")
            sep = ", "
        }
        return sb.toString()
    }

    @Suppress("CanBeParameter")
    private inner class MyAdapter(private val mContext: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater = mContext.getSystemService(LayoutInflater::class.java)
        private var mList = EMPTY_LIST
        override fun getCount(): Int {
            return mList.size
        }

        override fun getItem(position: Int): Any {
            return mList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun areAllItemsEnabled(): Boolean {
            return true
        }

        override fun isEnabled(position: Int): Boolean {
            return true
        }

        fun setShortcuts(list: List<ShortcutInfo>) {
            mList = list
            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = convertView ?: mInflater.inflate(R.layout.list_item, parent, false)
            bindView(view, position, mList[position])
            return view
        }

        @Suppress("UNUSED_PARAMETER")
        fun bindView(view: View, position: Int, shortcut: ShortcutInfo) {
            view.tag = shortcut
            val line1 = view.findViewById<View>(R.id.line1) as TextView
            val line2 = view.findViewById<View>(R.id.line2) as TextView
            line1.text = shortcut.longLabel
            line2.text = getType(shortcut)
            val remove = view.findViewById<View>(R.id.remove) as Button
            val disable = view.findViewById<View>(R.id.disable) as Button
            disable.setText(
                if (shortcut.isEnabled) R.string.disable_shortcut else R.string.enable_shortcut)
            remove.setOnClickListener(this@Main)
            disable.setOnClickListener(this@Main)
        }

    }

    companion object {
        const val TAG = "ShortcutSample"
        private const val ID_ADD_WEBSITE = "add_website"
        private const val ACTION_ADD_WEBSITE = "com.example.android.shortcutsample.ADD_WEBSITE"
        private val EMPTY_LIST: List<ShortcutInfo> = ArrayList()
    }
}