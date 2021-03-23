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

import android.app.ListActivity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ShortcutInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import java.util.ArrayList

class Main : ListActivity(), View.OnClickListener {
    private var mAdapter: MyAdapter? = null
    private var mHelper: ShortcutHelper? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        mHelper = ShortcutHelper(this)
        mHelper!!.maybeRestoreAllDynamicShortcuts()
        mHelper!!.refreshShortcuts( /*force=*/false)
        if (ACTION_ADD_WEBSITE == intent.action) {
            // Invoked via the manifest shortcut.
            addWebSite()
        }
        mAdapter = MyAdapter(this.applicationContext)
        listAdapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    /**
     * Handle the add button.
     */
    @Suppress("UNUSED_PARAMETER")
    fun onAddPressed(v: View?) {
        addWebSite()
    }

    private fun addWebSite() {
        Log.i(TAG, "addWebSite")

        // This is important.  This allows the launcher to build a prediction model.
        mHelper!!.reportShortcutUsed(ID_ADD_WEBSITE)
        val editUri = EditText(this)
        editUri.hint = "http://www.android.com/"
        editUri.inputType = EditorInfo.TYPE_TEXT_VARIATION_URI
        AlertDialog.Builder(this)
            .setTitle("Add new website")
            .setMessage("Type URL of a website")
            .setView(editUri)
            .setPositiveButton("Add") { dialog: DialogInterface?, whichButton: Int ->
                val url = editUri.text.toString().trim { it <= ' ' }
                if (url.length > 0) {
                    addUriAsync(url)
                }
            }
            .show()
    }

    private fun addUriAsync(uri: String) {
        object : AsyncTask<Void?, Void?, Void?>() {
            protected override fun doInBackground(vararg params: Void?): Void? {
                mHelper!!.addWebSiteShortcut(uri)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                refreshList()
            }
        }.execute()
    }

    private fun refreshList() {
        mAdapter!!.setShortcuts(mHelper!!.shortcuts)
    }

    override fun onClick(v: View) {
        val shortcut = (v.parent as View).tag as ShortcutInfo
        when (v.id) {
            R.id.disable -> {
                if (shortcut.isEnabled) {
                    mHelper!!.disableShortcut(shortcut)
                } else {
                    mHelper!!.enableShortcut(shortcut)
                }
                refreshList()
            }
            R.id.remove -> {
                mHelper!!.removeShortcut(shortcut)
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
            sep = ", "
        }
        return sb.toString()
    }

    private inner class MyAdapter(private val mContext: Context) : BaseAdapter() {
        private val mInflater: LayoutInflater
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
            val view: View = convertView ?: mInflater.inflate(R.layout.list_item, null)
            bindView(view, position, mList[position])
            return view
        }

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

        init {
            mInflater = mContext.getSystemService(LayoutInflater::class.java)
        }
    }

    companion object {
        const val TAG = "ShortcutSample"
        private const val ID_ADD_WEBSITE = "add_website"
        private const val ACTION_ADD_WEBSITE = "com.example.android.shortcutsample.ADD_WEBSITE"
        private val EMPTY_LIST: List<ShortcutInfo> = ArrayList()
    }
}