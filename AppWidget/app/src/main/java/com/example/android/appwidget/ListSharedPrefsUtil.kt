/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.example.android.appwidget

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.LayoutRes
import androidx.core.content.edit

/**
 * Contains some convenience functions for accessing the [SharedPreferences] file in which we store
 * the layout files for the `RemoteViews` used to implement our `AppWidget`'s
 */
object ListSharedPrefsUtil {
    /**
     * The name we use for our [SharedPreferences] file.
     */
    private const val PREFS_NAME = "com.example.android.appwidget.GroceryListWidget"

    /**
     * The prefix we prepend to the string value of the `appWidgetId` of the `AppWidget` to form a
     * key for storing, retrieving or removing its layout file from our [SharedPreferences] file.
     */
    private const val PREF_PREFIX_KEY = "appwidget_"

    /**
     * Saves its [Int] parameter [layoutId] in our [SharedPreferences] file under a key formed by
     * appending the string value of our [Int] parameter [appWidgetId] to the prefix string
     * [PREF_PREFIX_KEY].
     *
     * @param context the [Context] to use to retrieve the [SharedPreferences] for our preferences
     * file, the [Context] of [ListWidgetConfigureActivity] in our case.
     * @param appWidgetId the `appWidgetId` of the `AppWidget` whose layout file ID we are saving in
     * our preferences file.
     * @param layoutId the layout file ID of the `AppWidget` whose `appWidgetId` is [appWidgetId]
     */
    internal fun saveWidgetLayoutIdPref(
        context: Context,
        appWidgetId: Int,
        @LayoutRes layoutId: Int
    ) {
        context.getSharedPreferencesByNamedArgument(name = PREFS_NAME, mode = 0).edit {
            putInt(PREF_PREFIX_KEY + appWidgetId, layoutId)
        }
    }

    /**
     * Returns the [Int] layout file ID that was stored in our preferences file under the key formed
     * by appending the string value of our [appWidgetId] parameter to the prefix string
     * [PREF_PREFIX_KEY], defaulting to [R.layout.widget_grocery_list] if there is not such entry.
     *
     * @param context the [Context] to use to retrieve the [SharedPreferences] instance of our
     * preferences file
     * @param appWidgetId the `appWidgetId` of the `AppWidget` whose layout file ID we are to
     * retrieve from our preferences file.
     * @return the [Int] layout file ID that was stored in our preferences file under the key formed
     * by appending the string value of our [appWidgetId] to the prefix string [PREF_PREFIX_KEY]
     */
    internal fun loadWidgetLayoutIdPref(context: Context, appWidgetId: Int): Int =
        context.getSharedPreferencesByNamedArgument(name = PREFS_NAME, mode = 0)
            .getInt(PREF_PREFIX_KEY + appWidgetId, R.layout.widget_grocery_list)

    /**
     * Removes the preference value stored under the key formed by appending the string value of our
     * [appWidgetId] parameter to the prefix string [PREF_PREFIX_KEY]
     *
     * @param context the [Context] to use to retrieve the [SharedPreferences] instance of our
     * preferences file
     * @param appWidgetId the `appWidgetId` of the `AppWidget` whose layout file ID we are to
     * delete from our preferences file.
     */
    internal fun deleteWidgetLayoutIdPref(context: Context, appWidgetId: Int) {
        context.getSharedPreferencesByNamedArgument(name = PREFS_NAME, mode = 0).edit {
            remove(PREF_PREFIX_KEY + appWidgetId)
        }
    }

    /**
     * Wrapper for [Context.getSharedPreferences] to support named arguments. Returns the
     * [SharedPreferences] instance for the preferences file whose name is [name] opened in
     * [mode] preference mode.
     *
     * @param name the filename of the desired preferences file whose [SharedPreferences] instance
     * we are to return.
     * @param mode the mode in which the preferences file should be opened, one of [Context.MODE_PRIVATE],
     * [Context.MODE_WORLD_READABLE], [Context.MODE_WORLD_WRITEABLE], or [Context.MODE_MULTI_PROCESS]
     * ([Context.MODE_PRIVATE] is the only one which is still supported by newer SDK's).
     * @return the [SharedPreferences] instance for the preferences file whose name is [name] opened
     * in [mode] preference mode.
     */
    private fun Context.getSharedPreferencesByNamedArgument(name: String, mode: Int): SharedPreferences {
        return getSharedPreferences(name, mode)
    }
}