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
package com.android.example.text.styling.renderer.spans

import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.BackgroundColorSpan
import androidx.annotation.ColorInt

/**
 * To draw a code block, we set a font for the text and a background color. The same effect can be
 * achieved if on a text block, we set two spans: [FontSpan] and [BackgroundColorSpan]
 *
 * @param font the [Typeface] to use for a code block.
 * @param backgroundColor the color to use for the background color of a code block.
 */
class CodeBlockSpan(
    font: Typeface,
    @field:ColorInt @param:ColorInt private val backgroundColor: Int
) : FontSpan(font) {
    /**
     * Updates the draw state by changing the background color of its [TextPaint] paramter [textPaint]
     * to our property [backgroundColor]. Since this will not affect the measure state, we can just
     * override [updateDrawState]. First we call our super's implementation of `updateDrawState` to
     * have it set the [Typeface] of [textPaint] to our field [font]. Then we set the background color
     * of [textPaint] to our [backgroundColor] property.
     *
     * @param textPaint the [TextPaint] that will be used to draw the text of our span.
     */
    override fun updateDrawState(textPaint: TextPaint) {
        super.updateDrawState(textPaint)
        textPaint.bgColor = backgroundColor
    }
}