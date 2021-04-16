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
package com.example.android.downloadablefonts

/**
 * Builder class for constructing a query for downloading a font.
 */
internal class QueryBuilder(private var mFamilyName: String) {
    private var mWidth: Float? = null
    private var mWeight: Int? = null
    private var mItalic: Float? = null
    private var mBesteffort: Boolean? = null
    @Suppress("unused")
    fun withFamilyName(familyName: String): QueryBuilder {
        mFamilyName = familyName
        return this
    }

    fun withWidth(width: Float): QueryBuilder {
        require(width > Constants.WIDTH_MIN) { "Width must be more than 0" }
        mWidth = width
        return this
    }

    fun withWeight(weight: Int): QueryBuilder {
        require(!(weight <= Constants.WEIGHT_MIN || weight >= Constants.WEIGHT_MAX)) { "Weight must be between 0 and 1000 (exclusive)" }
        mWeight = weight
        return this
    }

    fun withItalic(italic: Float): QueryBuilder {
        require(!(italic < Constants.ITALIC_MIN || italic > Constants.ITALIC_MAX)) { "Italic must be between 0 and 1 (inclusive)" }
        mItalic = italic
        return this
    }

    fun withBestEffort(bestEffort: Boolean): QueryBuilder {
        mBesteffort = bestEffort
        return this
    }

    fun build(): String {
        if (mWeight == null && mWidth == null && mItalic == null && mBesteffort == null) {
            return mFamilyName
        }
        val builder = StringBuilder()
        builder.append("name=").append(mFamilyName)
        if (mWeight != null) {
            builder.append("&weight=").append(mWeight)
        }
        if (mWidth != null) {
            builder.append("&width=").append(mWidth)
        }
        if (mItalic != null) {
            builder.append("&italic=").append(mItalic)
        }
        if (mBesteffort != null) {
            builder.append("&besteffort=").append(mBesteffort)
        }
        return builder.toString()
    }
}