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

package com.example.androidx.preference.sample

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen

/**
 * Key used to save the title associated with this activity in the [Bundle] passed to
 * `onSaveInstanceState` and restore in `onCreate` from the [Bundle] passed it.
 */
private const val TITLE_TAG = "settingsActivityTitle"

/**
 * The goal of this sample is to demonstrate various functionality present in the AndroidX Preference
 * Library. For more information on how to use the AndroidX Preference Library to build settings
 * screens, see [Settings](https://developer.android.com/guide/topics/ui/settings/). As such all of
 * the "functionality" of the demo is implemented using `PreferenceScreen` xml files in our
 * res/xml/ directory.
 *
 * Note: The framework API (android.preference.*) is deprecated as of Q, and the AndroidX library
 * should be used instead.
 *
 * The `interface` [PreferenceFragmentCompat.OnPreferenceStartFragmentCallback] that we implement
 * has one method: `onPreferenceStartFragment` which we implement to be able to process preference
 * items that wish to switch to a specified fragment.
 */
class MainActivity : AppCompatActivity(),
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`
     * then we set our content view to our layout file [R.layout.activity_main] which just consists
     * of a `LinearLayout` root view holding a `FrameLayout` with resource ID [R.id.settings] into
     * which we add whichever fragment is called for.
     *
     * If our [Bundle] parameter [savedInstanceState] is `null` this is the first time we have been
     * called so we use the the [FragmentManager] for interacting with fragments associated with this
     * activity to begin a [FragmentTransaction] which we use to replace the current contents of the
     * container with ID [R.id.settings] with a new instance of [SettingsFragment] and then we commit
     * that [FragmentTransaction]. If [savedInstanceState] is not `null` we are being started after a
     * configuration change so we set the title associated with this activity to the [CharSequence]
     * stored in [savedInstanceState] under the key [TITLE_TAG].
     *
     * Next we add an [FragmentManager.OnBackStackChangedListener] whose lambda will set the title
     * associated with this activity to the string "Preferences Sample" if the `backStackEntryCount`
     * property of the [FragmentManager] for interacting with fragments associated with this activity
     * is equal to 0.
     *
     * Finally we call the `setDisplayHomeAsUpEnabled` method of the Activity's ActionBar with `true`
     * to indicate that home should be displayed as an "up" affordance.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     * ***Note: Otherwise it is null.*** In our case our [onSaveInstanceState] override stores the
     * title associated with this activity under the key [TITLE_TAG] in its [Bundle] parameter, and
     * if [savedInstanceState] is non-`null` we retrieve that [CharSequence] and restore the title.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        } else {
            title = savedInstanceState.getCharSequence(TITLE_TAG)
        }
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                setTitle(R.string.title)
            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * Called to retrieve per-instance state from an activity before being killed so that the state
     * can be restored in [onCreate] or [onRestoreInstanceState] (the [Bundle] populated by this
     * method will be passed to both). First we call our super's implementation of `onSaveInstanceState`
     * then we store the current activity title in [outState] under the key [TITLE_TAG].
     *
     * @param outState [Bundle] in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, title)
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar. If the [FragmentManager.popBackStackImmediate]
     * method of the [FragmentManager] for interacting with fragments associated with this activity
     * returns `true` (pops the top state off the back stack immediately and returns `true` if there
     * was something popped, or `false` if not) we return `true` to the caller to indicate the Up
     * navigation completed successfully. If [FragmentManager.popBackStackImmediate] returns `false`
     * we return the value returned by our super's implementation of `onSupportNavigateUp`.
     *
     * @return `true` if Up navigation completed successfully and this Activity was finished,
     * `false` otherwise.
     */
    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.popBackStackImmediate()) {
            return true
        }
        return super.onSupportNavigateUp()
    }

    /**
     * Called when the user has clicked on a preference that has a fragment class name associated
     * with it. The implementation should instantiate and switch to an instance of the given
     * fragment.
     *
     * This is called because a [Preference] in the [PreferenceScreen] defined in the xml/root.xml
     * file has an "app:fragment" attribute for one of the [PreferenceFragmentCompat] classes that
     * are defined in this file and the user has clicked on that [Preference].
     *
     * First we initialize our [Bundle] variable `val args` to the `extras` property of our [Preference]
     * parameter [pref]. We use the current [FragmentFactory] used to instantiate new Fragment instances
     * of the [FragmentManager] for interacting with fragments associated with this activity to create a
     * new instance of a [Fragment] whose class name is provided as the `fragment` property of [pref]
     * using the default classloader and `apply` a lambda which sets the construction arguments for the
     * fragment to `args` and sets the target for the fragment to our [PreferenceFragmentCompat] parameter
     * [caller].
     *
     * We then use the [FragmentManager] for interacting with fragments associated with this activity
     * to begin a new [FragmentTransaction] then use that [FragmentTransaction] to replace the current
     * contents of the container view with ID [R.id.settings] with `fragment`, add the transaction to
     * the back stack, and then commit the [FragmentTransaction]. Finally we set the title associated
     * with this activity to the `title` property of [pref] and return `true` to report that the
     * fragment creation has been handled.
     *
     * @param caller The fragment requesting navigation
     * @param pref   The [Preference] requesting the fragment
     * @return `true` if the fragment creation has been handled
     */
    override fun onPreferenceStartFragment(
        caller: PreferenceFragmentCompat,
        pref: Preference
    ): Boolean {
        // Instantiate the new Fragment
        val args: Bundle = pref.extras
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment!!
        ).apply {
            arguments = args
            @Suppress("DEPRECATION") // TODO: Replace with a FragmentResultListener
            setTargetFragment(caller, 0)
        }
        // Replace the existing Fragment with the new Fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.settings, fragment)
            .addToBackStack(null)
            .commit()
        title = pref.title
        return true
    }

    /**
     * The root preference fragment that displays preferences that link to the other preference
     * fragments below.
     */
    class SettingsFragment : PreferenceFragmentCompat() {
        /**
         * Called during [onCreate] to supply the preferences for this fragment. Subclasses are
         * expected to call [setPreferenceScreen] either directly or via helper methods such as
         * [addPreferencesFromResource].
         *
         * We just call the [setPreferencesFromResource] method with the resource ID of our root
         * [PreferenceScreen] xml file [R.xml.root] (see the file xml/root.xml) and our [String]
         * parameter [rootKey]. Our [PreferenceScreen] holds four [Preference] elements each having
         * the attributes:
         *  - "app:title" the `title` property of the [Preference] passed to our [onPreferenceStartFragment]
         *  override which it uses to set the title associated with this activity, as well as the first
         *  line of the view for the [Preference] in the inflated xml/root.xml file
         *  - "app:summary" the second line of the view for the [Preference] in the inflated
         *  xml/root.xml file
         *  - "app:fragment" the `fragment` property of the [Preference] passed to our [onPreferenceStartFragment]
         *  override which it uses as the class name of the [PreferenceFragmentCompat] which it constructs
         *  and loads into the container with ID [R.id.settings] (the [FrameLayout] in our activity's
         *  layout file layout/activity_main.xml)
         *
         * @param savedInstanceState If the fragment is being re-created from a previous saved state,
         *                           this is the state.
         * @param rootKey            If non-`null`, this preference fragment should be rooted at the
         *                           [PreferenceScreen] with this key.
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root, rootKey)
        }
    }

    /**
     * A preference fragment that demonstrates commonly used preference attributes:
     *  -
     */
    // Used in the file xml/root.xml as a Preference in its PreferenceScreen
    class BasicPreferencesFragment : PreferenceFragmentCompat() {
        /**
         *
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.basic_preferences, rootKey)
        }
    }

    /**
     * A preference fragment that demonstrates preferences which contain dynamic widgets.
     */
    // Used in the file xml/root.xml as a Preference in its PreferenceScreen
    class WidgetPreferencesFragment : PreferenceFragmentCompat() {
        /**
         *
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.widgets, rootKey)
        }
    }

    /**
     * A preference fragment that demonstrates preferences that launch a dialog when tapped.
     */
    // Used in the file xml/root.xml as a Preference in its PreferenceScreen
    class DialogPreferencesFragment : PreferenceFragmentCompat() {
        /**
         *
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.dialogs, rootKey)
        }
    }

    /**
     * A preference fragment that demonstrates more advanced attributes and functionality.
     */
    // Used in the file xml/root.xml as a Preference in its PreferenceScreen
    class AdvancedPreferencesFragment : PreferenceFragmentCompat() {
        /**
         *
         */
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.advanced, rootKey)
        }
    }
}
