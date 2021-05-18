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
package com.android.example.text.styling.parser

import java.util.ArrayList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The role of this parser is just to showcase ways of working with text. It should not be
 * expected to support complex markdown elements.
 *
 * Parse a text and extract markdown elements:
 *
 *  * Paragraphs starting with “> ” are transformed into quotes. Quotes can't contain
 *  other markdown elements
 *  *  Text enclosed in matching backticks`` ` `` will be transformed into inline code block
 *  * Lines starting with “+ ” or “* ” will be transformed into bullet points. Bullet
 *  points can contain nested markdown elements, like code.
 */
class Parser {
    /**
     * Parse a text and extract a [TextMarkdown] which holds a list of [Element] objects, each of
     * which holds a substring of our [String] parameter [string] along with the type of markdown
     * element it is to be treated as. First we initialize our [MutableList] of [Element]s variable
     * `val parents` to a new instance of [ArrayList].
     *
     * We initialize our [Pattern] variable `val quotePattern` to the results of using the
     * [Pattern.compile] method to compile our [String] constant [QUOTE_REGEX] (this consists
     * of am embedded flag expression (?m) which turns on multi-line mode followed by a ">"
     * character occurring at the beginning of the line).
     *
     * We initialize our [Pattern] variable `val pattern` to the results of using the [Pattern.compile]
     * method to compile our [String] constant [BULLET_POINT_CODE_BLOCK_REGEX] (which matches either
     * our [String] constant [BULLET_POINT_REGEX] or our our [String] constant [CODE_BLOCK]).
     *
     * We initialize our [Matcher] variable `val matcher` to a matcher that will match our [String]
     * parameter [string] against the `quotePattern` pattern, and initialize our [Int] variable
     * `var lastStartIndex` to 0.
     *
     * Then we proceed to loop while a subsequence of the input sequence starting at the index
     * `lastStartIndex` matches the pattern of `matcher`:
     *  - We initialize our [Int] variable `val startIndex` to the start index of the match just made.
     *  - We initialize our [Int] variable `val endIndex` to the index after the last character
     *  matched of the match just made.
     *  - If `lastStartIndex` is less than `startIndex` there are element before the quote block so
     *  we initialize our [String] variable `val text` to the substring from `lastStartIndex` to
     *  right before `startIndex` and add all our the [Element]s that our [findElements] method
     *  finds in `text` using `pattern` as its [Pattern] to `parents`.
     *  - Since a quote can only be a paragraph long, we next look for end of line by calling our
     *  method [getEndOfParagraph] with [string] and `endIndex` as the index in [string] to start
     *  looking for an end of line and use the [Int] it returns to initialize our `val endOfQuote`
     *  variable.
     *  - We set `lastStartIndex` to `endOfQuote` and initialize our [String] variable `val quotedText`
     *  to the substring from `endIndex` to right before `endOfQuote`, then we add an [Element] of
     *  type [Element.Type.QUOTE] containing `quotedText` as its text and an empty list as its
     *  list of [Element] field `elements`.
     *  - And now we loop around to look for the next quote in [string].
     *
     * Having found and processed all the quotes we check if there are any other elements after the
     * last quote found
     *
     * @param string string to be parsed into markdown elements
     * @return the [TextMarkdown]
     */
    fun parse(string: String): TextMarkdown {
        val parents: MutableList<Element> = ArrayList()
        val quotePattern: Pattern = Pattern.compile(QUOTE_REGEX)
        val pattern: Pattern = Pattern.compile(BULLET_POINT_CODE_BLOCK_REGEX)
        val matcher: Matcher = quotePattern.matcher(string)
        var lastStartIndex = 0
        while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            // we found a quote
            if (lastStartIndex < startIndex) {
                // check what was before the quote
                val text = string.substring(lastStartIndex, startIndex)
                parents.addAll(findElements(text, pattern))
            }
            // a quote can only be a paragraph long, so look for end of line
            val endOfQuote = getEndOfParagraph(string, endIndex)
            lastStartIndex = endOfQuote
            val quotedText = string.substring(endIndex, endOfQuote)
            parents.add(Element(Element.Type.QUOTE, quotedText, emptyList()))
        }
        // check if there are any other element after the quote
        if (lastStartIndex < string.length) {
            val text = string.substring(lastStartIndex, string.length)
            parents.addAll(findElements(text, pattern))
        }
        return TextMarkdown(parents)
    }

    @Suppress("ConvertToStringTemplate")
    companion object {
        private const val BULLET_PLUS = "+ "
        private const val BULLET_STAR = "* "

        /**
         * Multiline mode is enabled via the embedded flag expression (?m)
         */
        private const val QUOTE_REGEX = "(?m)^> "
        private const val BULLET_POINT_STAR = "(?m)^\\* "
        private const val BULLET_POINT_PLUS = "(?m)^\\+ "
        private const val BULLET_POINT_REGEX = "(" + BULLET_POINT_STAR + "|" + BULLET_POINT_PLUS + ")"
        private const val CODE_BLOCK = "`"
        private const val BULLET_POINT_CODE_BLOCK_REGEX = "(" + BULLET_POINT_REGEX + "|" + CODE_BLOCK + ")"
        private val LINE_SEPARATOR = System.getProperty("line.separator")
        private fun getEndOfParagraph(string: String, endIndex: Int): Int {
            var endOfParagraph = string.indexOf(LINE_SEPARATOR, endIndex)
            if (endOfParagraph == -1) {
                // we don't have an end of line, so the quote is the last element in the text
                // so we can consider that the end of the quote is the end of the text
                endOfParagraph = string.length
            } else {
                // add the new line as part of the element
                endOfParagraph += LINE_SEPARATOR!!.length
            }
            return endOfParagraph
        }

        private fun findElements(string: String, pattern: Pattern): List<Element> {
            val parents: MutableList<Element> = ArrayList()
            val matcher = pattern.matcher(string)
            var lastStartIndex = 0
            while (matcher.find(lastStartIndex)) {
                val startIndex = matcher.start()
                val endIndex = matcher.end()
                // we found a mark
                val mark = string.substring(startIndex, endIndex)
                if (lastStartIndex < startIndex) {
                    // check what was before the mark
                    parents.addAll(findElements(string.substring(lastStartIndex, startIndex), pattern))
                }
                var text: String
                when (mark) {
                    BULLET_PLUS, BULLET_STAR -> {
                        // every bullet point is max until a new line or end of text
                        val endOfBulletPoint = getEndOfParagraph(string, endIndex)
                        text = string.substring(endIndex, endOfBulletPoint)
                        lastStartIndex = endOfBulletPoint
                        // also see what else we have in the text
                        val subMarks = findElements(text, pattern)
                        val bulletPoint = Element(Element.Type.BULLET_POINT, text, subMarks)
                        parents.add(bulletPoint)
                    }
                    CODE_BLOCK -> {
                        // a code block is set between two "`" so look for the other one
                        // if another "`" is not found, then this is not a code block
                        var markEnd = string.indexOf(CODE_BLOCK, endIndex)
                        if (markEnd == -1) {
                            // we don't have an end of code block so this is just text
                            markEnd = string.length
                            text = string.substring(startIndex, markEnd)
                            parents.add(Element(Element.Type.TEXT, text, emptyList()))
                            lastStartIndex = markEnd
                        } else {
                            // we found the end of the code block
                            text = string.substring(endIndex, markEnd)
                            parents.add(Element(Element.Type.CODE_BLOCK, text, emptyList()))
                            // adding 1 so we can ignore the ending "`" for the code block
                            lastStartIndex = markEnd + 1
                        }
                    }
                }
            }
            // check if there's any more text left
            if (lastStartIndex < string.length) {
                val text = string.substring(lastStartIndex, string.length)
                parents.add(Element(Element.Type.TEXT, text, emptyList()))
            }
            return parents
        }
    }
}