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

import android.app.Application
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * Base class for maintaining global application state. Here we provide our own implementation by
 * creating a subclass and specifying the fully-qualified name of this subclass as the "android:name"
 * attribute in our AndroidManifest.xml's <application> tag.
 *
 * The [Application] class, or your subclass of the [Application] class, is instantiated before any
 * other class when the process for your application/package is created
 *
 * It is used as the android:name attribute of the application element in our AndroidManifest.xml
 */
@Suppress("unused")
class DarkThemeApplication : Application() {
    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created. First we call our super's implementation of
     * `onCreate`, then we initialize our [SharedPreferences] variable `val sharedPreferences` to an
     * instance that points to the default file that is used by the preference framework for our
     * context, and we initialize our [String] variable `val themePref` to the [String] value from
     * `sharedPreferences` that is stored in it under the key "themePref" defaulting to the [String]
     * [ThemeHelper.DEFAULT_MODE] ("default"). Finally we call the [ThemeHelper.applyTheme] method
     * with `themePref` to have it set the default night mode to `themePref`.
     */
    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences.getString("themePref", ThemeHelper.DEFAULT_MODE)
        ThemeHelper.applyTheme(themePref!!)
    }
}