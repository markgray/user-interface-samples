/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.appshortcuts

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * Just exists to allow any thread to use [Toast] to notify the user on the UI thread.
 */
object Utils {
    /**
     * Adds a [Runnable] displaying a [Toast] to the message queue of the UI thread.
     *
     * @param context the context to use for the [Toast], the [Context] used to construct the
     * [ShortcutHelper] that calls us in our case.
     * @param message the mesage to toast, errors that occur when [ShortcutHelper] uses the
     * [ShortcutManager] in its `callShortcutManager` method in our case.
     */
    fun showToast(context: Context?, message: String?) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}