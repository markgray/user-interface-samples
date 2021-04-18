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

import androidx.core.provider.FontRequest

/**
 * Builder class for constructing a query for downloading a font.
 *
 * @param familyName the family name of the font that is to be requested.
 * @param width The width of the font, more than [Constants.WIDTH_MIN] (0) and less than
 * [Constants.WIDTH_MAX] (1000), with the default [Constants.WIDTH_DEFAULT] (100). If non-`null`
 * its value is appended to the "&width=" query string of the query that is built.
 * @param weight The weight of the font, greater than [Constants.WEIGHT_MIN] (0) and less than
 * [Constants.WEIGHT_MAX] (1000) with the default [Constants.WEIGHT_DEFAULT] (400). If non-`null`
 * its value is appended to the "&weight=" query string of the query that is built.
 * @param italic The italic value of the font, greater than or equal to [Constants.ITALIC_MIN] (0f)
 * and less than or equal to [Constants.ITALIC_MAX] (1.0f) with the default [Constants.ITALIC_DEFAULT]
 * (0f). If non-`null` its value is appended to the "&italic=" query string of the query that is built.
 * @param besteffort `true` or `false` value of the "&besteffort=" query string added to our query.
 * It defaults to `true` and if `true` and your query specifies a valid family name but the requested
 * width/weight/italic value is not supported the font provider will return the best match it can
 * find within the family.
 */
internal class QueryBuilder(
    private val familyName: String,
    val width: Float? = null,
    val weight: Int? = null,
    val italic: Float? = null,
    val besteffort: Boolean? = null
) {

    /**
     * Builds `this` [QueryBuilder] into a query that can be used in a [FontRequest] for a font from
     * a font provider. If our fields [weight], [width], [italic], and [besteffort] are all `null`
     * we return our font family name field [familyName] to the caller. Otherwise we initialize our
     * [StringBuilder] variable `val builder` to a new instance and add the [String] "name=" followed
     * by [familyName] and proceed to check each of our fields for a non-`null` value and add query
     * strings for the non-`null` fields to `builder` as follows:
     *  - [weight] non-`null` adds the query string "&weight=" followed by the string value of the
     *  [Int] field [weight] to `builder`
     *  - [width] non-`null` adds the query string "&width=" followed by the string value of the
     *  [Float] field [width] to `builder`
     *  - [italic] non-`null` adds the query string "&italic=" followed by the string value of the
     *  [Float] field [italic] to `builder`
     *  - [besteffort] non-`null` adds the query string "&besteffort=" followed by the string value
     *  of the [Boolean] field [besteffort] ("true" or "false") to `builder`
     *
     * Finally we return the [String] value of `builder` to the caller.
     *
     * @return the query [String] to be used when constructing a [FontRequest]
     */
    fun build(): String {
        if (weight == null && width == null && italic == null && besteffort == null) {
            return familyName
        }
        val builder = StringBuilder()
        builder.append("name=").append(familyName)
        weight?.let { builder.append("&weight=").append(weight) }
        width?.let { builder.append("&width=").append(width) }
        italic?.let { builder.append("&italic=").append(italic) }
        besteffort?.let { builder.append("&besteffort=").append(besteffort) }
        return builder.toString()
    }
}
