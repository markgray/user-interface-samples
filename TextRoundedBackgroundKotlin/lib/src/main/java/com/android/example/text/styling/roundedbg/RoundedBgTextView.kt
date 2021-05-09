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
import android.graphics.Canvas
import android.text.Spanned
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.withTranslation

/**
 * A TextView that can draw rounded background to the portions of the text. See
 * [TextRoundedBgHelper] for more information.
 *
 * See [TextRoundedBgAttributeReader] for supported attributes.
 *
 * @param context The [Context] the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 */
class RoundedBgTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * The [TextRoundedBgHelper] we use to draw the background of our view.
     */
    private val textRoundedBgHelper: TextRoundedBgHelper

    init {
        /**
         * A [TextRoundedBgAttributeReader] that reads our [AttributeSet] field [attrs] and sets its
         * corresponding properties to the values of the custom attributes it finds therein or to
         * the default values for those attributes. These are the attributes that the constructor of
         * [TextRoundedBgAttributeReader] needs.
         */
        val attributeReader = TextRoundedBgAttributeReader(context, attrs)
        textRoundedBgHelper = TextRoundedBgHelper(
            horizontalPadding = attributeReader.horizontalPadding,
            verticalPadding = attributeReader.verticalPadding,
            drawable = attributeReader.drawable,
            drawableLeft = attributeReader.drawableLeft,
            drawableMid = attributeReader.drawableMid,
            drawableRight = attributeReader.drawableRight
        )
    }

    /**
     * We implement this to do our drawing. If the `text` property of our [AppCompatTextView] super
     * is an instance of [Spanned] and the `Layout` that it is currently using to display its text
     * is not `null` we use the [Canvas.withTranslation] extension function of our [Canvas] parameter
     * [canvas] to have it translate to the x coordinate of the total left padding of its view and
     * y coordinate of the total top padding of its view and then execute the `draw` method of our
     * [TextRoundedBgHelper] field [textRoundedBgHelper] to draw the background of the `text` on our
     * [Canvas] parameter [canvas] with our super's `layout` property as the `Layout` to use.
     * The translation and lambda are wrapped in in calls to [Canvas.save] and [Canvas.restoreToCount]
     * so that the state of [canvas] is retored after doing the drawing. Finally we call our super's
     * implementation of `onDraw` to have it draw the `text` on top of our rounded background.
     *
     * @param canvas the [Canvas] on which the background will be drawn
     */
    override fun onDraw(canvas: Canvas) {
        // need to draw bg first so that text can be on top during super.onDraw()
        if (text is Spanned && layout != null) {
            canvas.withTranslation(totalPaddingLeft.toFloat(), totalPaddingTop.toFloat()) {
                textRoundedBgHelper.draw(canvas, text as Spanned, layout)
            }
        }
        super.onDraw(canvas)
    }
}