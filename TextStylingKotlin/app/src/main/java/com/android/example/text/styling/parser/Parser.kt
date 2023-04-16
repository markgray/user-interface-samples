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

import java.util.Collections.emptyList
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * The role of this parser is just to showcase ways of working with text. It should not be
 * expected to support complex markdown elements.
 *
 * Parse text and extract markdown elements:
 *
 *  * Paragraphs starting with “> ” are transformed into quotes. Quotes can't contain
 *  other markdown elements.
 *  *  Text enclosed in “`” will be transformed into inline code block.
 *  * Lines starting with “+ ” or “* ” will be transformed into bullet points. Bullet
 *  points can contain nested markdown elements, like code.
 *
 */
object Parser {

    /**
     * Parse a text and extract a [TextMarkdown] which holds a list of [Element] objects, each of
     * which holds a substring of our [String] parameter [string] along with the type of markdown
     * element it is to be treated as. First we initialize our [MutableList] of [Element]s variable
     * `val parents` to a new instance.
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
     *  - If `lastStartIndex` is less than `startIndex` there are elements before the quote block so
     *  we initialize our [String] variable `val text` to the substring from `lastStartIndex` to
     *  right before `startIndex` and add all the [Element]s that our [findElements] method finds in
     *  `text` using `pattern` as its [Pattern] to `parents`.
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
     * last quote found by seeing if `lastStartIndex` is less than the length of [string], and if it
     * is we initialize our [String] variable `val text` to the substring of [string] from
     * `lastStartIndex` to right before the length of [string], call our method [findElements] with
     * `text` as its [String] and `pattern` as its [Pattern] then add the list of [Element]s it
     * extracts from `text` based on `pattern` to `parents`.
     *
     * Having finished parsing [string] into the list of [Element]s `parents` we return a [TextMarkdown]
     * instance constructed from `parents` to the caller.
     *
     * @param string string to be parsed into markdown elements
     * @return the [TextMarkdown]
     */
    fun parse(string: String): TextMarkdown {
        val parents = mutableListOf<Element>()

        val patternQuote = Pattern.compile(QUOTE_REGEX)
        val pattern = Pattern.compile(BULLET_POINT_CODE_BLOCK_REGEX)

        val matcher: Matcher = patternQuote.matcher(string)
        var lastStartIndex = 0

        while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            // we found a quote
            if (lastStartIndex < startIndex) {
                // check what was before the quote
                val text = string.subSequence(lastStartIndex, startIndex)
                parents.addAll(findElements(text, pattern))
            }
            // a quote can only be a paragraph long, so look for end of line
            val endOfQuote = getEndOfParagraph(string, endIndex)
            lastStartIndex = endOfQuote
            val quotedText = string.subSequence(endIndex, endOfQuote)
            parents.add(Element(Element.Type.QUOTE, quotedText, emptyList()))
        }

        // check if there are any other element after the quote
        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.addAll(findElements(text, pattern))
        }

        return TextMarkdown(parents)
    }

    /**
     * Used to find the end of a line in its [String] parameter [string] starting at the index
     * [endIndex]. We initialize our [Int] variable `var endOfParagraph` to the index within our
     * [String] parameter [string] of the first occurrence of the [LINE_SEPARATOR] string, starting
     * from our [Int] parameter [endIndex]. If `endOfParagraph` is equal to -1 (no [LINE_SEPARATOR]
     * string was found) we set `endOfParagraph` to the length of [string] (if we don't have an
     * end of line the quote is the last element in the text so we can consider that the end of
     * the quote is the end of the text). Otherwise we add the length of [LINE_SEPARATOR] to
     * `endOfParagraph` to add the new line as part of the element.
     *
     * Finally we return `endOfParagraph` to the caller.
     *
     * @param string the [String] we are to search for an end of line.
     * @param endIndex the index into [string] at which to start searching.
     * @return the index of the character in [string] after the line separator we find, or the
     * length of [string] if none is found.
     */
    private fun getEndOfParagraph(string: CharSequence, endIndex: Int): Int {
        var endOfParagraph = string.indexOf(LINE_SEPARATOR, endIndex)
        if (endOfParagraph == -1) {
            // we don't have an end of line, so the quote is the last element in the text
            // so we can consider that the end of the quote is the end of the text
            endOfParagraph = string.length
        } else {
            // add the line separator as part of the element
            endOfParagraph += LINE_SEPARATOR.length
        }
        return endOfParagraph
    }

    /**
     * Returns a [List] of all the [Element]s it finds in its [String] parameter [string] using
     * its [Pattern] parameter [pattern] as the [Pattern] for the [Matcher] it uses to search
     * for an [Element] in [string].
     *
     * We initialize our [MutableList] of [Element] variable `val parents` to a new instance,
     * initialize our [Matcher] variable `val matcher` to a matcher that will match [string]
     * against [pattern], and initialize our [Int] variable `var lastStartIndex` to 0.
     *
     * Then we loop while a subsequence of [string] starting at the index `lastStartIndex` matches
     * the pattern of `matcher`:
     *  - We initialize our [Int] variable `val startIndex` to the start index of the match just
     *  made by `matcher` and our [Int] variable `val endIndex` to the offset after the last
     *  character matched.
     *  - We initialize our [String] variable `val mark` to the substring of [string] from
     *  `startIndex` to just before `endIndex` (this is the substring that matched [pattern])
     *  - If `lastStartIndex` is less than `startIndex` there is unprocessed text before our
     *  match so we add the [List] of [Element]s that a recursive call to [findElements] returns
     *  for the substring of [string] from `lastStartIndex` to just before `startIndex` to
     *  `parents` before proceeding to process the [Element] that `matcher` found.
     *  - Next we declare our [CharSequence] variable `var text` and branch on the contents of `mark`:
     *      * [BULLET_PLUS] or [BULLET_STAR] we initialize our [Int] variable `val endOfBulletPoint`
     *      to the value that our [getEndOfParagraph] returns when it searches [string] for a
     *      line separator, and set `text` to the substring of [string] from `endIndex` to just
     *      before `endOfBulletPoint` and set `lastStartIndex` to `endOfBulletPoint`. We initialize
     *      our [List] of [Element]s variable `val subMarks` to the [Element]s a recursive call to
     *      [findElements] finds in `text` and then initialize our [Element] variable `val bulletPoint`
     *      to a new instance of type [Element.Type.BULLET_POINT] holding `text` and `subMarks`.
     *      And then we add `bulletPoint` to `parents`.
     *      * [CODE_BLOCK] a code block is set between two backquotes so first we look for the
     *      other one and if another backquote is not found, then this is not a code block so
     *      we set `markEnd` to the length of [string], set `text` to the substring of [string]
     *      from `startIndex` to just before `markEnd`, and add to `parents` an [Element] of type
     *      [Element.Type.TEXT] constructed from `text` and an [emptyList] for its `elements`
     *      list of sub-elements. But if we do find another backquote we set `text` to the
     *      substring of [string] from `endIndex` to just before `markEnd`, and add to `parents`
     *      an [Element] of type [Element.Type.CODE_BLOCK] constructed from `text` and an
     *      [emptyList] for its `elements` list of sub-elements -- then we set `lastStartIndex`
     *      to `markEnd` plus 1 so we can ignore the ending backquote for the code block.
     *  - We then loop around to handle the next match of `matcher`.
     *
     * When done processing all of the matches of `matcher` we check if there's any more text
     * left (ie. `lastStartIndex` is less than the length of [string]) and if there is we
     * initialize `val text` to the substring of [string] from `lastStartIndex` to just before
     * the length of [string], and add an [Element] of type [Element.Type.TEXT] constructed from
     * `text` and an [emptyList] for its `elements` list of sub-elements to `parents`.
     *
     * Finally we return `parents` to the caller.
     *
     * @param string the [String] to search for [Element]s in.
     * @param pattern the [Pattern] to use in a [Matcher] that will find [Element]s in [string].
     * @return a [List] of all of the [Element]s found in [string].
     */
    private fun findElements(string: CharSequence, pattern: Pattern): List<Element> {
        val parents = mutableListOf<Element>()
        val matcher: Matcher = pattern.matcher(string)
        var lastStartIndex = 0

        while (matcher.find(lastStartIndex)) {
            val startIndex = matcher.start()
            val endIndex = matcher.end()
            // we found a mark
            val mark = string.subSequence(startIndex, endIndex)
            if (lastStartIndex < startIndex) {
                // check what was before the mark
                parents.addAll(findElements(string.subSequence(lastStartIndex, startIndex), pattern))
            }
            val text: CharSequence
            // check what kind of mark this was
            when (mark) {
                BULLET_PLUS, BULLET_STAR -> {
                    // every bullet point is max until a new line or end of text
                    val endOfBulletPoint = getEndOfParagraph(string, endIndex)
                    text = string.subSequence(endIndex, endOfBulletPoint)
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
                        parents.add(Element(Element.Type.CODE_BLOCK, text,
                            kotlin.collections.emptyList()))
                        // adding 1 so we can ignore the ending "`" for the code block
                        lastStartIndex = markEnd + 1
                    }
                }
            }
        }

        // check if there's any more text left
        if (lastStartIndex < string.length) {
            val text = string.subSequence(lastStartIndex, string.length)
            parents.add(Element(Element.Type.TEXT, text, emptyList()))
        }

        return parents
    }

    /**
     * The character "+" followed by a space character which is used to mark a bullet point.
     * This is used in our [findElements] method when the [Matcher] finds a match and the method
     * must decide whether it is a bullet point or a code block.
     */
    private const val BULLET_PLUS = "+ "

    /**
     * The character "*" followed by a space character which is used to mark a bullet point.
     * This is used in our [findElements] method when the [Matcher] finds a match and the method
     * must decide whether it is a bullet point or a code block.
     */
    private const val BULLET_STAR = "* "

    /**
     * [String] which is compiled into a [Pattern] for matching a block quote. Multiline mode is
     * enabled via the embedded flag expression `(?m)` (in multiline mode the expressions "^" and
     * "$" match just after or just before, respectively, a line terminator or the end of the
     * input sequence) and a ">" character followed by a space at the beginning of the line is
     * used to indicate that the text is a block quote.
     */
    private const val QUOTE_REGEX = "(?m)^> "

    /**
     * [String] which when compiled as part of a [Pattern] will match a bullet point which begins
     * with a "*" character. Multiline mode is enabled via the embedded flag expression `(?m)`
     * (in multiline mode the expressions "^" and "$" match just after or just before, respectively,
     * a line terminator or the end of the input sequence) and a "*" character followed by a space
     * at the beginning of the line is used to indicate that the text is a bullet point. It is
     * used as part of the regular expression string [BULLET_POINT_REGEX] which in turn is used
     * as part of the regular expression string [BULLET_POINT_CODE_BLOCK_REGEX].
     */
    private const val BULLET_POINT_STAR = "(?m)^\\$BULLET_STAR"

    /**
     * [String] which when compiled as part of a [Pattern] will match a bullet point which begins
     * with a "+" character. Multiline mode is enabled via the embedded flag expression `(?m)`
     * (in multiline mode the expressions "^" and "$" match just after or just before, respectively,
     * a line terminator or the end of the input sequence) and a "*" character followed by a space
     * at the beginning of the line is used to indicate that the text is a bullet point. It is
     * used as part of the regular expression string [BULLET_POINT_REGEX] which in turn is used
     * as part of the regular expression string [BULLET_POINT_CODE_BLOCK_REGEX].
     */
    private const val BULLET_POINT_PLUS = "(?m)^\\$BULLET_PLUS"

    /**
     * Regular expression string which when compiled as part of a [Pattern] will match either
     * the regular expression string [BULLET_POINT_STAR] or the regular expression string
     * [BULLET_POINT_STAR].
     */
    private const val BULLET_POINT_REGEX = "($BULLET_POINT_STAR|$BULLET_POINT_PLUS)"

    /**
     * Regular expression string which when compiled as part of a [Pattern] will match a back
     * quote character which denotes a code block.
     */
    private const val CODE_BLOCK = "`"

    /**
     * Regular expression string which when compiled as part of a [Pattern] will match either
     * the regular expression string [BULLET_POINT_REGEX] or the regular expression string
     * [CODE_BLOCK]. The [Pattern] compiled from it is used as the [Pattern] in all or our calls
     * to the [findElements] method.
     */
    private const val BULLET_POINT_CODE_BLOCK_REGEX = "($BULLET_POINT_REGEX|$CODE_BLOCK)"

    /**
     * The line separator used on this device. Used in our [getEndOfParagraph] method to search
     * for a line separator in the [String] passed it, and to advance over the line separator if
     * one is found (the new line is part of the element).
     */
    private val LINE_SEPARATOR = System.getProperty("line.separator")!!
}
