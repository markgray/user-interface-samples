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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLConnection
import java.util.function.BooleanSupplier

/**
 * This class exists to make it easier to interact with the [ShortcutManager] system level service.
 */
class ShortcutHelper(private val mContext: Context) {
    /** Coroutine variables */

    /**
     * [shortcutHelperJob] allows us to cancel all coroutines started by [ShortcutHelper].
     */
    private var shortcutHelperJob = Job()

    /**
     * A [CoroutineScope] that keeps track of all coroutines started by [ShortcutHelper]. Because we
     * pass it [shortcutHelperJob], any coroutine started in this scope can be cancelled by calling
     * `viewModelJob.cancel()`. By default, all coroutines started in [uiScope] will launch in
     * [Dispatchers.Main] which is the main thread on Android. This is a sensible default because
     * most coroutines started by [ShortcutHelper] update the UI after performing some processing.
     */
    private val uiScope = CoroutineScope(Dispatchers.Main + shortcutHelperJob)

    /**
     * Our handle to the [ShortcutManager] system level service.
     */
    private val mShortcutManager: ShortcutManager = mContext.getSystemService(ShortcutManager::class.java)

    /**
     * If this application is always supposed to have dynamic shortcuts, then we should publish them
     * here. We have none. Called from the `onCreate` override of [Main]. If the size of the [List]
     * of [ShortcutInfo] objects returned by the `dynamicShortcuts` property of our [ShortcutManager]
     * field [mShortcutManager] is 0 we restore dynamic shortcuts our app is always supposed to have.
     */
    fun maybeRestoreAllDynamicShortcuts() {
        @Suppress("ControlFlowWithEmptyBody")
        if (mShortcutManager.dynamicShortcuts.size == 0) {
            // NOTE: If this application is always supposed to have dynamic shortcuts, then publish
            // them here.
            // Note when an application is "restored" on a new device, all dynamic shortcuts
            // will *not* be restored but the pinned shortcuts *will*.
        }
    }

    /**
     * Called to report the user selected the shortcut containing ID [id] (or has completed an action
     * in the app that is equivalent to selecting the shortcut). It is called from the `addWebSite`
     * method of [Main] to report that the user has expressed a desire to add another website to our
     * shortcuts. We just call the [ShortcutManager.reportShortcutUsed] method of our [ShortcutManager]
     * field [mShortcutManager] with our [id] parameter.
     *
     * @param id the ID of the shortcut, in our case only the ID `ID_ADD_WEBSITE` is used.
     */
    fun reportShortcutUsed(id: String?) {
        mShortcutManager.reportShortcutUsed(id)
    }

    /**
     * Use this when interacting with [ShortcutManager] to show consistent error messages. This is
     * called with a lambda whose last statement is a call to a [ShortcutManager] method which
     * returns `true` for success and `false` for failure and is used by our [addWebSiteShortcut]
     * method when it calls the [ShortcutManager.addDynamicShortcuts] method of [mShortcutManager]
     * and by our [refreshShortcuts] method when it calls the [ShortcutManager.updateShortcuts]
     * method of [mShortcutManager]. If the result of our [BooleanSupplier] parameter [r] is `false`
     * we toast the message "Call to ShortcutManager is rate-limited", and if the lambda we are
     * called with throws an exception we toast the message "Error while calling ShortcutManager:"
     * along with the [String] vaule or the [Exception] caught.
     *
     * @param r If `true` no error has occurred in the call to the [ShortcutManager], and if `false`
     * the call is rate-limited (ie. an error).
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
    }

    /**
     * Returns all mutable dynamic shortcuts and pinned shortcuts in a single [List] of [ShortcutInfo]
     * objects without any duplicates. To do this its `get` getter initializes its [MutableList] of
     * [ShortcutInfo] variable `val ret` to an [ArrayList], and its [HashSet] of [String] variable
     * `val seenKeys` to a new instance. We loop over the [ShortcutInfo] variable `shortcut` for each
     * object in the [List] of [ShortcutInfo] objects returned by the `dynamicShortcuts` property of
     * our [ShortcutManager] field [mShortcutManager] (all our dynamic shortcuts), and if the
     * `isImmutable` property of `shortcut` is `false` we add `shortcut` to `ret` and add the `id`
     * property of `shortcut` to `seenKeys`. Then we loop over the [ShortcutInfo] variable `shortcut`
     * for each object in the [List] of [ShortcutInfo] objects returned by the `pinnedShortcuts`
     * property of our [ShortcutManager] field [mShortcutManager] (all our pinned shortcuts), and if
     * the `isImmutable` property of `shortcut` is `false` and it is not already in the set `seenKeys`
     * we add `shortcut` to `ret` and add the `id` property of `shortcut` to `seenKeys`. Finally we
     * return `ret` to the caller (in kotlin it is the value of our [shortcuts] property).
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
     * Called when the activity starts from the `onCreate` override of [Main] and from the `onReceive`
     * override of [MyReceiver] when it receives an ACTION_LOCALE_CHANGED broadcast `Intent`. Looks
     * for shortcuts that have been pushed and refreshes them (but the refresh part isn't implemented
     * yet...). We call the [CoroutineScope.noParamNoResultAsync] extension function to do the
     * following in a background thread:
     *  - We log the fact that we are "refreshingShortcuts...".
     *  - We initialize our [Long] variable `val now` to the current time in milliseconds.
     *  - We initialize our [Long] variable `val staleThreshold` to `now` if [force] is `true` or to
     *  `now` minus [REFRESH_INTERVAL_MS] if it is `false`.
     *  - We initialize our [MutableList] of [ShortcutInfo] variable `val updateList` to an [ArrayList].
     *  - We loop over the [ShortcutInfo] variable `shortcut` for each object in the [List] of
     *  [ShortcutInfo] objects in our field [shortcuts].
     *      - If the `isImmutable` property of `shortcut` is `true` we skip that [ShortcutInfo].
     *      - We initialize our [PersistableBundle] variable `val extras` to the `extras` of `shortcut`
     *      - If `extras` is not `null` and the [Long] stored under the key `EXTRA_LAST_REFRESH` is
     *      greater than or equal to `staleThreshold` we skip that [ShortcutInfo].
     *      - We log the fact that we are "Refreshing" the shortcut with the `id` property of `shortcut`.
     *      - We initialize our [ShortcutInfo.Builder] variable `val b` to a new instance with the
     *      ID of the shortcut it is to build being the `id` property of `shortcut`.
     *      - We call our [setSiteInformation] method to have it configure `b` with the site info of
     *      `shortcut` using the `data` property of the `intent` property of `shortcut`
     *      - We call our [setExtras] method to have it add the current time in milliseconds to a
     *      [PersistableBundle] under the key [EXTRA_LAST_REFRESH] and add that [PersistableBundle]
     *      as an extra to `b`.
     *      - We then build `b` into a [ShortcutInfo] and add it to `updateList`.
     *  - Having finished creating `updateList`, if its `size` is greater than 0 we call our method
     *  [callShortcutManager] with a lambda that calls the [ShortcutManager.updateShortcuts] method
     *  of our  [ShortcutManager] field [mShortcutManager] with `updateList` to update all existing
     *  shortcuts with the same IDs as those in `updateList`.
     *
     * @param force if `true` force an immediate refresh of all shortcuts, if `false` a refresh of a
     * shortcut is performed only if the shortcut is older than [REFRESH_INTERVAL_MS] milliseconds
     * (60 minutes). We are called with `false` from the `onCreate` override of [Main] and with `true`
     * from the `onReceive` override of [MyReceiver]
     */
    fun refreshShortcuts(force: Boolean) {
        uiScope.noParamNoResultAsync(
            doInBackground = {
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
                    val extras: PersistableBundle? = shortcut.extras
                    if (extras != null && extras.getLong(EXTRA_LAST_REFRESH) >= staleThreshold) {
                        // Shortcut still fresh.
                        continue
                    }
                    Log.i(TAG, "Refreshing shortcut: " + shortcut.id)
                    val b = ShortcutInfo.Builder(mContext, shortcut.id)
                    setSiteInformation(b, (shortcut.intent ?: return@noParamNoResultAsync).data)
                    setExtras(b)
                    updateList.add(b.build())
                }
                // Call update.
                if (updateList.size > 0) {
                    callShortcutManager { mShortcutManager.updateShortcuts(updateList) }
                }
            },
            onPostExecute = {
            }
        )

    }

    /**
     * Creates a [ShortcutInfo] object from its URL [String] parameter [urlAsString]. First we log
     * the fact that we were called with [urlAsString]. We initialize our [ShortcutInfo.Builder]
     * variable `val b` with a new instance whose ID is [urlAsString], and our [Uri] variable
     * `val uri` to the [Uri] that the [Uri.parse] method creates from [urlAsString]. We set the
     * [Intent] of `b` to an [Intent] whose action is [Intent.ACTION_VIEW] (display the data to the
     * user) and whose [Intent] data URI is `uri`. We call our [setSiteInformation] method to have
     * it set the site information of `b` to those of `uri` (the short title, the text, and the icon),
     * and call our [setExtras] method to have it set the extras of `b` to a [PersistableBundle] which
     * has the current time in milliseconds stored under the key [EXTRA_LAST_REFRESH] in it. Finally
     * we build and return the [ShortcutInfo] built from `b` to the caller.
     *
     * @param urlAsString a [String] holding a URL for a website beginning with "http://" or "https://"
     */
    private fun createShortcutForUrl(urlAsString: String): ShortcutInfo {
        Log.i(TAG, "createShortcutForUrl: $urlAsString")
        val b = ShortcutInfo.Builder(mContext, urlAsString)
        val uri: Uri = Uri.parse(urlAsString)
        b.setIntent(Intent(Intent.ACTION_VIEW, uri))
        setSiteInformation(b, uri)
        setExtras(b)
        return b.build()
    }

    /**
     * Sets the site information of its [ShortcutInfo.Builder] parameter [b] to those pertaining to
     * its [Uri] parameter [uri] (the short title, the text, and the icon). First we set the short
     * label of [b] to the `host` property of [uri] (the encoded host from the authority for this
     * URI). Next we set the text of [b] to the encoded string representation of [uri]. We set our
     * [Bitmap] variable `val bmp` to the "favicon.ico" icon that is associtated with [uri] which
     * our [fetchFavicon] method fetches from the internet. If `bmp` is not `null` we set the icon
     * of [b] to it, otherwise we set it to the [Icon] created from the drawable with resource ID
     * [R.drawable.link]. Finally we return [b] to the caller to allow chaining.
     *
     * @param b the [ShortcutInfo.Builder] to which we add the site information pertaining to our
     * [Uri] parameter [uri].
     * @param uri the [Uri] whose [ShortcutInfo] object is being built in our [ShortcutInfo.Builder]
     * parameter [b].
     * @return the modified [ShortcutInfo.Builder] parameter passed us in [b] to allow chaining.
     */
    private fun setSiteInformation(b: ShortcutInfo.Builder, uri: Uri?): ShortcutInfo.Builder {
        // TODO Get the actual site <title> and use it.
        // TODO Set the current locale to accept-language to get localized title.
        b.setShortLabel(uri!!.host!!)
        b.setLongLabel(uri.toString())
        val bmp: Bitmap? = fetchFavicon(uri)
        if (bmp != null) {
            b.setIcon(Icon.createWithBitmap(bmp))
        } else {
            b.setIcon(Icon.createWithResource(mContext, R.drawable.link))
        }
        return b
    }

    /**
     * Constructs a [PersistableBundle] holding the current time in milliseconds stored under the key
     * [EXTRA_LAST_REFRESH] and adds it as the extras of its [ShortcutInfo.Builder] parameter [b].
     * First we initialize our [PersistableBundle] variable `val extras` with a new instance, then we
     * store the current time in milliseconds under the key [EXTRA_LAST_REFRESH] in `extras`, and set
     * the extras of [b] to `extras`. Finally we return [b] to the caller to allow chaining.
     *
     * @param b the [ShortcutInfo.Builder] we are to add a [PersistableBundle] to as its extras.
     * @return the modified [ShortcutInfo.Builder] parameter passed us in [b] to allow chaining.
     */
    private fun setExtras(b: ShortcutInfo.Builder): ShortcutInfo.Builder {
        val extras = PersistableBundle()
        extras.putLong(EXTRA_LAST_REFRESH, System.currentTimeMillis())
        b.setExtras(extras)
        return b
    }

    /**
     * If its [String] parameter [urlAsString] starts with the substrings "http://" or "https://"
     * this method just returns [urlAsString] as is, otherwise it will prepend the substring "http://"
     * to [urlAsString] and return that.
     *
     * @param urlAsString a [String] which may or may not be usable as is for a website URL (ie. it
     * starts with the substrings "http://" or "https://").
     * @return a [String] which *can* be used as a website URL (ie. it definitely starts with either
     * the substring "http://" or "https://").
     */
    private fun normalizeUrl(urlAsString: String): String {
        return if (urlAsString.startsWith("http://") || urlAsString.startsWith("https://")) {
            urlAsString
        } else {
            "http://$urlAsString"
        }
    }

    /**
     * Creates a [ShortcutInfo] from the website URL contained in its [String] parameter [urlAsString]
     * and publishes this dynamic shortcut to the [ShortcutManager]. We call our [callShortcutManager]
     * method with a lambda which first uses our [createShortcutForUrl] method to create a [ShortcutInfo]
     * from the normalized URL [String] returned by our [normalizeUrl] method and uses that [ShortcutInfo]
     * to initialize its variable `val shortcut`. It then calls the [ShortcutManager.addDynamicShortcuts]
     * method of our [ShortcutManager] field [mShortcutManager] to publish shortcut `shortcut`. The
     * method [callShortcutManager] is called this way so that it can toast an error message if the
     * [ShortcutManager.addDynamicShortcuts] method returns `false` or throws an [Exception].
     *
     * @param urlAsString a [String] which may be used to connect to a website (once [normalizeUrl]
     * makes sure it starts with the substrings "http://" or "https://")
     */
    fun addWebSiteShortcut(urlAsString: String) {
        callShortcutManager {
            val shortcut: ShortcutInfo = createShortcutForUrl(normalizeUrl(urlAsString))
            mShortcutManager.addDynamicShortcuts(listOf(shortcut))
        }
    }

    /**
     * Removes the dynamic shortcut of [ShortcutInfo] parameter [shortcut] by its ID. It does this
     * by calling the [ShortcutManager.removeDynamicShortcuts] method of [ShortcutManager] field
     * [mShortcutManager] with the `id` property of [shortcut]. Called from the `onClick` override
     * of [Main] when the ID of the `View` clicked is [R.id.remove] (the "Remove" `Button` in the
     * layout file layout/list_item.xml which is used for each of the items in the `ListView` of
     * the UI of [Main]).
     *
     * @param shortcut the [ShortcutInfo] object of the shortcut the user wants to remove.
     */
    fun removeShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.removeDynamicShortcuts(listOf(shortcut.id))
    }

    /**
     * Disables the dynamic shortcut of [ShortcutInfo] parameter [shortcut] by its ID. It does this
     * by calling the [ShortcutManager.disableShortcuts] method of [ShortcutManager] field
     * [mShortcutManager] with the `id` property of [shortcut]. Called from the `onClick` override
     * of [Main] when the ID of the `View` clicked is [R.id.disable] and the shortcut is presently
     * enabled (the `Button` is in the layout file layout/list_item.xml which is used for each of
     * the items in the `ListView` of the UI of [Main]  -- its label is set to "Disable" when the
     * shortcut is currently enabled).
     *
     * @param shortcut the [ShortcutInfo] object of the shortcut the user wants to disable.
     */
    fun disableShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.disableShortcuts(listOf(shortcut.id))
    }

    /**
     * Enables the dynamic shortcut of [ShortcutInfo] parameter [shortcut] by its ID. It does this
     * by calling the [ShortcutManager.enableShortcuts] method of [ShortcutManager] field
     * [mShortcutManager] with the `id` property of [shortcut]. Called from the `onClick` override
     * of [Main] when the ID of the `View` clicked is [R.id.disable] and the shortcut is presently
     * disabled (the `Button` is in the layout file layout/list_item.xml which is used for each of
     * the items in the `ListView` of the UI of [Main] -- its label is set to "Enable" when the
     * shortcut is currently disabled).
     *
     * @param shortcut the [ShortcutInfo] object of the shortcut the user wants to enable.
     */
    fun enableShortcut(shortcut: ShortcutInfo) {
        mShortcutManager.enableShortcuts(listOf(shortcut.id))
    }

    /**
     * Fetches the "favicon" associated with the website whose URL is our [Uri] parameter [uri] and
     * if it succeeds returns it as a [Bitmap], and if it fails it returns `null`. First we initialize
     * our [Uri] variable `val iconUri` by constructing a new [Uri.Builder] by copying the attributes
     * from [Uri] parameter [uri], appending the path "favicon.ico" to it, and then building the
     * [Uri.Builder] into a [Uri]. We log the fact that we are fetching the favicon from `iconUri`,
     * then wrapped in a `try` block intended to catch [IOException] we:
     *  - initialize our [URLConnection] variable `val conn` by converting `iconUri` to a [String]
     *  and constructing a [URL] from that [String] whose [URL.openConnection] method we use to
     *  open a [URLConnection] to the [URL].
     *  - we call the [URLConnection.connect] method of `conn` to open a communications link to the
     *  resource referenced by `conn`.
     *  - we initialize our [InputStream] variable `val inputStream` to an input stream that reads
     *  from the `conn` open connection.
     *  - we initialize our [BufferedInputStream] variable `val bis` to use `inputStream` as its
     *  underlying input stream with a buffer size of 8192.
     *  - the last line of the `try` block calls the [BitmapFactory.decodeStream] method with `bis`
     *  to have it decode it into a [Bitmap] which we return to our caller.
     *  - If the `try` block catches an [IOException] we log the fact that we failed and return `null`
     *  to the caller.
     *
     * @param uri the [Uri] of the website whose "favicon" we are to fetch, decode and return.
     * @return a [Bitmap] version of the "favicon" if found, otherwise `null`.
     */
    private fun fetchFavicon(uri: Uri?): Bitmap? {
        val iconUri: Uri = (uri ?: return null).buildUpon().path("favicon.ico").build()
        Log.i(TAG, "Fetching favicon from: $iconUri")
        return try {
            val conn: URLConnection = URL(iconUri.toString()).openConnection()
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
        /**
         * TAG to use for logging
         */
        private const val TAG = Main.TAG

        /**
         * The key under which we store the current time in milliseconds when the [ShortcutInfo] was
         * last refreshed in the [PersistableBundle] extra to that [ShortcutInfo].
         *
         */
        private const val EXTRA_LAST_REFRESH = "com.example.android.appshortcuts.EXTRA_LAST_REFRESH"

        /**
         * The length of time to wait between refreshing a [ShortcutInfo] (60 minutes)
         */
        private const val REFRESH_INTERVAL_MS = (60 * 60 * 1000).toLong()
    }

}