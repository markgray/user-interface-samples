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
package com.android.example.text.styling.renderer

import android.graphics.Typeface
import androidx.annotation.ColorInt
import android.text.SpannableStringBuilder
import android.text.SpannedString
import android.text.style.LeadingMarginSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import com.android.example.text.styling.parser.Element
import com.android.example.text.styling.parser.Parser
import com.android.example.text.styling.parser.TextMarkdown
import com.android.example.text.styling.renderer.spans.BulletPointSpan
import com.android.example.text.styling.renderer.spans.CodeBlockSpan

/**
 * Renders the text as simple markdown, using spans.
 *
 * @param bulletPointColor the color to use for bullet points.
 * @param codeBackgroundColor the color to use for the background of code blocks.
 * @param codeBlockTypeface the [Typeface] to use for code blocks
 * @param parser the [Parser] to use to parse markdown text.
 */
class MarkdownBuilder(
    @ColorInt private val bulletPointColor: Int,
    @ColorInt private val codeBackgroundColor: Int,
    private val codeBlockTypeface: Typeface?,
    private val parser: Parser
) {

    /**
     * Converts a [String] containing markdown formatted text into a [SpannedString] which can be
     * used as a rich text [CharSequence] for display in a `TextView`. We initialize our variable
     * `val markdown` to the [TextMarkdown] instance that our [Parser] field [parser] parses from
     * our [String] parameter [string]. It consists of a [List] of [Element] objects each of which
     * holds a substring of [string] and a [Element.Type] which describes what that substring should
     * be treated as given the markdown formatting which is relevant for it. We then return the
     * [SpannedString] returned by the [buildSpannedString] method when it uses our supplied lambda
     * to populate a newly created [SpannableStringBuilder] receiver which it then converts to a
     * [SpannedString]. In our lambda we iterate over all of the [Element]s in the `elements` list
     * of `markdown` calling our [buildElement] method with each element using `this` as the
     * [SpannableStringBuilder] that it adds spans to for each [Element].
     *
     * @param string the [String] we are to parse as markdown formatted text and convert into a
     * rich text [SpannedString].
     * @return the [SpannedString] which can be used as a rich text [CharSequence] for display in a
     * `TextView`.
     */
    fun markdownToSpans(string: String): SpannedString {
        val markdown: TextMarkdown = parser.parse(string)

        return buildSpannedString {
            markdown.elements.forEach {
                buildElement(it, this)
            }
        }
    }

    /**
     * Interprets the [Element.Type] of its [Element] parameter [element] and adds a span to its
     * [SpannableStringBuilder] parameter [builder] whose type depends on the type of the element.
     * We return the value returned when we call the [SpannableStringBuilder.apply] extension function
     * of our [SpannableStringBuilder] parameter [builder] when it applies our lambda to that
     * [SpannableStringBuilder]. Our lambda branches on the `type` property of our [Element] parameter
     * [element]:
     *  - [Element.Type.CODE_BLOCK] we call the [SpannableStringBuilder.inSpans] method of `this`
     *  [SpannableStringBuilder] to add a [CodeBlockSpan] that uses our [codeBlockTypeface] field as
     *  its [Typeface] and our [codeBackgroundColor] field as its background color then append the
     *  [Element.text] field of [element] to `this` [SpannableStringBuilder].
     *  - [Element.Type.QUOTE] we call the [SpannableStringBuilder.inSpans] method of `this`
     *  [SpannableStringBuilder] to add a [StyleSpan] of type [Typeface.ITALIC], an [LeadingMarginSpan]
     *  for all lines of 40, and a [RelativeSizeSpan] of 1.1f then append the [Element.text] field of
     *  [element] to `this` [SpannableStringBuilder].
     *  - [Element.Type.BULLET_POINT] we call the [SpannableStringBuilder.inSpans] method of `this`
     *  [SpannableStringBuilder] to add a [BulletPointSpan] with a gap width of 40 that uses our
     *  field [bulletPointColor] as the color of the bullet point then for all of the `child`
     *  [Element]s in the [Element.elements] field of [element] we make a recursive call to
     *  [buildElement] with that `child` and our [SpannableStringBuilder] parameter [builder] to
     *  add spans for those [Element]s.
     *  - [Element.Type.TEXT] - we just call the [SpannableStringBuilder.append] method of `this`
     *  [SpannableStringBuilder] to have it append the [Element.text] field of [element] to itself.
     *
     * @param element the [Element] whose text we are to wrap in a span that is appropriate for the
     * [Element.Type] of the [Element].
     * @param builder the [SpannableStringBuilder] we are to add our spans to.
     * @return our [SpannableStringBuilder] parameter [builder] to allow chaining.
     */
    private fun buildElement(
        element: Element,
        builder: SpannableStringBuilder
    ): SpannableStringBuilder {
        return builder.apply {
            // apply different spans depending on the type of the element
            when (element.type) {
                Element.Type.CODE_BLOCK -> {
                    inSpans(CodeBlockSpan(codeBlockTypeface, codeBackgroundColor)) {
                        append(element.text)
                    }
                }
                Element.Type.QUOTE -> {
                    // You can set multiple spans for the same text
                    inSpans(StyleSpan(Typeface.ITALIC),
                        LeadingMarginSpan.Standard(40),
                        RelativeSizeSpan(1.1f)) {
                        append(element.text)
                    }
                }
                Element.Type.BULLET_POINT -> {
                    inSpans(BulletPointSpan(20, bulletPointColor)) {
                        for (child in element.elements) {
                            buildElement(child, builder)
                        }
                    }
                }
                Element.Type.TEXT -> append(element.text)
            }
        }
    }

}
