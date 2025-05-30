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

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView

/**
 * Sample demonstrating the different ways to support Dark Mode on Android. Uses the Material Design
 * Components Library. Light mode uses the colors from the file values/colors.xml and night mode uses
 * the colors from the file values-night/colors.xml -- see README.md for a very detailed explanation.
 *
 * The UI consists of three framents which are switched between using a [BottomNavigationView]:
 *  - [WelcomeFragment] just consists of a `TextView` with a welcome message and two `ImageView` in
 *  a vertical `LinearLayout` (does nothing)
 *  - [PreferencesFragment] just consists of two `TextView`, an `EditText`, a `RadioGroup` with two
 *  `RadioButton`, a `SwitchMaterial` switch, and a `Button` in a vertical `LinearLayout` (does
 *  nothing)
 *  - [SettingsFragment] extends `PreferenceFragmentCompat` and its xml/preferences.xml file consists
 *  of a `androidx.preference.PreferenceScreen` whose "Theme" `PreferenceCategory` contains a single
 *  `ListPreference` with the key "themePref" which allows the user to select between the themes for
 *  "Light", "Dark" or "System Default" (this does what it says it does)
 */
class MainActivity : AppCompatActivity() {
    /**
     * Listener for handling selection events on bottom navigation items. Its lambda override of the
     * `onNavigationItemSelected` method of this interface is called when an item in the bottom
     * navigation menu is selected with the [MenuItem] which was selected and the lambda branches on
     * the [MenuItem.getItemId] of the item (aka kotlin `itemId` property):
     *  - `R.id.navigation_home` calls our [showFragment] method to replace the fragment with tag
     *  [WelcomeFragment.TAG] with a new instance of [WelcomeFragment].
     *  - `R.id.navigation_preferences` calls our [showFragment] method to replace the fragment with
     *  tag [PreferencesFragment.TAG] with a new instance of [PreferencesFragment].
     *  - `R.id.navigation_settings` calls our [showFragment] method to replace the fragment with
     *  tag [SettingsFragment.TAG] with a new instance of [SettingsFragment].
     *
     * After calling [showFragment] each branch returns `true` to display the item as the selected
     * item. The `FrameLayout` with ID `R.id.fragment_layout` in our layout file is used to hold
     * the latest fragment. If the `itemID` is not one of ours we return `false` so the item will
     * not be selected.
     */
    private val mOnNavigationListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                showFragment(WelcomeFragment.TAG)
                return@OnItemSelectedListener true
            }

            R.id.navigation_preferences -> {
                showFragment(PreferencesFragment.TAG)
                return@OnItemSelectedListener true
            }

            R.id.navigation_settings -> {
                showFragment(SettingsFragment.TAG)
                return@OnItemSelectedListener true
            }
        }
        false
    }

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file `R.layout.activity_main`. The root view of
     * this layout is a `ConstraintLayout` with ID `R.id.container` which holds a [Toolbar] with ID
     * `R.id.toolbar`, a `FrameLayout` with ID `R.id.fragment_layout`, and a [BottomNavigationView]
     * with ID `R.id.navigation` at the bottom of the `ConstraintLayout`. We set the [Toolbar] with
     * ID `R.id.toolbar` in our UI to act as the `ActionBar` for this Activity window by calling the
     * [setSupportActionBar] method. Next we initialize our [BottomNavigationView] variable
     * `val navigation` by finding the view with ID `R.id.navigation` and set its
     * [BottomNavigationView.OnNavigationItemSelectedListener] to our field [mOnNavigationListener].
     * Finally if our [Bundle] parameter [savedInstanceState] is `null` we are being started for the
     * first time so we call our [showFragment] method to have it load our [WelcomeFragment] into
     * the `FrameLayout` with ID `R.id.fragment_layout`. If it is non-`null` we are being restarted
     * and the system will care of restoring whichever fragment was running when we were shut down.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     * ***Note: Otherwise it is null.***
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<ConstraintLayout>(R.id.container)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
                topMargin = insets.top
                bottomMargin = insets.bottom
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnItemSelectedListener(mOnNavigationListener)
        if (savedInstanceState == null) {
            showFragment(WelcomeFragment.TAG)
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in the [Menu] parameter [menu]. This is only called once, the first time the options
     * menu is displayed. To update the menu every time it is displayed, see [onPrepareOptionsMenu].
     * We use a [MenuInflater] for this context to inflate our menu layout file `R.menu.main_menu`
     * into our [Menu] parameter [menu]. If holds a single [MenuItem] with ID `R.id.action_more` and
     * the title "More". We initialize our [MenuItem] variable `val item` by finding that [MenuItem]
     * then initialize our [Drawable] variable `val drawableWrap` by using the [DrawableCompat.wrap]
     * method to wrap the [MenuItem.getIcon] (aka kotlin `icon` property) of `item` and making that
     * [Drawable] mutable. We then use the [DrawableCompat.setTint] method to set the tint of
     * `drawableWrap` to the current theme color which our [ColorUtils.getThemeColor] method returns
     * for the `com.google.android.material.R.attr.colorOnPrimary` color (which resolves to the color
     * named "primary" which is "Blue 300" in the resource file values-night/colors.xml, and "Blue 700"
     * in the resource file values/colors.xml). We then set the `icon` of `item` to `drawableWrap` and
     * return `true` so that the menu will be displayed. ***Note: the tinting of the icon is useless
     * on newer devices because menu icons are not displayed***
     *
     * @param menu The options menu in which you place your items.
     * @return You must return `true` for the menu to be displayed, if you return `false` it will
     * not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // This demonstrates how to programmatically tint a drawable
        val item = menu.findItem(R.id.action_more)
        val drawableWrap: Drawable = DrawableCompat.wrap(item.icon!!).mutate()
        DrawableCompat.setTint(drawableWrap, ColorUtils.getThemeColor(this, com.google.android.material.R.attr.colorOnPrimary))
        item.icon = drawableWrap
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected. We initialize our [Int]
     * variable `val id` to the `itemId` of our [MenuItem] parameter [item]. If `id` is equal to the
     * resource ID `R.id.action_more` (our [MenuItem]) we return `true` to the caller to consume the
     * event here, otherwise we return the value returned by our super's implementation of
     * `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return Return `false` to allow normal menu processing to proceed, `true` to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id: Int = item.itemId
        return if (id == R.id.action_more) {
            // TODO
            true
        } else super.onOptionsItemSelected(item)
    }

    /**
     * Called to replace the [Fragment] occupying the `R.id.fragment_layout` container in our UI with
     * the [Fragment] whose tag is the same as our [String] parameter [tag]. We use the [FragmentManager]
     * for interacting with fragments associated with this activity to search for a [Fragment] whose
     * tag is [tag] and initialize our variable `var fragment` to what it returns. If `fragment` is
     * `null` (an instance of a [Fragment] with tag [tag] is not currently a part of the activity
     * state) we set `fragment` to a new instance of one of our fragments based on the value of [tag]:
     *  - [WelcomeFragment.TAG] we set it to a new instance of [WelcomeFragment]
     *  - [PreferencesFragment.TAG] we set it to a new instance of [PreferencesFragment]
     *  - [SettingsFragment.TAG] we set it to a new instance of [SettingsFragment]
     *
     * We then use the [FragmentManager] for interacting with fragments associated with this activity
     * to begin a new [FragmentTransaction] which we use to replace the contents of the container with
     * resource ID `R.id.fragment_layout` with `fragment` using [tag] as its tag and commit that
     * [FragmentTransaction].
     */
    @Suppress("SameParameterValue") // Suggested change would make method less reusable
    private fun showFragment(tag: String) {
        var fragment: Fragment? = supportFragmentManager.findFragmentByTag(tag)
        if (fragment == null) {
            fragment = when (tag) {
                WelcomeFragment.TAG -> {
                    WelcomeFragment()
                }

                PreferencesFragment.TAG -> {
                    PreferencesFragment()
                }

                SettingsFragment.TAG -> {
                    SettingsFragment()
                }

                else -> {
                    WelcomeFragment()
                }
            }
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_layout, fragment, tag)
            .commit()
    }
}
