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
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.SpannedString
import android.text.style.LeadingMarginSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.annotation.ColorInt
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
    @field:ColorInt @param:ColorInt private val bulletPointColor: Int,
    @field:ColorInt @param:ColorInt private val codeBackgroundColor: Int,
    private val codeBlockTypeface: Typeface?,
    private val parser: Parser
) {
    /**
     * Converts a [String] containing markdown formatted text into a [SpannedString] which can be
     * used as a rich text [CharSequence] for display in a `TextView`. We initialize our variable
     * `val markdown` to the [TextMarkdown] instance that our [Parser] field [parser] parses from
     * our [String] parameter [string]. It consists of a [List] of [Element] objects each of which
     * hold a substring of [string] and a [Element.Type] which describes what that substring should
     * be treated as given the markdown formatting which is relevant for it. Next we initialize our
     * [SpannableStringBuilder] variable `val builder` to a new instance, then loop over `i` for all
     * of the indices in the `elements` list of [Element] objects field of `markdown` calling our
     * method [buildSpans] with the `i`'th [Element] of the `elements` list of [Element] objects
     * field of `markdown` and `builder` to have it interpret the [Element.type] property of the
     * `i`'th [Element] and add the [Element.text] field with the appropriate rich text markup to
     * `builder`. When done with all the [Element]'s in `markdown` we return a [SpannedString] that
     * is constructed from `builder`.
     *
     * @param string the [String] we are to parse as markdown formatted text and convert into a
     * rich text [SpannedString].
     * @return the [SpannedString] which can be used as a rich text [CharSequence] for display in a
     * `TextView`.
     */
    fun markdownToSpans(string: String): SpannedString {
        val markdown: TextMarkdown = parser.parse(string)

        // In the SpannableStringBuilder, the text and the markup are mutable.
        val builder = SpannableStringBuilder()
        for (i in markdown.elements.indices) {
            buildSpans(markdown.elements[i], builder)
        }
        return SpannedString(builder)
    }

    /**
     * Build the spans for an element and inserts them in the builder. We branch on the [Element.type]
     * property of our [Element] parameter [element]:
     *  - [Element.Type.CODE_BLOCK] we call our [buildCodeBlockSpan] method with our parameters to
     *  have it apply a [CodeBlockSpan] to the [Element.text] string in [element] and add it to
     *  [builder].
     *  - [Element.Type.QUOTE] we call our [buildQuoteSpans] method with our parameters to have it
     *  apply rich text formatting for a quote block to the [Element.text] string in [element] and
     *  add it to [builder].
     *  - [Element.Type.BULLET_POINT] we call our [buildBulletPointSpans] method with our parameters
     *  to have it apply a [BulletPointSpan] to the child strings in [element] and add it to [builder].
     *  - [Element.Type.TEXT] we just call the [SpannableStringBuilder.append] method of [builder] to
     *  have it append the [Element.text] string in [element] to [builder] verbatim.
     *
     * @param element the element for which the spans are built
     * @param builder a [SpannableStringBuilder] that gathers all the spans
     */
    private fun buildSpans(element: Element, builder: SpannableStringBuilder) {
        // apply different spans depending on the type of the element
        when (element.type) {
            Element.Type.CODE_BLOCK -> buildCodeBlockSpan(element, builder)
            Element.Type.QUOTE -> buildQuoteSpans(element, builder)
            Element.Type.BULLET_POINT -> buildBulletPointSpans(element, builder)
            Element.Type.TEXT -> builder.append(element.text)
        }
    }

    /**
     * Applies the appropriate rich text formatting for an [Element.Type.BULLET_POINT] type of
     * [Element] and adds all of the children [Element]'s of the [Element] paramter [element] to
     * the [SpannableStringBuilder] parameter [builder]. We initialize our [Int] variable
     * `val startIndex` to the current length of [builder] then we loop over the `child` [Element]'s
     * in the [Element.elements] list of [Element]'s of [element] calling our method [buildSpans]
     * with `child` and our [SpannableStringBuilder] parameter [builder] to have it add the child
     * with rich text markup appropriate for the child to [builder]. Finally we call the method
     * [SpannableStringBuilder.setSpan] method of [builder] to have it mark the range of text from
     * `startIndex` to the new length of [builder] with a [BulletPointSpan] constructed with a gap
     * width of 20 and the color [bulletPointColor] using the flag [Spanned.SPAN_EXCLUSIVE_EXCLUSIVE]
     * (does not expand to include text inserted at either the starting or ending point, can never
     * have a length of 0 and is automatically removed from the buffer if all the text it covers
     * is removed).
     *
     * @param element the element for which the span is to be built
     * @param builder a [SpannableStringBuilder] that gathers all the spans
     */
    private fun buildBulletPointSpans(element: Element, builder: SpannableStringBuilder) {
        val startIndex = builder.length
        for (child in element.elements) {
            buildSpans(child, builder)
        }
        builder.setSpan(
            BulletPointSpan(20, bulletPointColor),
            startIndex,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * Adds the [Element.text] string of its [Element] parameter [element] to its [SpannableStringBuilder]
     * parameter [builder] and applies the appropriate rich text formatting for an [Element.Type.QUOTE]
     * type of [Element]. We initialize our [Int] variable `val startIndex` to the current length of
     * [builder]. Then we append the [Element.text] string of [element] to [builder]. We call the method
     * [SpannableStringBuilder.setSpan] method of [builder] to have it mark the range of text from
     * `startIndex` to the new length of [builder] with a [StyleSpan] for [Typeface.ITALIC], then call
     * the method [SpannableStringBuilder.setSpan] method of [builder] to have it mark the range of
     * text from `startIndex` to the new length of [builder] with a [LeadingMarginSpan.Standard] for
     * an indent of 40, and finally call the method [SpannableStringBuilder.setSpan] method of [builder]
     * to have it mark the range of text from `startIndex` to the new length of [builder] with a
     * [RelativeSizeSpan] to scale the text by 1.1f. All of these spans use the flag
     * [Spanned.SPAN_EXCLUSIVE_EXCLUSIVE] (does not expand to include text inserted at either the
     * starting or ending point, can never have a length of 0 and is automatically removed from the
     * buffer if all the text it covers is removed).
     *
     * @param element the element for which the span is to be built
     * @param builder a [SpannableStringBuilder] that gathers all the spans
     */
    private fun buildQuoteSpans(element: Element, builder: SpannableStringBuilder) {
        val startIndex = builder.length
        builder.append(element.text)
        // You can set multiple spans for the same text
        builder.setSpan(
            StyleSpan(Typeface.ITALIC),
            startIndex,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            LeadingMarginSpan.Standard(40),
            startIndex,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            RelativeSizeSpan(1.1f),
            startIndex,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    /**
     * Adds the [Element.text] string of its [Element] parameter [element] to its [SpannableStringBuilder]
     * parameter [builder] and applies the appropriate rich text formatting for an [Element.Type.CODE_BLOCK]
     * type of [Element]. We initialize our [Int] variable `val startIndex` to the current length of
     * [builder]. Then we append the [Element.text] string of [element] to [builder]. We call the method
     * [SpannableStringBuilder.setSpan] method of [builder] to have it mark the range of text from
     * `startIndex` to the new length of [builder] with a [CodeBlockSpan] constructed to use our
     * [Typeface] field [codeBlockTypeface] as its typeface and the color [codeBackgroundColor] as
     * the background color using the flag [Spanned.SPAN_EXCLUSIVE_EXCLUSIVE] (does not expand to
     * include text inserted at either the starting or ending point, can never have a length of 0
     * and is automatically removed from the buffer if all the text it covers is removed).
     *
     * @param element the element for which the span is to be built
     * @param builder a [SpannableStringBuilder] that gathers all the spans
     */
    private fun buildCodeBlockSpan(element: Element, builder: SpannableStringBuilder) {
        val startIndex = builder.length
        builder.append(element.text)
        builder.setSpan(
            CodeBlockSpan(codeBlockTypeface!!, codeBackgroundColor),
            startIndex,
            builder.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}