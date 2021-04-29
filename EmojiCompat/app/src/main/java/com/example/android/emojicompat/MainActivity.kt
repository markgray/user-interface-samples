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

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.provider.FontRequest
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import java.lang.ref.WeakReference

/**
 * This sample demonstrates usage of EmojiCompat support library. You can use this library
 * to prevent your app from showing missing emoji characters in the form of tofu (□). You
 * can use either bundled or downloadable emoji fonts. This sample shows both usages.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting.
     *
     * @param savedInstanceState We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEmojiCompat()
        setContentView(R.layout.activity_main)

        // TextView variant provided by EmojiCompat library
        val emojiTextView = findViewById<TextView>(R.id.emoji_text_view)
        emojiTextView.text = getString(R.string.emoji_text_view, EMOJI)

        // EditText variant provided by EmojiCompat library
        val emojiEditText = findViewById<TextView>(R.id.emoji_edit_text)
        emojiEditText.text = getString(R.string.emoji_edit_text, EMOJI)

        // Button variant provided by EmojiCompat library
        val emojiButton = findViewById<TextView>(R.id.emoji_button)
        emojiButton.text = getString(R.string.emoji_button, EMOJI)

        // Regular TextView without EmojiCompat support; you have to manually process the text
        val regularTextView = findViewById<TextView>(R.id.regular_text_view)
        EmojiCompat.get().registerInitCallback(InitCallback(regularTextView))

        // Custom TextView
        val customTextView = findViewById<TextView>(R.id.emoji_custom_text_view)
        customTextView.text = getString(R.string.custom_text_view, EMOJI)
    }

    private fun initEmojiCompat() {
        val config: EmojiCompat.Config = if (USE_BUNDLED_EMOJI) {
            // Use the bundled font for EmojiCompat
            BundledEmojiCompatConfig(applicationContext)
        } else {
            // Use a downloadable font for EmojiCompat
            val fontRequest = FontRequest(
                "com.google.android.gms.fonts",
                "com.google.android.gms",
                "Noto Color Emoji Compat",
                R.array.com_google_android_gms_fonts_certs)
            FontRequestEmojiCompatConfig(applicationContext, fontRequest)
        }
        config.setReplaceAll(true)
            .registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    Log.i(TAG, "EmojiCompat initialized")
                }

                override fun onFailed(throwable: Throwable?) {
                    Log.e(TAG, "EmojiCompat initialization failed", throwable)
                }
            })
        EmojiCompat.init(config)
    }

    private class InitCallback(regularTextView: TextView) : EmojiCompat.InitCallback() {
        private val mRegularTextViewRef: WeakReference<TextView> = WeakReference(regularTextView)
        override fun onInitialized() {
            val regularTextView = mRegularTextViewRef.get()
            if (regularTextView != null) {
                val compat = EmojiCompat.get()
                val context = regularTextView.context
                regularTextView.text = compat.process(context.getString(R.string.regular_text_view, EMOJI))
            }
        }

    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "MainActivity"

        /** Change this to `false` when you want to use the downloadable Emoji font.  */
        private const val USE_BUNDLED_EMOJI = true

        // [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F4BB] (PERSONAL COMPUTER)
        private const val WOMAN_TECHNOLOGIST = "\uD83D\uDC69\u200D\uD83D\uDCBB"

        // [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F3A4] (MICROPHONE)
        private const val WOMAN_SINGER = "\uD83D\uDC69\u200D\uD83C\uDFA4"
        private const val EMOTIONS = "\uD83D\uDE0D\uD83E\uDD29\n"
        const val EMOJI = "$WOMAN_TECHNOLOGIST $WOMAN_SINGER $EMOTIONS"
    }
}