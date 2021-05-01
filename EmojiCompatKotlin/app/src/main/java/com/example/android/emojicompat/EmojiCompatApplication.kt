/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.example.android.emojicompat

import android.app.Application
import android.content.Context
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.core.provider.FontRequest
import android.util.Log
import androidx.emoji.text.EmojiSpan

/**
 * This application uses [EmojiCompat] which we configure here in our [onCreate] override.
 */
@Suppress("unused") // it is used as the android:name= attribute of the application element
class EmojiCompatApplication : Application() {

    companion object {
        /**
         * TAG used for logging
         */
        private const val TAG = "EmojiCompatApplication"

        /**
         * Change this to `false` when you want to use the downloadable Emoji font.
         */
        private const val USE_BUNDLED_EMOJI = true
    }

    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created.
     *
     * First we call our super's implementation of `onCreate`, then we declare our [EmojiCompat.Config]
     * variable `val config`. If our [USE_BUNDLED_EMOJI] static field is `true` we initialize `config`
     * to a new instance of [BundledEmojiCompatConfig] constructed to use the context of the single,
     * global Application object of the current process as its [Context] (this configuration will cause
     * [EmojiCompat] to use the font loaded when the app is installed due to the presence of a `meta-data`
     * element in the `application` element of our AndroidManifest.xml file with a
     * android:name="fontProviderRequests" attribute and a android:value="Noto Color Emoji Compat"
     * attribute).
     *
     * If [USE_BUNDLED_EMOJI] is `false` we want [EmojiCompat] to download the font it uses so we
     * initialize our [FontRequest] variable `val fontRequest` to a new instance which will request
     * the font "Noto Color Emoji Compat" from the font provider "com.google.android.gms.fonts", and
     * initialize `config` to a new instance of [FontRequestEmojiCompatConfig] constructed to use
     * the context of the single, global Application object of the current process as its [Context],
     * and `fontRequest` as its [FontRequest] then chain a call to its `setReplaceAll` method with
     * `true` so that [EmojiCompat] will replace all the emojis it finds with its [EmojiSpan]s instead
     * of using system emojis that it finds available, and to that we chain a call to `registerInitCallback`
     * to register an anonymous [EmojiCompat.InitCallback] whose `onInitialized` override logs the
     * message "EmojiCompat initialized", and whose `onFailed` override logs the message "EmojiCompat
     * initialization failed" with the [Throwable] that was generated.
     *
     * Having initialized `config` appropriately we call the [EmojiCompat.init] method with `config`
     * to initialize the singleton instance of [EmojiCompat] with this configuration.
     */
    override fun onCreate() {
        super.onCreate()

        val config: EmojiCompat.Config
        if (USE_BUNDLED_EMOJI) {
            // Use the bundled font for EmojiCompat
            config = BundledEmojiCompatConfig(applicationContext)
        } else {
            // Use a downloadable font for EmojiCompat
            val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
            config = FontRequestEmojiCompatConfig(applicationContext, fontRequest)
                .setReplaceAll(true)
                .registerInitCallback(object : EmojiCompat.InitCallback() {
                    override fun onInitialized() {
                        Log.i(TAG, "EmojiCompat initialized")
                    }

                    override fun onFailed(throwable: Throwable?) {
                        Log.e(TAG, "EmojiCompat initialization failed", throwable)
                    }
                })
        }
        EmojiCompat.init(config)
    }

}
