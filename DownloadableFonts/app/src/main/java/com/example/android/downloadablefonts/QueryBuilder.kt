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
 *
 * @param mFamilyName the family name of the font that is to be requested.
 */
internal class QueryBuilder(private var mFamilyName: String) {
    /**
     * The width of the font, more than [Constants.WIDTH_MIN] (0) and less than [Constants.WIDTH_MAX]
     * (1000), with the default [Constants.WIDTH_DEFAULT] (100). Set by our [withWidth] method, its
     * value is appended to the "&width=" query string of the query that is built.
     */
    private var mWidth: Float? = null

    /**
     * The weight of the font, greater than [Constants.WEIGHT_MIN] (0) and less than [Constants.WEIGHT_MAX]
     * (1000) with the default [Constants.WEIGHT_DEFAULT] (400). Set by our [withWeight] method, its
     * value is appended to the "&weight=" query string of the query that is built.
     */
    private var mWeight: Int? = null

    /**
     * The italic value of the font, greater than or equal to [Constants.ITALIC_MIN] (0f) and less
     * than or equal to [Constants.ITALIC_MAX] (1.0f) with the default [Constants.ITALIC_DEFAULT]
     * (0f). Set by our [withItalic] method, its value is appended to the "&italic=" query string
     * of the query that is built.
     */
    private var mItalic: Float? = null

    /**
     * `true` or `false` value of the "&besteffort=" query string added to our query. Set by our
     * [withBestEffort] method it defaults to `true` and if `true` and your query specifies a valid
     * family name but the requested width/weight/italic value is not supported the font provider
     * will return the best match it can find within the family.
     */
    private var mBesteffort: Boolean? = null

    /**
     * Unused setter for our [String] field [mFamilyName] which is used as the font family name in
     * the request we are building. (our constructor sets the field to its parameter). We just set
     * [mFamilyName] to our [String] parameter [familyName] and return `this` [QueryBuilder] to
     * allow chaining.
     *
     * @param familyName the font family name to use in the request being built.
     * @return `this` [QueryBuilder] to allow chaining.
     */
    @Suppress("unused")
    fun withFamilyName(familyName: String): QueryBuilder {
        mFamilyName = familyName
        return this
    }

    /**
     * Setter for our [Float] field [mWidth] which is used to select the width of the font in the
     * request we are building. We [require] that our [Float] parameter [width] be greater than
     * [Constants.WIDTH_MIN] (0f) throwing an [IllegalArgumentException] with the message "Width
     * must be more than 0" if it is not, then set our field [mWidth] to [width] and return `this`
     * [QueryBuilder] to allow chaining.
     *
     * @param width the width of the font to request in the request we are building.
     * @return `this` [QueryBuilder] to allow chaining.
     */
    fun withWidth(width: Float): QueryBuilder {
        require(width > Constants.WIDTH_MIN) { "Width must be more than 0" }
        mWidth = width
        return this
    }

    /**
     * Setter for our [Int] field [mWeight] which is used to select the weight of the font in the
     * request we are building. We [require] that our [Int] parameter [weight] be between
     * [Constants.WEIGHT_MIN] (0) and [Constants.WEIGHT_MAX] (1000) exclusive throwing an
     * [IllegalArgumentException] with the message "Weight must be between 0 and 1000 (exclusive)"
     * if it is not, then set our field [mWidth] to [weight] and return `this` [QueryBuilder] to
     * allow chaining.
     *
     * @param weight the weight of the font to request in the request we are building.
     * @return `this` [QueryBuilder] to allow chaining.
     */
    fun withWeight(weight: Int): QueryBuilder {
        require(!(weight <= Constants.WEIGHT_MIN || weight >= Constants.WEIGHT_MAX)) {
            "Weight must be between 0 and 1000 (exclusive)"
        }
        mWeight = weight
        return this
    }

    /**
     * Setter for our [Float] field [mItalic] which is used to select the italic value of the font
     * in the request we are building. We [require] that our [Float] parameter [italic] be between
     * [Constants.ITALIC_MIN] (0) and [Constants.ITALIC_MAX] (1000) inclusive throwing an
     * [IllegalArgumentException] with the message "Italic must be between 0 and 1 (inclusive)"
     * if it is not, then set our field [mItalic] to [italic] and return `this` [QueryBuilder] to
     * allow chaining.
     *
     * @param italic the value of italic of the font to request in the request we are building.
     * @return `this` [QueryBuilder] to allow chaining.
     */
    fun withItalic(italic: Float): QueryBuilder {
        require(!(italic < Constants.ITALIC_MIN || italic > Constants.ITALIC_MAX)) {
            "Italic must be between 0 and 1 (inclusive)"
        }
        mItalic = italic
        return this
    }

    /**
     * Setter for our [Boolean] field [mBesteffort] which is used to supply the value of the
     * "&besteffort=" query string of the request we are building. We just our field [mBesteffort]
     * to [bestEffort] and return `this` [QueryBuilder] to allow chaining.
     *
     * @param bestEffort the `true` of `false` value to use for the "&besteffort=" query string.
     * @return `this` [QueryBuilder] to allow chaining.
     */
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