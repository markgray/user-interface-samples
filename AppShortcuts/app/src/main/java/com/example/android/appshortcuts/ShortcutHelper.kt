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
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.PersistableBundle
import android.util.Log
import com.example.android.appshortcuts.Utils.showToast
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.ArrayList
import java.util.HashSet
import java.util.function.BooleanSupplier

class ShortcutHelper(private val mContext: Context) {
    private val mShortcutManager: ShortcutManager = mContext.getSystemService(ShortcutManager::class.java)
    fun maybeRestoreAllDynamicShortcuts() {
        @Suppress("ControlFlowWithEmptyBody")
        if (mShortcutManager.dynamicShortcuts.size == 0) {
            // NOTE: If this application is always supposed to have dynamic shortcuts, then publish
            // them here.
            // Note when an application is "restored" on a new device, all dynamic shortcuts
            // will *not* be restored but the pinned shortcuts *will*.
        }
    }

    fun reportShortcutUsed(id: String?) {
        mShortcutManager.reportShortcutUsed(id)
    }

    /**
     * Use this when interacting with ShortcutManager to show consistent error messages.
     */
    private fun callShortcutManager(r: BooleanSupplier) {
        try {
            if (!r.asBoolean) {
                showToast(mContext, "Call to ShortcutManager is rate-limited")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Caught Exception", e)
            showToast(mContext, "Error while calling ShortcutManager: $e")
        }
    }// Load mutable dynamic shortcuts and pinned shortcuts and put them into a single list
    // removing duplicates.

    // Check existing shortcuts shortcuts
    /**
     * Return all mutable shortcuts from this app self.
     */
    val shortcuts: List<ShortcutInfo>
        get() {
            // Load mutable dynamic shortcuts and pinned shortcuts and put them into a single list
            // removing duplicates.
            val ret: MutableList<ShortcutInfo> = ArrayList()
            val seenKeys = HashSet<String>()

            // Check existing shortcuts shortcuts
            for (shortcut in mShortcutManager.dynamicShortcuts) {
                if (!shortcut.isImmutable) {
                    ret.add(shortcut)
                    seenKeys.add(shortcut.id)
                }
            }
            for (shortcut in mShortcutManager.pinnedShortcuts) {
                if (!shortcut.isImmutable && !seenKeys.contains(shortcut.id)) {
                    ret.add(shortcut)
                    seenKeys.add(shortcut.id)
                }
            }
            return ret
        }

    /**
     * Called when the activity starts.  Looks for shortcuts that have been pushed and refreshes
     * them (but the refresh part isn't implemented yet...).
     */
    @SuppressLint("StaticFieldLeak")
    fun refreshShortcuts(force: Boolean) {
        object : CoroutinesAsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg params: Void?): Void? {
                Log.i(TAG, "refreshingShortcuts...")
                val now = System.currentTimeMillis()
                val staleThreshold = if (force) now else now - REFRESH_INTERVAL_MS

                // Check all existing dynamic and pinned shortcut, and if their last refresh
                // time is older than a certain threshold, update them.
                val updateList: MutableList<ShortcutInfo> = ArrayList()
                for (shortcut in shortcuts) {
                    if (shortcut.isImmutable) {
                        continue
                    }
                    val extras = shortcut.extras
                    if (extras != null && extras.getLong(EXTRA_LAST_REFRESH) >= staleThreshold) {
                        // Shortcut still fresh.
                        continue
                    }
                    Log.i(TAG, "Refreshing shortcut: " + shortcut.id)
                    val b = ShortcutInfo.Builder(
                        mContext, shortcut.id)
                    setSiteInformation(b, shortcut.intent!!.data)
                    setExtras(b)
                    updateList.add(b.build())
                }
                // Call update.
                if (updateList.size > 0) {
                    callShortcutManager { mShortcutManager.updateShortcuts(updateList) }
                }
                return null
            }
        }.execute()
    }

    private fun createShortcutForUrl(urlAsString: String): ShortcutInfo {
        Log.i(TAG, "createShortcutForUrl: $urlAsString")
        val b = ShortcutInfo.Builder(mContext, urlAsString)
        val uri = Uri.parse(urlAsString)
        b.setIntent(Intent(Intent.ACTION_VIEW, uri))
        setSiteInformation(b, uri)
        setExtras(b)
        return b.build()
    }

    private fun setSiteInformation(b: ShortcutInfo.Builder, uri: Uri?): ShortcutInfo.Builder {
        // TODO Get the actual site <title> and use it.
        // TODO Set the current locale to accept-language to get localized title.
        b.setShortLabel(uri!!.host!!)
        b.setLongLabel(uri.toString())
        val bmp = fetchFavicon(uri)
        if (bmp != null) {
            b.setIcon(Icon.createWithBitmap(bmp))
        } else {
            b.setIcon(Icon.createWithResource(mContext, R.drawable.link))
        }
        return b
    }

    private fun setExtras(b: ShortcutInfo.Builder): ShortcutInfo.Builder {
        val extras = PersistableBundle()
        extras.putLong(EXTRA_LAST_REFRESH, System.currentTimeMillis())
        b.setExtras(extras)
        return b
    }

    private fun normalizeUrl(urlAsString: String): String {
        return if (urlAsString.startsWith("http://") || urlAsString.startsWith("https://")) {
            urlAsString
        } else {
            "http://$urlAsString"
        }
    }

    fun addWebSiteShortcut(urlAsString: String) {
        callShortcutManager {
            val shortcut = createShortcutForUrl(normalizeUrl(urlAsString))
            mShortcutManager.addDynamicShortcuts(listOf(shortcut))
        }
    }

    fun removeShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.removeDynamicShortcuts(listOf(shortcut.id))
    }

    fun disableShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.disableShortcuts(listOf(shortcut.id))
    }

    fun enableShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.enableShortcuts(listOf(shortcut.id))
    }

    private fun fetchFavicon(uri: Uri?): Bitmap? {
        val iconUri = uri!!.buildUpon().path("favicon.ico").build()
        Log.i(TAG, "Fetching favicon from: $iconUri")
        return try {
            val conn = URL(iconUri.toString()).openConnection()
            conn.connect()
            val inputStream: InputStream = conn.getInputStream()
            val bis = BufferedInputStream(inputStream, 8192)
            BitmapFactory.decodeStream(bis)
        } catch (e: IOException) {
            Log.w(TAG, "Failed to fetch favicon from $iconUri", e)
            null
        }
    }

    companion object {
        private const val TAG = Main.TAG
        private const val EXTRA_LAST_REFRESH = "com.example.android.appshortcuts.EXTRA_LAST_REFRESH"
        private const val REFRESH_INTERVAL_MS = (60 * 60 * 1000).toLong()
    }

}