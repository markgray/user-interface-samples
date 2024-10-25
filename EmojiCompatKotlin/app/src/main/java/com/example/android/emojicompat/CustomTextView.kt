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
import androidx.emoji.widget.EmojiTextViewHelper
import androidx.appcompat.widget.AppCompatTextView
import android.text.InputFilter
import android.util.AttributeSet
import android.widget.TextView
import androidx.emoji.text.EmojiCompat


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
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * The [EmojiTextViewHelper] instance whose [InputFilter]s we use to add [EmojiCompat] emojis
     * to the text which is displayed when the `text` property of our super is changed. Private so
     * that other classes cannot access it, read only access is provided by our [emojiTextViewHelper]
     * property whose `get` method lazily initializes us.
     */
    private var mEmojiTextViewHelper: EmojiTextViewHelper? = null

    /**
     * Returns the [EmojiTextViewHelper] for this TextView. This field can be accessed from our
     * super's constructor through [setFilters] or [setAllCaps]. Its `get` method will lazily
     * initialize the backing field [mEmojiTextViewHelper] with a new instance the first time it
     * is called.
     */
    private val emojiTextViewHelper: EmojiTextViewHelper
        get() {
            if (mEmojiTextViewHelper == null) {
                mEmojiTextViewHelper = EmojiTextViewHelper(this)
            }
            return mEmojiTextViewHelper as EmojiTextViewHelper
        }

    init {
        /**
         * Updates widget's TransformationMethod so that the transformed text can be processed.
         */
        emojiTextViewHelper.updateTransformationMethod()
    }

    /**
     * Appends [EmojiCompat] specific [InputFilter]s to the widget [InputFilter]s passed us in our
     * [filters] parameter if they are not already there. We just call our super's implementation of
     * `setFilters` with the list of [InputFilter]s that the [EmojiTextViewHelper.getFilters] method
     * returns for our parameter [filters] (the [EmojiTextViewHelper.getFilters] method returns the
     * same array if the array passed it already contains the [EmojiCompat] specific [InputFilter]
     * or a new array copy if not).
     *
     * @param filters the current list of [InputFilter]s used by our [AppCompatTextView] super.
     */
    override fun setFilters(filters: Array<InputFilter>) {
        super.setFilters(emojiTextViewHelper.getFilters(filters))
    }

    /**
     * Sets the properties the [AppCompatTextView] to transform input to ALL CAPS display if its
     * [Boolean] parameter [allCaps] is `true`. It does this by calling the [TextView.setAllCaps]
     * method of our super with [allCaps], and then calling the [EmojiTextViewHelper.setAllCaps]
     * method of [emojiTextViewHelper] with [allCaps].
     *
     * @param allCaps if `true` transform input to ALL CAPS display.
     */
    override fun setAllCaps(allCaps: Boolean) {
        super.setAllCaps(allCaps)
        emojiTextViewHelper.setAllCaps(allCaps)
    }

}
