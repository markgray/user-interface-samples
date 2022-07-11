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
@file:Suppress("unused")

package com.example.android.downloadablefonts

/**
 * Constants class
 */
internal object Constants {
    /**
     * The default value to use for the "&width=" query string.
     */
    const val WIDTH_DEFAULT = 100

    /**
     * The maximum value to use for the "&width=" query string.
     */
    const val WIDTH_MAX = 1000

    /**
     * The minimum value to use for the "&width=" query string.
     */
    const val WIDTH_MIN = 0

    /**
     * The default value to use for the "&weight=" query string.
     */
    const val WEIGHT_DEFAULT = 400

    /**
     * The maximum value to use for the "&weight=" query string.
     */
    const val WEIGHT_MAX = 1000

    /**
     * The minimum value to use for the "&weight=" query string.
     */
    const val WEIGHT_MIN = 0

    /**
     * The default value to use for the "&italic=" query string.
     */
    const val ITALIC_DEFAULT = 0f

    /**
     * The maximum value to use for the "&italic=" query string.
     */
    const val ITALIC_MAX = 1f

    /**
     * The minimum value to use for the "&italic=" query string.
     */
    const val ITALIC_MIN = 0f
}
