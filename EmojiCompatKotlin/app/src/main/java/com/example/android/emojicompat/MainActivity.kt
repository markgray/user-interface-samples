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

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.emoji.text.EmojiCompat
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.emoji.text.EmojiSpan
import androidx.emoji.widget.EmojiAppCompatButton
import androidx.emoji.widget.EmojiAppCompatEditText
import androidx.emoji.widget.EmojiAppCompatTextView
import java.lang.ref.WeakReference

/**
 * This sample demonstrates usage of [EmojiCompat] support library. You can use this library
 * to prevent your app from showing missing emoji characters in the form of tofu (□). You
 * can use either bundled or downloadable emoji fonts. This sample shows both usages.
 */
class MainActivity : AppCompatActivity() {

    companion object {
        /**
         * [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F4BB] (PERSONAL COMPUTER)
         */
        private const val WOMAN_TECHNOLOGIST = "\uD83D\uDC69\u200D\uD83D\uDCBB"

        /**
         * [U+1F469] (WOMAN) + [U+200D] (ZERO WIDTH JOINER) + [U+1F3A4] (MICROPHONE)
         */
        private const val WOMAN_SINGER = "\uD83D\uDC69\u200D\uD83C\uDFA4"

        /**
         * The emoji [String] that is displayed in each of our [TextView]s, it consists of the
         * [String] field [WOMAN_TECHNOLOGIST] followed by the [String] field [WOMAN_SINGER]
         */
        const val EMOJI: String = "$WOMAN_TECHNOLOGIST $WOMAN_SINGER"
    }

    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and set our content
     * view to our layout file `R.layout.activity_main` which consists of a [ScrollView] root view
     * holding a vertical [LinearLayout] with an [EmojiAppCompatTextView], an
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
     *  have to manually process the text and to do this we fetch the singleton [EmojiCompat]
     *  instance and register an instance of our initialization callback [InitCallback] constructed
     *  to use the singleton [EmojiCompat] to process the text and set the text of `regularTextView`
     *  to it in its `onInitialized` override (**Note:** if initialization has already occurred
     *  `onInitialized` is called immediately).
     *  - `val customTextView` is initialized by finding the [TextView] in our UI with resource ID
     *  `R.id.emoji_custom_text_view`. It is an instance of our [CustomTextView] so we can just set
     *  its `text` property  to the emoji containing formatted string "Custom TextView [EMOJI]"
     *  created by calling the [getString] method with the resource ID `R.string.custom_text_view`
     *  formatting string and the [EMOJI] string constant.
     *
     * @param savedInstanceState We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
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
        val emojiTextView: TextView = findViewById(R.id.emoji_text_view)
        emojiTextView.text = getString(R.string.emoji_text_view, EMOJI)

        // EditText variant provided by EmojiCompat library
        val emojiEditText: TextView = findViewById(R.id.emoji_edit_text)
        emojiEditText.text = getString(R.string.emoji_edit_text, EMOJI)

        // Button variant provided by EmojiCompat library
        val emojiButton: TextView = findViewById(R.id.emoji_button)
        emojiButton.text = getString(R.string.emoji_button, EMOJI)

        // Regular TextView without EmojiCompat support; you have to manually process the text
        val regularTextView: TextView = findViewById(R.id.regular_text_view)
        EmojiCompat.get().registerInitCallback(InitCallback(regularTextView))

        // Custom TextView
        val customTextView: TextView = findViewById(R.id.emoji_custom_text_view)
        customTextView.text = getString(R.string.custom_text_view, EMOJI)
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
        val regularTextViewRef = WeakReference(regularTextView)

        /**
         * Called when [EmojiCompat] is initialized and the emoji data is loaded. We initialize our
         * [TextView] variable `val regularTextView` to the referent of our [WeakReference] field
         * [regularTextViewRef], and if that is not `null` we initialize our variable `val compat`
         * to the singleton [EmojiCompat] instance, and our [Context] variable `val context` to the
         * [Context] of `regularTextView`. We then set the `text` of `regularTextView` to the string
         * returned by the [EmojiCompat.process] method of `compat` when it adds [EmojiSpan]s for the
         * emojis in the [CharSequence] returned by the [Context.getString] method of `context` when
         * it formats [EMOJI] into the format string "Regular TextView %s" (the string with resource
         * ID `R.string.regular_text_view`).
         */
        override fun onInitialized() {
            val regularTextView = regularTextViewRef.get()
            if (regularTextView != null) {
                val compat = EmojiCompat.get()
                val context = regularTextView.context
                regularTextView.text = compat.process(
                    context.getString(R.string.regular_text_view, EMOJI))
            }
        }

    }

}
