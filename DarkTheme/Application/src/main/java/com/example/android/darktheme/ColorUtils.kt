/*
 * Copyright (C) 2019 The Android Open Source Project
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
package com.example.android.darktheme

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt

/**
 * Singleton holder for the [getThemeColor] method
 */
object ColorUtils {
    /**
     * Queries the theme of the given [Context] for a theme color. Called from the `onCreateOptionsMenu`
     * override of [MainActivity] to retrieve the color [com.google.android.material.R.attr.colorOnPrimary]
     * that is used for the current theme. **Note: this is used to tint the icon used for a `MenuItem`,
     * but icons are not shown on newer devices.**
     *
     * First we initialize our [TypedArray] variable `val a` to the value that the method
     * [Context.obtainStyledAttributes] of our parameter [context] returns for the theme color
     * attribute that our [Int] parameter [attrResId] resolves to. Then wrapped in a `try` block
     * whose `finally` block recycles `a` we retrieve and return the color value for the attribute
     * at index 0 in `a`, defaulting to [Color.MAGENTA].
     *
     * @param context   the context holding the current theme.
     * @param attrResId the theme color attribute to resolve.
     * @return the theme color
     */
    @ColorInt
    fun getThemeColor(context: Context, @AttrRes attrResId: Int): Int {
        val a: TypedArray = context.obtainStyledAttributes(null, intArrayOf(attrResId))
        return try {
            a.getColor(0, Color.MAGENTA)
        } finally {
            a.recycle()
        }
    }
}