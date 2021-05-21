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

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.text.Layout
import android.text.Spanned
import android.text.style.BulletSpan
import android.text.style.LeadingMarginSpan
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting

/**
 * Creating a bullet span with bigger bullets than [BulletSpan] and with a left margin
 */
class BulletPointSpan(
    @field:Px @param:Px private val gapWidth: Int,
    @field:ColorInt @param:ColorInt private val color: Int
    ) : LeadingMarginSpan {
    /**
     * Returns the amount by which to adjust the leading margin. Positive values move away from the
     * leading edge of the paragraph, negative values move towards it. We just return 2 times our
     * constant [BULLET_RADIUS] plus 2 times our property [gapWidth].
     *
     * @param first `true` if the request is for the first line of a paragraph, `false` for
     * subsequent lines
     * @return the offset for the margin in pixels.
     */
    override fun getLeadingMargin(first: Boolean): Int {
        return (2 * BULLET_RADIUS + 2 * gapWidth).toInt()
    }

    /**
     * Renders the leading margin. This is called before the margin has been adjusted by the value
     * returned by [getLeadingMargin]. If the beginning of the range of text to which our [CharSequence]
     * parameter [text] is attached is not equal to our [Int] parameter [start] we return having done
     * nothing. Otherwise we save the style of our [Paint] parameter [paint] in our variable `val style`
     * and its sRGB color in our variable `val oldcolor` in order to restore them before returning.
     * Then we set the color of [paint] to our [color] property, and its style to [Paint.Style.FILL]
     * (Geometry and text drawn with this style will be filled, ignoring all stroke-related settings
     * in the paint). We initialize our [Float] variable `val y` to the sum of our [top] parameter
     * and our [bottom] parameter divided by 2f.
     *
     * @param canvas the [Canvas] to draw on.
     * @param paint the [Paint] to use. The this should be left unchanged on exit.
     * @param x the current position of the margin.
     * @param dir the base direction of the paragraph; if negative, the margin
     * is to the right of the text, otherwise it is to the left.
     * @param top the top of the line
     * @param baseline the baseline of the line
     * @param bottom the bottom of the line
     * @param text the text
     * @param start the start of the line
     * @param end the end of the line
     * @param first true if this is the first line of its paragraph
     * @param l the layout containing this line
     */
    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        x: Int,
        dir: Int,
        top: Int,
        baseline: Int,
        bottom: Int,
        text: CharSequence,
        start: Int,
        end: Int,
        first: Boolean,
        l: Layout
    ) {
        if ((text as Spanned).getSpanStart(this) == start) {
            val style = paint.style
            val oldcolor = paint.color
            paint.color = color
            paint.style = Paint.Style.FILL
            val y: Float = (top + bottom) / 2f
            if (canvas.isHardwareAccelerated) {
                if (bulletPath == null) {
                    bulletPath = Path()
                    bulletPath!!.addCircle(0.0f, 0.0f, BULLET_RADIUS, Path.Direction.CW)
                }
                canvas.save()
                canvas.translate(gapWidth + x + dir * BULLET_RADIUS, y)
                canvas.drawPath(bulletPath!!, paint)
                canvas.restore()
            } else {
                canvas.drawCircle(gapWidth + x + dir * BULLET_RADIUS, y, BULLET_RADIUS, paint)
            }

            // restore
            paint.color = oldcolor
            paint.style = style
        }
    }

    companion object {
        @VisibleForTesting
        const val BULLET_RADIUS = 15.0f
        private var bulletPath: Path? = null
    }
}