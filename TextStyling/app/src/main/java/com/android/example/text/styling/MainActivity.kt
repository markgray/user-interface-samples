/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.android.example.text.styling

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.android.example.text.styling.parser.Parser
import com.android.example.text.styling.renderer.MarkdownBuilder

/**
 * This sample demonstrates techniques for stying text; it is not intended to be a full markdown
 * parser.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and set our content
     * view to our layout file `R.layout.activity_main` which consists of a [ConstraintLayout]
     * root view holding a single [TextView].
     *
     * We initialize our [ConstraintLayout] variable `rootView`
     * to the view with ID `R.id.root_view` then call
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
     * We initialize our [TextView] variable `val textView` by finding that view whose ID is
     * `R.id.styled_text`. We initialize our [Int] variable `val bulletPointColor` by retrieving
     * the color `R.color.colorAccent` which is `FF4081` -- a pastel shade of red. We initialize
     * our [Int] variable `val codeBackgroundColor` by retrieving the color `R.color.code_background`
     * which is `BBBBBB` -- a light gray.
     *
     * We then initialize our [Typeface] variable `val codeBlockTypeface` by using the `font-family`
     * which is defined in the font/inconsolata.xml file to download the "Inconsolata" font from the
     * font provider "com.google.android.gms.fonts".
     *
     * We construct a [MarkdownBuilder] using `bulletPointColor` as the color to be used for bullet
     * point spans, using `codeBackgroundColor` as the color to be used for the background color of
     * code blocks, using `codeBlockTypeface` as the [Typeface] to be used for code blocks, and a
     * new instance of [Parser] to be used to parse the markdown text. We then call the method
     * `markdownToSpans` of that [MarkdownBuilder] to have it create a [CharSequence] from the
     * markdown [String] with the resource ID `R.string.display_text` and use that [CharSequence] to
     * initialize our variable `val text`. Finally we set the `text` of our [TextView] variable
     * `textView` to our [CharSequence] variable `text`
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<ConstraintLayout>(R.id.root_view)
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
        val textView = findViewById<TextView>(R.id.styled_text)

        // This is a simple markdown parser, where:
        // Paragraphs starting with “> ” are transformed into quotes. Quotes can't contain
        // other markdown elements
        // Text enclosed in “`” will be transformed into inline code block
        // Lines starting with “+ ” or “* ” will be transformed into bullet points. Bullet
        // points can contain nested markdown elements, like code.
        val bulletPointColor = ContextCompat.getColor(this, R.color.colorAccent)
        val codeBackgroundColor = ContextCompat.getColor(this, R.color.code_background)
        val codeBlockTypeface: Typeface? = ResourcesCompat.getFont(this, R.font.inconsolata)
        val text: CharSequence = MarkdownBuilder(
            bulletPointColor,
            codeBackgroundColor,
            codeBlockTypeface,
            Parser()
        ).markdownToSpans(getString(R.string.display_text))
        textView.text = text
    }
}
