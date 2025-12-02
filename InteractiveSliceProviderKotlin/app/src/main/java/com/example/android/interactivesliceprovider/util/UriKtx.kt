/*
 * Copyright 2018 The Android Open Source Project
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

package com.example.android.interactivesliceprovider.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri

/**
 * Builds a [Uri] with the `content` scheme and the package name as the authority.
 *
 * @param path The path to append to the Uri.
 * @return A content Uri with the format: `content://<your.package.name>/<path>`.
 */
fun Context.buildUriWithAuthority(path: String): Uri {
    return Uri.Builder()
        .scheme(ContentResolver.SCHEME_CONTENT)
        .authority(packageName)
        .appendPath(path)
        .build()
}