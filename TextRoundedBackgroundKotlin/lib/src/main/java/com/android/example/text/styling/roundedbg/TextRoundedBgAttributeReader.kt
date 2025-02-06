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
package com.android.example.text.styling.roundedbg

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.core.content.res.getDrawableOrThrow

/**
 * Reads default attributes that [TextRoundedBgHelper] needs from resources. The attributes read
 * are:
 *
 * - roundedTextHorizontalPadding: the padding to be applied to left & right of the background
 * - roundedTextVerticalPadding: the padding to be applied to top & bottom of the background
 * - roundedTextDrawable: the drawable used to draw the background
 * - roundedTextDrawableLeft: the drawable used to draw left edge of the background
 * - roundedTextDrawableMid: the drawable used to draw for whole line
 * - roundedTextDrawableRight: the drawable used to draw right edge of the background
 */
class TextRoundedBgAttributeReader(context: Context, attrs: AttributeSet?) {

    /**
     * The value of the `roundedTextHorizontalPadding` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is 2dp. It is used in constructing a [TextRoundedBgHelper] as its
     * `horizontalPadding` property.
     */
    val horizontalPadding: Int

    /**
     * The value of the `roundedTextVerticalPadding` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is 2dp. It is used in constructing a [TextRoundedBgHelper] as its
     * `verticalPadding` property.
     */
    val verticalPadding: Int

    /**
     * The value of the `roundedTextDrawable` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is `R.drawable.rounded_text_bg`. It is used in constructing a
     * [TextRoundedBgHelper] as its `drawable` property.
     */
    val drawable: Drawable

    /**
     * The value of the `roundedTextDrawableLeft` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is `R.drawable.rounded_text_bg_left`. It is used in constructing
     * a [TextRoundedBgHelper] as its `drawableLeft` property.
     */
    val drawableLeft: Drawable

    /**
     * The value of the `roundedTextDrawableMid` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is `R.drawable.rounded_text_bg_mid`. It is used in constructing
     * a [TextRoundedBgHelper] as its `drawableMid` property.
     */
    val drawableMid: Drawable

    /**
     * The value of the `roundedTextDrawableRight` attribute used, the default in the style resource
     * `R.style.RoundedBgTextView` is `R.drawable.rounded_text_bg_right`. It is used in constructing
     * a [TextRoundedBgHelper] as its `drawableRight` property.
     */
    val drawableRight: Drawable

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.TextRoundedBgHelper,
            0,
            R.style.RoundedBgTextView
        )
        horizontalPadding = typedArray.getDimensionPixelSize(
            R.styleable.TextRoundedBgHelper_roundedTextHorizontalPadding,
            0
        )
        verticalPadding = typedArray.getDimensionPixelSize(
            R.styleable.TextRoundedBgHelper_roundedTextVerticalPadding,
            0
        )
        drawable = typedArray.getDrawableOrThrow(
            R.styleable.TextRoundedBgHelper_roundedTextDrawable
        )
        drawableLeft = typedArray.getDrawableOrThrow(
            R.styleable.TextRoundedBgHelper_roundedTextDrawableLeft
        )
        drawableMid = typedArray.getDrawableOrThrow(
            R.styleable.TextRoundedBgHelper_roundedTextDrawableMid
        )
        drawableRight = typedArray.getDrawableOrThrow(
            R.styleable.TextRoundedBgHelper_roundedTextDrawableRight
        )
        typedArray.recycle()
    }
}
