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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.provider.FontRequest
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.emoji.bundled.BundledEmojiCompatConfig
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.EmojiSpan
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.emoji.widget.EmojiAppCompatButton
import androidx.emoji.widget.EmojiAppCompatEditText
import androidx.emoji.widget.EmojiAppCompatTextView
import java.lang.ref.WeakReference

/**
 * This sample demonstrates usage of EmojiCompat support library. You can use this library
 * to prevent your app from showing missing emoji characters in the form of tofu (□). You
 * can use either bundled or downloadable emoji fonts. This sample shows both usages.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge]
     * to enable edge to edge display, then we call our super's implementation
     * of `onCreate`. We then call our method [initEmojiCompat] to have it initialize [EmojiCompat]
     * to use either the bundled "Noto Color Emoji Compat" font bundled with our app due to the
     * presence of a android:name="fontProviderRequests" meta-data element in our AndroidManifest
     * file (downloads the font when the app is installed) or the same font downloaded using a
     * [FontRequest] depending on whether our [USE_BUNDLED_EMOJI] field is `true` or `false`. Next
     * we set our content view to our layout file `R.layout.activity_main` which consists of a
     * [ScrollView] root view holding a vertical [LinearLayout] with an [EmojiAppCompatTextView], an
     * [EmojiAppCompatEditText], an [EmojiAppCompatButton], a [TextView] and our [CustomTextView]
     * custom [TextView].
     *
     * We initialize our [ScrollView] variable `rootView`
     * to the view with ID `R.id.scroll` then call
     * [ViewCompat.setOnApplyWindowInsetsListener] to take over the policy
     * for applying window insets to `rootView`, with the `listener`
     * argument a lambda that accepts the [View] passed the lambda
     * in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `insets` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument, then it updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `insets.left`, the right margin set to
     * `insets.right`, the top margin set to `insets.top`, and the bottom margin
     * set to `insets.bottom`. Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * We then proceed to locate each of the above text displaying views in order to set their text
     * to a [CharSequence] which include emojis from [EmojiCompat]:
     *  - `val emojiTextView` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.emoji_text_view`. It is an [EmojiAppCompatTextView] so we can just set its `text`
     *  property to the emoji containing formatted string "Emoji TextView [EMOJI]" created by calling
     *  the [getString] method with the resource ID `R.string.emoji_text_view` formatting string and
     *  the [EMOJI] string constant.
     *  - `val emojiEditText` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.emoji_edit_text`. It is an [EmojiAppCompatEditText] so we can just set its `text`
     *  property to the emoji containing formatted string "Emoji EditText [EMOJI]" created by calling
     *  the [getString] method with the resource ID `R.string.emoji_edit_text` formatting string and
     *  the [EMOJI] string constant.
     *  - `val emojiButton` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.emoji_button`. It is an [EmojiAppCompatButton] so we can just set its `text` property
     *  to the emoji containing formatted string "Emoji Button [EMOJI]" created by calling the
     *  [getString] method with the resource ID `R.string.emoji_button` formatting string and the
     *  [EMOJI] string constant.
     *  - `val regularTextView` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.regular_text_view`. This is a regular [TextView] without [EmojiCompat] support so we
     *  have to manually process the text and to do this we fetch the singleton [EmojiCompat] instance
     *  and register an instance of our initialization callback [InitCallback] constructed to use the
     *  singleton [EmojiCompat] to process the text and set the text of `regularTextView` to it in
     *  its `onInitialized` override (**Note:** if initialization has already occurred `onInitialized`
     *  is called immediately).
     *  - `val customTextView` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.emoji_custom_text_view`. It is an instance of our [CustomTextView] so we can just set
     *  its `text` property  to the emoji containing formatted string "Custom TextView [EMOJI]" created
     *  by calling the [getString] method with the resource ID `R.string.custom_text_view` formatting
     *  string and the [EMOJI] string constant.
     *
     * @param savedInstanceState We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        initEmojiCompat()
        setContentView(R.layout.activity_main)
        val rootView = findViewById<ScrollView>(R.id.scroll)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val insets: Insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                topMargin = insets.top
                bottomMargin = insets.bottom
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            EmojiCompat.get().registerInitCallback(InitCallback(regularTextView))
        }

        // Custom TextView
        val customTextView = findViewById<TextView>(R.id.emoji_custom_text_view)
        customTextView.text = getString(R.string.custom_text_view, EMOJI)
    }

    /**
     * Initialize the singleton instance of [EmojiCompat] with a configuration. If our static field
     * [USE_BUNDLED_EMOJI] is `true` it will be configured to use the bundled font using an instance
     * of [BundledEmojiCompatConfig] as its [EmojiCompat.Config], and if [USE_BUNDLED_EMOJI] is
     * `false` it will be configured to use a downloadable font using an instance of
     * [FontRequestEmojiCompatConfig] as its [EmojiCompat.Config].
     *
     * To do this an `if` expression is used to initialize our [EmojiCompat.Config] variable `val config`
     * with an instance of [BundledEmojiCompatConfig] constructed using the context of the single,
     * global Application object of the current process if [USE_BUNDLED_EMOJI] is `true` or `else`
     * to an instance of [FontRequestEmojiCompatConfig] constructed using the context of the single,
     * global Application object of the current process, and a [FontRequest] constructed to initialize
     * our variable `val fontRequest` which requests the font "Noto Color Emoji Compat" from the font
     * provider "com.google.android.gms.fonts".
     *
     * Having initialized `config` we call its [EmojiCompat.Config.setReplaceAll] method with `true`
     * to have [EmojiCompat] replace all the emojis it finds with the [EmojiSpan]s rather than try
     * to use the system provided emojis if they are available. We chain that call to a call to its
     * [EmojiCompat.Config.registerInitCallback] method to register an anonymous [EmojiCompat.InitCallback]
     * whose `onInitialized` override logs the message "EmojiCompat initialized", and whose `onFailed`
     * override logs the message "EmojiCompat initialization failed" with the [Throwable] it is called
     * with.
     *
     * Having configured `config` to our liking we call the [EmojiCompat.init] method with it to have
     * it initialize the singleton instance with the configuration `config`.
     */
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

    /**
     * The custom [EmojiCompat.InitCallback] we use to allow our regular [TextView] (resource ID
     * `R.id.regular_text_view`) to display the [EmojiCompat] emojis.
     *
     * We initialize our variable `val mRegularTextViewRef` to a [WeakReference] to our [TextView]
     * parameter `regularTextView` and override the `onInitialized` method of [EmojiCompat.InitCallback]
     * to have it have the [EmojiCompat] singleton instance process the formatted string which the
     * [Context.getString] method creates using the `R.string.regular_text_view` string format
     * "Regular TextView [EMOJI]", and set that [CharSequence] as the text of `regularTextView`.
     *
     * @param regularTextView the regular [TextView] instance in which we want to display the
     * [EmojiCompat] emojis.
     */
    private class InitCallback(regularTextView: TextView) : EmojiCompat.InitCallback() {
        /**
         * [WeakReference] to our [TextView] field `regularTextView`.
         */
        private val mRegularTextViewRef: WeakReference<TextView> = WeakReference(regularTextView)

        /**
         * Called when [EmojiCompat] is initialized and the emoji data is loaded. We initialize our
         * [TextView] variable `val regularTextView` to the referent of our [WeakReference] field
         * [mRegularTextViewRef], and if that is not `null` we initialize our variable `val compat`
         * to the singleton [EmojiCompat] instance, and our [Context] variable `val context` to the
         * [Context] of `regularTextView`. We then set the `text` of `regularTextView` to the string
         * returned by the [EmojiCompat.process] method of `compat` when it adds [EmojiSpan]s for the
         * emojis in the [CharSequence] returned by the [Context.getString] method of `context` when
         * it formats [EMOJI] into the format string "Regular TextView %s" (the string with resource
         * ID `R.string.regular_text_view`).
         */
        @SuppressLint("NewApi")
        override fun onInitialized() {
            val regularTextView: TextView? = mRegularTextViewRef.get()
            if (regularTextView != null) {
                val compat = EmojiCompat.get()
                val context: Context = regularTextView.context
                regularTextView.text = compat.process(context.getString(R.string.regular_text_view, EMOJI))
            }
        }

    }

    companion object {
        /**
         * TAG used for logging.
         */
        private const val TAG = "MainActivity"

        /**
         * Change this to `false` when you want to use the downloadable Emoji font.
         */
        private const val USE_BUNDLED_EMOJI = true

        /**
         * [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F4BB] (PERSONAL COMPUTER)
         */
        private const val WOMAN_TECHNOLOGIST = "\uD83D\uDC69\u200D\uD83D\uDCBB"

        /**
         * [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F3A4] (MICROPHONE)
         */
        private const val WOMAN_SINGER = "\uD83D\uDC69\u200D\uD83C\uDFA4"

        /**
         * A closed mouth smiley face with hearts for eyes and an open mouth smiley face with stars
         * for eyes.
         */
        private const val EMOTIONS = "\uD83D\uDE0D\uD83E\uDD29\n"

        /**
         * The string formed by concatenating the emoji strings defined above
         */
        const val EMOJI: String = "$WOMAN_TECHNOLOGIST $WOMAN_SINGER $EMOTIONS"
    }
}
