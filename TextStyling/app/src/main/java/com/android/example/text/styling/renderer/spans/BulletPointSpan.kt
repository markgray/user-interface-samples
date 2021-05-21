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
    override fun getLeadingMargin(first: Boolean): Int {
        return (2 * BULLET_RADIUS + 2 * gapWidth).toInt()
    }

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
            val y = (top + bottom) / 2f
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