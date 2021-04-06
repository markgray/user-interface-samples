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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * Sample demonstrating the different ways to support Dark Mode on Android. Uses the Material Design
 * Components Library. Light mode uses the colors from the file values/colors.xml and night mode uses
 * the colors from the file values-night/colors.xml -- see README.md for a very detailed explanation.
 */
class MainActivity : AppCompatActivity() {
    private val mOnNavigationListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                showFragment(WelcomeFragment.TAG)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_preferences -> {
                showFragment(PreferencesFragment.TAG)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                showFragment(SettingsFragment.TAG)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById<View>(R.id.toolbar) as Toolbar)
        val navigation = findViewById<BottomNavigationView>(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationListener)
        if (savedInstanceState == null) {
            showFragment(WelcomeFragment.TAG)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        // This demonstrates how to programmatically tint a drawable
        val item = menu.findItem(R.id.action_more)
        val drawableWrap = DrawableCompat.wrap(item.icon).mutate()
        DrawableCompat.setTint(drawableWrap, ColorUtils.getThemeColor(this, R.attr.colorOnPrimary))
        item.icon = drawableWrap
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.action_more) {
            // TODO
            true
        } else super.onOptionsItemSelected(item)
    }

    private fun showFragment(tag: String) {
        var fragment = supportFragmentManager.findFragmentByTag(tag)
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