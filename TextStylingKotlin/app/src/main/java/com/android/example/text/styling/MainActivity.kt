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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import android.graphics.Typeface
import com.android.example.text.styling.parser.Parser
import com.android.example.text.styling.renderer.MarkdownBuilder

/**
 * This sample demonstrates techniques for stying text; it is not intended to be a full markdown
 * parser.
 */
class MainActivity : AppCompatActivity() {

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main] which consists of a
     * `ConstraintLayout` root view holding a single [TextView]. We initialize our [Int] variable
     * `val bulletPointColor` by retrieving the color [R.color.colorAccent] which is `FF4081` -- a
     * pastel shade of red. We initialize our [Int] variable `val codeBackgroundColor` by retrieving
     * the color [R.color.code_background] which is `BBBBBB` -- a light gray.
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
     * markdown [String] with the resource ID [R.string.display_text] and use the `run` extension
     * function on that [CharSequence] to execute a lambda which finds the [TextView] with ID
     * [R.id.styledText] and sets the `text` of that [TextView] to `this` [CharSequence].
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // This is a simple markdown parser, where:
        // Paragraphs starting with “> ” are transformed into quotes. Quotes can't contain
        // other markdown elements
        // Text enclosed in “`” will be transformed into inline code block
        // Lines starting with “+ ” or “* ” will be transformed into bullet points. Bullet
        // points can contain nested markdown elements, like code.
        val bulletPointColor = getColorCompat(R.color.colorAccent)
        val codeBackgroundColor = getColorCompat(R.color.code_background)
        val codeBlockTypeface = getFontCompat(R.font.inconsolata)

        MarkdownBuilder(bulletPointColor, codeBackgroundColor, codeBlockTypeface, Parser)
            .markdownToSpans(getString(R.string.display_text))
            .run { findViewById<TextView>(R.id.styledText).text = this }
    }
}