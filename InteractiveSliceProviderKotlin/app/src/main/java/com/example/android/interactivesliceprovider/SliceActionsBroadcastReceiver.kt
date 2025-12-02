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
@file:Suppress("DEPRECATION")

package com.example.android.interactivesliceprovider

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.slice.Slice.EXTRA_RANGE_VALUE
import android.app.slice.Slice.EXTRA_TOGGLE_STATE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.android.interactivesliceprovider.util.buildUriWithAuthority

/**
 * BroadcastReceiver to handle slice actions.
 *
 * It is responsible for receiving actions from the Slice, and updating the state of the slice.
 *
 * For example, when the user toggles the Wi-Fi in the slice, this class receives the
 * broadcast, attempts to change the Wi-Fi state, and sends a notification to the slice to
 * update itself.
 */
class SliceActionsBroadcastReceiver : BroadcastReceiver() {

    /**
     * Called when this BroadcastReceiver receives an Intent broadcast.
     *
     * It checks the intent's action and handles it accordingly.
     *
     *  - [InteractiveSliceProvider.ACTION_WIFI_CHANGED]: Toggles the Wi-Fi state and notifies a
     *  change on the slice URI to trigger an update.
     *  - [InteractiveSliceProvider.ACTION_TOAST]: Shows a toast with a message from the intent extras.
     *  - [InteractiveSliceProvider.ACTION_TOAST_RANGE_VALUE]: Shows a toast with a range value from
     *  the intent extras.
     */
    @SuppressLint("InlinedApi") // TODO: EXTRA_TOGGLE_STATE requires API 28
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            InteractiveSliceProvider.ACTION_WIFI_CHANGED -> {
                val wm = context.applicationContext
                    .getSystemService(Context.WIFI_SERVICE) as WifiManager
                val newState = intent.getBooleanExtra(EXTRA_TOGGLE_STATE, wm.isWifiEnabled)
                @Suppress("DEPRECATION") // TODO: Starting with SDK Q applications are not allowed to enable/disable Wifi
                wm.isWifiEnabled = newState
                // Wait a bit for wifi to update (TODO: is there a better way to do this?)
                val h = Handler(Looper.myLooper() ?: return)
                h.postDelayed({
                    val uri = context.buildUriWithAuthority("wifi")
                    context.contentResolver.notifyChange(uri, null)
                }, 1000)
            }

            InteractiveSliceProvider.ACTION_TOAST -> {
                val message = (intent.extras ?: return).getString(
                    InteractiveSliceProvider.EXTRA_TOAST_MESSAGE,
                    "no message"
                )
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }

            InteractiveSliceProvider.ACTION_TOAST_RANGE_VALUE -> {
                val range = (intent.extras ?: return).getInt(EXTRA_RANGE_VALUE, 0)
                Toast.makeText(context, "value: $range", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        /**
         * Returns a PendingIntent to be used as an action in a slice.
         *
         * @param context the context to use.
         * @param action the action to be broadcasted.
         * @param message an optional message to be included in the intent.
         */
        fun getIntent(context: Context, action: String, message: String?): PendingIntent {
            val intent = Intent(action)
            intent.setClass(context, SliceActionsBroadcastReceiver::class.java)
            // Ensure a new PendingIntent is created for each message.
            var requestCode = 0
            if (message != null) {
                intent.putExtra(InteractiveSliceProvider.EXTRA_TOAST_MESSAGE, message)
                requestCode = message.hashCode()
            }
            return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}