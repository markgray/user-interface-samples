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

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

/**
 * This file holds a single method: [applyTheme] which is called to apply whichever theme the user
 * has chosen, either "light", "dark" or "default" using the [AppCompatDelegate.setDefaultNightMode]
 * method. It is called from the `onCreate` override of [DarkThemeApplication] to set it to the theme
 * stored under the key "themePref" in the app's shared preferences (defaulting to "default" is the
 * preference has not been set) and by the `onCreatePreferences` override of [SettingsFragment] when
 * the user sets a new value for the "themePref" shared preference.
 */
object ThemeHelper {
    /**
     * Value of `themePref` to select "light" mode
     */
    private const val LIGHT_MODE = "light"

    /**
     * Value of `themePref` to select "dark" mode
     */
    private const val DARK_MODE = "dark"

    /**
     * Value of `themePref` to select "default" mode
     */
    const val DEFAULT_MODE: String = "default"

    /**
     * Sets the default night mode for our app based on the value of its [String] parameter [themePref].
     * We branch on the value of [themePref]:
     *  - [LIGHT_MODE] we call [AppCompatDelegate.setDefaultNightMode] with [AppCompatDelegate.MODE_NIGHT_NO]
     *  to have it set the mode to one which always uses a light mode, enabling `notnight` qualified
     *  resources regardless of the time.
     *  - [DARK_MODE] we call [AppCompatDelegate.setDefaultNightMode] with [AppCompatDelegate.MODE_NIGHT_YES]
     *  to have it set the mode to one which always uses a dark mode, enabling night qualified resources
     *  regardless of the time.
     *  - For all other values of [themePref] we branch based on the version of android we are running:
     *      - Greater than or equal to "Q": we call [AppCompatDelegate.setDefaultNightMode] with
     *      [AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM] to have it set the mode to one which uses
     *      the system's night mode setting to determine if it is night or not.
     *      - Less than "Q": we call [AppCompatDelegate.setDefaultNightMode] with
     *      [AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY] to have it set the mode to one which uses a
     *      dark mode when the system's 'Battery Saver' feature is enabled, otherwise it uses a
     *      'light mode'.
     *
     * @param themePref a [String] selecting which default night mode the user has selected, one of
     * "light", "dark" or "default".
     */
    fun applyTheme(themePref: String) {
        when (themePref) {
            LIGHT_MODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            DARK_MODE -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                }
            }
        }
    }
}