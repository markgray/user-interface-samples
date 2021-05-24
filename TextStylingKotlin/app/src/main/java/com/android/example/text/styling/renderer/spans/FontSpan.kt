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
import android.text.style.MetricAffectingSpan

/**
 * Span that changes the typeface of the text used to the one provided. The style set before will
 * be kept.
 *
 * @param font the [Typeface] to use for the text in our span.
 */
open class FontSpan(private val font: Typeface?) : MetricAffectingSpan() {

    /**
     * Classes that extend [MetricAffectingSpan] implement this method to update the text formatting
     * in a way that can change the width or height of characters. We just call our [update] method
     * to have it install our [Typeface] field [font] into our [TextPaint] parameter [textPaint].
     *
     * @param textPaint the paint used for drawing the text
     */
    override fun updateMeasureState(textPaint: TextPaint) = update(textPaint)

    /**
     * Updates the draw state by calling our [update] method to have it install our [Typeface] field
     * [font] into our [TextPaint] parameter [textPaint].
     *
     * @param textPaint the [TextPaint] that will be used to draw the text of our span.
     */
    override fun updateDrawState(textPaint: TextPaint) = update(textPaint)

    /**
     * Installs our [Typeface] field [font] into our [TextPaint] parameter [textPaint] while keeping
     * the style of the old [Typeface] of [textPaint] the same. We use the [TextPaint.apply] extension
     * function of our [TextPaint] parameter [textPaint] to execute a lambda on it which initializes
     * its [Typeface] variable `val old` to the old [Typeface] of [textPaint]. If this is not `null`
     * it initializes its [Int] variable `val oldStyle` to the style of `old` or to 0 if it is `null`.
     * Next it initializes its [Typeface] variable `val fontOldStyle` to a [Typeface] object that
     * best matches our [Typeface] field [font] and the Style `oldStyle` and then sets the typeface
     * of [textPaint] to `fontOldStyle`.
     *
     * @param textPaint the [TextPaint] that will be used to draw the text of our span.
     */
    private fun update(textPaint: TextPaint) {
        textPaint.apply {
            val old = typeface
            val oldStyle = old?.style ?: 0

            // keep the style set before
            val fontOldStyle = Typeface.create(font, oldStyle)
            typeface = fontOldStyle
        }
    }
}