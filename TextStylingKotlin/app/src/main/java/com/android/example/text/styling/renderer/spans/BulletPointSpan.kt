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
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Path.Direction
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import android.text.Layout
import android.text.Spanned
import android.text.style.LeadingMarginSpan
import android.text.style.BulletSpan
import androidx.core.graphics.withTranslation

/**
 * Creating a bullet span with bigger bullets than [BulletSpan] and with a left margin.
 *
 * @param gapWidth the size of the gap width (left margin) defaults to [DEFAULT_GAP_WIDTH]
 * @param color custom color to be used for bullets
 * @param useColor flag which if `true` tells us to use the custom color [color] for the bullet
 */
class BulletPointSpan(
        @Px private val gapWidth: Int = DEFAULT_GAP_WIDTH,
        @ColorInt private val color: Int = Color.BLACK,
        private val useColor: Boolean = color != Color.BLACK
) : LeadingMarginSpan {

    /**
     * The [Path] we use to draw a circle if the [Canvas] is hardware accelerated.
     *
     * By default, lazy is thread safe. This is good if this property can be accessed from different
     * threads, but impacts performance otherwise. As this property is initialized in a draw method,
     * it's important to be as fast as possible.
     */
    private val bulletPath: Path by lazy(LazyThreadSafetyMode.NONE) { Path() }

    /**
     * Returns the amount by which to adjust the leading margin. Positive values move away from the
     * leading edge of the paragraph, negative values move towards it. We just return 2 times our
     * constant [DEFAULT_BULLET_RADIUS] plus 2 times our property [gapWidth].
     *
     * @param first `true` if the request is for the first line of a paragraph, `false` for
     * subsequent lines
     * @return the offset for the margin in pixels.
     */
    override fun getLeadingMargin(first: Boolean): Int {
        return (2 * DEFAULT_BULLET_RADIUS + 2 * gapWidth).toInt()
    }

    /**
     * Using a similar drawing mechanism with [BulletSpan] but adding margins before the bullet.
     * This is called before the margin has been adjusted by the value returned by [getLeadingMargin].
     * If the beginning of the range of text to which our [CharSequence] parameter [text] is attached
     * is not equal to our [Int] parameter [lineStart] we return having done nothing. Otherwise we
     * use the [Paint.withCustomColor] extension function of our [Paint] parameter [paint] to have
     * it save the style and color of [paint], execute a lambda and restore the old style and color
     * of [paint] afterwards. The lambda branches on whether our [Canvas] parameter [canvas] is
     * hardware accelerated:
     *  - It _does_ use hardware acceleration -- we add a circle to our [Path] field [bulletPath]
     *  centered at (0,0) of radius [DEFAULT_BULLET_RADIUS] with a direction of [Path.Direction.CW].
     *  We use the [Canvas.withTranslation] extension function of [canvas] to translate [canvas] to
     *  the X coordinate whose value is that returned by our [getCircleXLocation] method for our
     *  parameters [currentMarginLocation], [paragraphDirection] and Y coordinate is the value
     *  returned by our [getCircleYLocation] method for our parameters [lineTop], [lineBottom].
     *  In the lambda of [Canvas.withTranslation] we draw [bulletPath] on [canvas] using [paint] as
     *  the [paint].
     *  - It **does NOT** use hardware acceleration -- we just call the [Canvas.drawCircle] method
     *  of [canvas] to have it draw a circle whose X coordinate is the value returned by our
     *  [getCircleXLocation] method for our parameters [currentMarginLocation], [paragraphDirection]
     *  and Y coordinate is the value returned by our [getCircleYLocation] method for our parameters
     *  [lineTop], [lineBottom], of radius [DEFAULT_BULLET_RADIUS] using [paint] as its [Paint].
     *
     * @param canvas the [Canvas] to draw on.
     * @param paint the [Paint] to use. The this should be left unchanged on exit.
     * @param currentMarginLocation the current position of the margin.
     * @param paragraphDirection the base direction of the paragraph; if negative, the margin
     * is to the right of the text, otherwise it is to the left.
     * @param lineTop the top of the line
     * @param lineBaseline the baseline of the line
     * @param lineBottom the bottom of the line
     * @param text the text
     * @param lineStart the start of the line
     * @param lineEnd the end of the line
     * @param isFirstLine true if this is the first line of its paragraph
     * @param layout the layout containing this line
     */
    override fun drawLeadingMargin(
        canvas: Canvas,
        paint: Paint,
        currentMarginLocation: Int,
        paragraphDirection: Int,
        lineTop: Int,
        lineBaseline: Int,
        lineBottom: Int,
        text: CharSequence,
        lineStart: Int,
        lineEnd: Int,
        isFirstLine: Boolean,
        layout: Layout
    ) {
        if ((text as Spanned).getSpanStart(this) == lineStart) {
            paint.withCustomColor {
                if (canvas.isHardwareAccelerated) {
                    // Bullet is slightly better to avoid aliasing artifacts on mdpi devices.
                    bulletPath.addCircle(0.0f, 0.0f, 1.2f * DEFAULT_BULLET_RADIUS, Direction.CW)

                    canvas.withTranslation(
                        getCircleXLocation(currentMarginLocation, paragraphDirection),
                        getCircleYLocation(lineTop, lineBottom)
                    ) {
                        drawPath(bulletPath, paint)
                    }
                } else {
                    canvas.drawCircle(
                        getCircleXLocation(currentMarginLocation, paragraphDirection),
                        getCircleYLocation(lineTop, lineBottom),
                        DEFAULT_BULLET_RADIUS,
                        paint
                    )
                }
            }
        }
    }

    /**
     * Convenience function to calculate the Y coordinate of the center of our bullet point circle
     * on the [Canvas] it is to be drawn on.
     *
     * @param lineTop the Y coordinate of the top of the line
     * @param lineBottom the Y coordinate of the bottom of the line
     * @return the Y coordinate of the center of the line.
     */
    private fun getCircleYLocation(lineTop: Int, lineBottom: Int) =
            (lineTop + lineBottom) / 2.0f

    /**
     * Convenience function to calculate the X coordinate of the center of our bullet point circle
     * on the [Canvas] it is to be drawn on. We just return the value [gapWidth] plus [currentMarginLocation]
     * plus [paragraphDirection] times [DEFAULT_BULLET_RADIUS]
     *
     * @param currentMarginLocation the current position of the margin.
     * @param paragraphDirection the base direction of the paragraph; if negative, the margin
     * is to the right of the text, otherwise it is to the left.
     * @return the X coordinate on the [Canvas] at which the bullet point circle should be drawn.
     */
    private fun getCircleXLocation(currentMarginLocation: Int, paragraphDirection: Int) =
            gapWidth + currentMarginLocation + paragraphDirection * DEFAULT_BULLET_RADIUS

    companion object {
        /**
         * The default value to use for our left margin.
         */
        private const val DEFAULT_GAP_WIDTH = 2

        /**
         * The default value to use for the radius of our bullet point circle.
         */
        @VisibleForTesting
        const val DEFAULT_BULLET_RADIUS = 15.0f
    }

    /**
     * When a custom color is used for bullets, the default style and colors need to be saved to
     * then be set again after the draw finishes. This extension hides the boilerplate.
     */
    private inline fun Paint.withCustomColor(block: () -> Unit) {
        val oldStyle = style
        val oldColor = if (useColor) color else Color.TRANSPARENT

        if (useColor) {
            color = this@BulletPointSpan.color
        }

        style = Paint.Style.FILL

        block()

        if (useColor) {
            color = oldColor
        }

        style = oldStyle
    }
}
