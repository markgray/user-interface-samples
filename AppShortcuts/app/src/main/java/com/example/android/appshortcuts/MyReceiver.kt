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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * This is named as a [BroadcastReceiver] for the action "android.intent.action.LOCALE_CHANGED" in
 * our AndroidManifest.xml file. It will receive this [Intent] when the current device's locale has
 * changed.
 */
class MyReceiver : BroadcastReceiver() {
    /**
     * This method is called when the [BroadcastReceiver] is receiving an [Intent] broadcast.
     * During this time you can use the other methods on [BroadcastReceiver] to view/modify the
     * current result values. This method is always called within the main thread of its process.
     * We log the [Intent] that called us, and if the `action` of our our [Intent] parameter
     * [intent] is [Intent.ACTION_LOCALE_CHANGED] we construct an instance of [ShortcutHelper] in
     * order to call its [ShortcutHelper.refreshShortcuts] method with the `force` parameter `true`
     * in order to have it look for shortcuts that have been pushed and refresh them.
     *
     * @param context The [Context] in which the receiver is running.
     * @param intent The [Intent] being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive: $intent")
        if (Intent.ACTION_LOCALE_CHANGED == intent.action) {
            // Refresh all shortcut to update the labels.
            // (Right now shortcut labels don't contain localized strings though.)
            ShortcutHelper(context).refreshShortcuts(true)
        }
    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = Main.TAG
    }
}