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

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

/**
 * Extends [PreferenceFragmentCompat] and its xml/preferences.xml file consists of a
 * `androidx.preference.PreferenceScreen` whose "Theme" `PreferenceCategory` contains a single
 * `ListPreference` with the key "themePref" which allows the user to select between the themes
 * for "Light", "Dark" or "System Default" (this does what it says it does)
 */
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePreference = findPreference<ListPreference>("themePref")
        if (themePreference != null) {
            themePreference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
                val themeOption = newValue as String
                ThemeHelper.applyTheme(themeOption)
                true
            }
        }
    }

    companion object {
        /**
         * TAG used by [MainActivity.showFragment] when adding an instance of [SettingsFragment]
         * to the activity state.
         */
        const val TAG = "SettingsFragmentTag"
    }
}