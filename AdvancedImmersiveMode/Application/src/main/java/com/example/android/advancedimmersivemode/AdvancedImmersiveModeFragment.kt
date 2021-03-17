/*
* Copyright (C) 2012 The Android Open Source Project
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
@file:Suppress("DEPRECATION")

package com.example.android.advancedimmersivemode

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log

/**
 * Demonstrates how to update the app's UI by toggling immersive mode.
 * Checkboxes are also made available for toggling other UI flags which can
 * alter the behavior of immersive mode.
 * TODO: Switch to android.view.WindowInsetsController for SDK 30+
 */
class AdvancedImmersiveModeFragment : Fragment() {
    private lateinit var mHideNavCheckbox: CheckBox
    private lateinit var mHideStatusBarCheckBox: CheckBox
    private lateinit var mImmersiveModeCheckBox: CheckBox
    private lateinit var mImmersiveModeStickyCheckBox: CheckBox
    private lateinit var mLowProfileCheckBox: CheckBox
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        val flagsView = inflater.inflate(R.layout.fragment_flags, container, false)
        mLowProfileCheckBox = flagsView.findViewById<View>(R.id.flag_enable_lowprof) as CheckBox
        mHideNavCheckbox = flagsView.findViewById<View>(R.id.flag_hide_navbar) as CheckBox
        mHideStatusBarCheckBox = flagsView.findViewById<View>(R.id.flag_hide_statbar) as CheckBox
        mImmersiveModeCheckBox = flagsView.findViewById<View>(R.id.flag_enable_immersive) as CheckBox
        mImmersiveModeStickyCheckBox = flagsView.findViewById<View>(R.id.flag_enable_immersive_sticky) as CheckBox
        val toggleFlagsButton = flagsView.findViewById<View>(R.id.btn_changeFlags) as Button
        toggleFlagsButton.setOnClickListener { toggleUiFlags() }
        val presetsImmersiveModeButton = flagsView.findViewById<View>(R.id.btn_immersive) as Button
        presetsImmersiveModeButton.setOnClickListener { // BEGIN_INCLUDE(immersive_presets)
            // For immersive mode, the FULLSCREEN, HIDE_HAVIGATION and IMMERSIVE
            // flags should be set (you can use IMMERSIVE_STICKY instead of IMMERSIVE
            // as appropriate for your app).  The LOW_PROFILE flag should be cleared.

            // Immersive mode is primarily for situations where the user will be
            // interacting with the screen, like games or reading books.
            var uiOptions = flagsView.systemUiVisibility
            uiOptions = uiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
            uiOptions = uiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
            flagsView.systemUiVisibility = uiOptions
            // END_INCLUDE(immersive_presets)
            dumpFlagStateToLog(uiOptions)

            // The below code just updates the checkboxes to reflect which flags have been set.
            mLowProfileCheckBox.isChecked = false
            mHideNavCheckbox.isChecked = true
            mHideStatusBarCheckBox.isChecked = true
            mImmersiveModeCheckBox.isChecked = true
            mImmersiveModeStickyCheckBox.isChecked = false
        }
        val presetsLeanbackModeButton = flagsView.findViewById<View>(R.id.btn_leanback) as Button
        presetsLeanbackModeButton.setOnClickListener { // BEGIN_INCLUDE(leanback_presets)
            // For leanback mode, only the HIDE_NAVE and HIDE_STATUSBAR flags
            // should be checked.  In this case IMMERSIVE should *not* be set,
            // since this mode is left as soon as the user touches the screen.
            var uiOptions = flagsView.systemUiVisibility
            uiOptions = uiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            uiOptions = uiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
            uiOptions = uiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
            flagsView.systemUiVisibility = uiOptions
            // END_INCLUDE(leanback_presets)
            dumpFlagStateToLog(uiOptions)

            // The below code just updates the checkboxes to reflect which flags have been set.
            mLowProfileCheckBox.isChecked = false
            mHideNavCheckbox.isChecked = true
            mHideStatusBarCheckBox.isChecked = true
            mImmersiveModeCheckBox.isChecked = false
            mImmersiveModeStickyCheckBox.isChecked = false
        }

        // Setting these flags makes the content appear under the navigation
        // bars, so that showing/hiding the nav bars doesn't resize the content
        // window, which can be jarring.
        var uiOptions = flagsView.systemUiVisibility
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        flagsView.systemUiVisibility = uiOptions
        return flagsView
    }

    /**
     * Helper method to dump flag state to the log.
     * @param uiFlags Set of UI flags to inspect
     */
    private fun dumpFlagStateToLog(uiFlags: Int) {
        if (uiFlags and View.SYSTEM_UI_FLAG_LOW_PROFILE != 0) {
            Log.i(TAG, "SYSTEM_UI_FLAG_LOW_PROFILE is set")
        } else {
            Log.i(TAG, "SYSTEM_UI_FLAG_LOW_PROFILE is unset")
        }
        if (uiFlags and View.SYSTEM_UI_FLAG_FULLSCREEN != 0) {
            Log.i(TAG, "SYSTEM_UI_FLAG_FULLSCREEN is set")
        } else {
            Log.i(TAG, "SYSTEM_UI_FLAG_FULLSCREEN is unset")
        }
        if (uiFlags and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION != 0) {
            Log.i(TAG, "SYSTEM_UI_FLAG_HIDE_NAVIGATION is set")
        } else {
            Log.i(TAG, "SYSTEM_UI_FLAG_HIDE_NAVIGATION is unset")
        }
        if (uiFlags and View.SYSTEM_UI_FLAG_IMMERSIVE != 0) {
            Log.i(TAG, "SYSTEM_UI_FLAG_IMMERSIVE is set")
        } else {
            Log.i(TAG, "SYSTEM_UI_FLAG_IMMERSIVE is unset")
        }
        if (uiFlags and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY != 0) {
            Log.i(TAG, "SYSTEM_UI_FLAG_IMMERSIVE_STICKY is set")
        } else {
            Log.i(TAG, "SYSTEM_UI_FLAG_IMMERSIVE_STICKY is unset")
        }
    }

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    private fun toggleUiFlags() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The "Decor View" is the parent view of the Activity.  It's also conveniently the easiest
        // one to find from within a fragment, since there's a handy helper method to pull it, and
        // we don't have to bother with picking a view somewhere deeper in the hierarchy and calling
        // "findViewById" on it.
        val decorView = activity!!.window.decorView
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        // END_INCLUDE (get_current_ui_flags)

        // BEGIN_INCLUDE (toggle_lowprofile_mode)
        // Low profile mode doesn't resize the screen at all, but it covers the nav & status bar
        // icons with black so they're less distracting.  Unlike "full screen" and "hide nav bar,"
        // this mode doesn't interact with immersive mode at all, but it's instructive when running
        // this sample to observe the differences in behavior.
        newUiOptions = if (mLowProfileCheckBox.isChecked) {
            newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        } else {
            newUiOptions and View.SYSTEM_UI_FLAG_LOW_PROFILE.inv()
        }
        // END_INCLUDE (toggle_lowprofile_mode)

        // BEGIN_INCLUDE (toggle_fullscreen_mode)
        // When enabled, this flag hides non-critical UI, such as the status bar,
        // which usually shows notification icons, battery life, etc
        // on phone-sized devices.  The bar reappears when the user swipes it down.  When immersive
        // mode is also enabled, the app-drawable area expands, and when the status bar is swiped
        // down, it appears semi-transparently and slides in over the app, instead of pushing it
        // down.
        newUiOptions = if (mHideStatusBarCheckBox.isChecked) {
            newUiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            newUiOptions and View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
        }
        // END_INCLUDE (toggle_fullscreen_mode)

        // BEGIN_INCLUDE (toggle_hidenav_mode)
        // When enabled, this flag hides the black nav bar along the bottom,
        // where the home/back buttons are.  The nav bar normally instantly reappears
        // when the user touches the screen.  When immersive mode is also enabled, the nav bar
        // stays hidden until the user swipes it back.
        newUiOptions = if (mHideNavCheckbox.isChecked) {
            newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        } else {
            newUiOptions and View.SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        }
        // END_INCLUDE (toggle_hidenav_mode)

        // BEGIN_INCLUDE (toggle_immersive_mode)
        // Immersive mode doesn't do anything without at least one of the previous flags
        // enabled.  When enabled, it allows the user to swipe the status and/or nav bars
        // off-screen.  When the user swipes the bars back onto the screen, the flags are cleared
        // and immersive mode is automatically disabled.
        newUiOptions = if (mImmersiveModeCheckBox.isChecked) {
            newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        } else {
            newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE.inv()
        }
        // END_INCLUDE (toggle_immersive_mode)

        // BEGIN_INCLUDE (toggle_immersive_mode_sticky)
        // There's actually two forms of immersive mode, normal and "sticky".  Sticky immersive mode
        // is different in 2 key ways:
        //
        // * Uses semi-transparent bars for the nav and status bars
        // * This UI flag will *not* be cleared when the user interacts with the UI.
        //   When the user swipes, the bars will temporarily appear for a few seconds and then
        //   disappear again.
        newUiOptions = if (mImmersiveModeStickyCheckBox.isChecked) {
            newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        } else {
            newUiOptions and View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY.inv()
        }
        // END_INCLUDE (toggle_immersive_mode_sticky)

        // BEGIN_INCLUDE (set_ui_flags)
        //Set the new UI flags.
        decorView.systemUiVisibility = newUiOptions
        // END_INCLUDE (set_ui_flags)
        dumpFlagStateToLog(uiOptions)
    }

    companion object {
        const val TAG = "AdvancedImmersiveModeFragment"
    }
}