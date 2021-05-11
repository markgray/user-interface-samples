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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
     * `ConstraintLayout` root view holding a single [TextView] and we initialize our [TextView]
     * variable `val textView` by finding that view whose ID is [R.id.styled_text]. We initialize
     * our [Int] variable `val bulletPointColor` by retrieving the color [R.color.colorAccent] which
     * is `FF4081` -- a pastel shade of red. We initialize our [Int] variable `val codeBackgroundColor`
     * by retrieving the color [R.color.code_background] which is `BBBBBB` -- a light gray.
     *
     * We then initialize our [Typeface] variable `val codeBlockTypeface` by using the `font-family`
     * which is defined in the font/inconsolata.xml file to download the "Inconsolata" font from the
     * font provider "com.google.android.gms.fonts".
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        val text: CharSequence = MarkdownBuilder(bulletPointColor, codeBackgroundColor,
            codeBlockTypeface, Parser())
            .markdownToSpans(getString(R.string.display_text))
        textView.text = text
    }
}