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
    fun markdownToSpans(string: String): SpannedString {
        val markdown = parser.parse(string)

        // In the SpannableStringBuilder, the text and the markup are mutable.
        val builder = SpannableStringBuilder()
        for (i in markdown.elements.indices) {
            buildSpans(markdown.elements[i], builder)
        }
        return SpannedString(builder)
    }

    /**
     * Build the spans for an element and insert them in the builder
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