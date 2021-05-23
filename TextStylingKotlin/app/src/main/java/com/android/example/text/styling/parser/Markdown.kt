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

/**
 * Simple markdown parsing of text.
 * Contains a list of markdown [Element]s
 *
 * @param elements the [List] of [Element]s we hold.
 */
data class TextMarkdown(val elements: List<Element>)

/**
 * Markdown like type of element.
 *
 * @param type the [Type] of the [Element], one of [Type.TEXT], [Type.QUOTE], [Type.BULLET_POINT],
 * or [Type.CODE_BLOCK]
 * @param text the text that this [Element] marks up.
 * @param elements a list of sub-elements found inside this [Element].

 */
data class Element(
    val type: Type,
    val text: CharSequence,
    val elements: List<Element> = emptyList()
) {
    enum class Type {
        /**
         * A plain text only [Element].
         */
        TEXT,
        /**
         * A Blockquote [Element]
         */
        QUOTE,
        /**
         * The [Element] is a bullet in an unordered list.
         */
        BULLET_POINT,
        /**
         * The [Element] is a code block.
         */
        CODE_BLOCK
    }
}
