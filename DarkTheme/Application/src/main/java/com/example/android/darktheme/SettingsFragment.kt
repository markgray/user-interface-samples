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
import androidx.preference.PreferenceScreen

/**
 * Extends [PreferenceFragmentCompat] and its xml/preferences.xml file consists of a
 * `androidx.preference.PreferenceScreen` whose "Theme" `PreferenceCategory` contains a single
 * `ListPreference` with the key "themePref" which allows the user to select between the themes
 * for "Light", "Dark" or "System Default" (this does what it says it does)
 */
class SettingsFragment : PreferenceFragmentCompat() {
    /**
     * Called during [onCreate] to supply the preferences for this fragment. Subclasses are expected
     * to call [setPreferenceScreen] either directly or via helper methods such as
     * [addPreferencesFromResource].
     *
     * First we call the [setPreferencesFromResource] method to have it inflate the XML resource with
     * ID `R.xml.preferences` and replace the current preference hierarchy with the preference
     * hierarchy rooted at the key [rootKey]. Next we initialize our [ListPreference] variable
     * `val themePref` by using the [findPreference] method to find the [Preference] with the key
     * "themePref", and if this is not `null` we set the [Preference.OnPreferenceChangeListener] of
     * `themePref` to a lambda whose `onPreferenceChange` override initializes its [String] variable
     * `val themeOption` to the new value of the preference, calls our [ThemeHelper.applyTheme]
     * method with `themeOption` to have it set the default night mode to the value selected (one
     * of "light", "dark" or "default"), and finally we return `true` to the caller to update the
     * state of the preference with the new value.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     * @param rootKey If non-`null`, this preference fragment should be rooted at the
     * [PreferenceScreen] with this key.
     */
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val themePref = findPreference<ListPreference>("themePref")
        if (themePref != null) {
            themePref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
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
        const val TAG: String = "SettingsFragmentTag"
    }
}
