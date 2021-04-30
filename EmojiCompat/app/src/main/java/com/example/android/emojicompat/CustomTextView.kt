/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.example.android.emojicompat

import android.content.Context
import android.text.InputFilter
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.emoji.text.EmojiCompat
import androidx.emoji.widget.EmojiTextViewHelper

/**
 * A sample implementation of a custom `TextView` which uses [EmojiTextViewHelper] to make it
 * compatible with [EmojiCompat]. The constructor performs inflation from XML and applies a class
 * specific base style from a theme attribute or style resource.
 *
 * @param context The [Context] the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 */
class CustomTextView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context!!, attrs, defStyleAttr) {
    private var mEmojiTextViewHelper: EmojiTextViewHelper? = null
    override fun setFilters(filters: Array<InputFilter>) {
        super.setFilters(emojiTextViewHelper.getFilters(filters))
    }

    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        emojiTextViewHelper.setAllCaps(allCaps)
    }

    /**
     * Returns the [EmojiTextViewHelper] for this TextView.
     *
     *
     * This method can be called from super constructors through [ ][.setFilters] or [.setAllCaps].
     */
    private val emojiTextViewHelper: EmojiTextViewHelper
        get() {
            if (mEmojiTextViewHelper == null) {
                mEmojiTextViewHelper = EmojiTextViewHelper(this)
            }
            return mEmojiTextViewHelper!!
        }

    init {
        emojiTextViewHelper.updateTransformationMethod()
    }
}