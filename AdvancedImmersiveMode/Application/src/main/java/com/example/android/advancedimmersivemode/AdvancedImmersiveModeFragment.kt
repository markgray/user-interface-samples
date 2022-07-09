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
    /**
     * The [CheckBox] labeled "Hide Navigation bar" resource ID [R.id.flag_hide_navbar], toggles the
     * `setSystemUiVisibility` flag [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION] which requests that the
     * system navigation be temporarily hidden when set.
     */
    private lateinit var mHideNavCheckbox: CheckBox

    /**
     * The [CheckBox] labeled "Hide Status Bar" resource ID [R.id.flag_hide_statbar], toggles the
     * `setSystemUiVisibility` flag [View.SYSTEM_UI_FLAG_FULLSCREEN] which requests that the [View]
     * go into the normal fullscreen mode so that its content can take over the screen while still
     * allowing the user to interact with the application.
     */
    private lateinit var mHideStatusBarCheckBox: CheckBox

    /**
     * The [CheckBox] labeled "Enable Immersive Mode" resource ID [R.id.flag_enable_immersive],
     * toggles the `setSystemUiVisibility` flag [View.SYSTEM_UI_FLAG_IMMERSIVE] which requests that
     * the [View] would like to remain interactive when hiding the navigation bar. If this flag is
     * not set, [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION] will be force cleared by the system on any
     * user interaction.
     */
    private lateinit var mImmersiveModeCheckBox: CheckBox

    /**
     * The [CheckBox] labeled "Enable Immersive Mode (Sticky)" resource ID [R.id.flag_enable_immersive_sticky],
     * toggles the `setSystemUiVisibility` flag [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] which requests
     * that the [View] would like to remain interactive when hiding the status bar and/or hiding the
     * navigation bar. Use this flag to create an immersive experience while also hiding the system
     * bars. If this flag is not set, [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION] will be force cleared by
     * the system on any user interaction, and [View.SYSTEM_UI_FLAG_FULLSCREEN] will be force-cleared
     * by the system if the user swipes from the top of the screen.
     */
    private lateinit var mImmersiveModeStickyCheckBox: CheckBox

    /**
     * The [CheckBox] labeled "Enable Low Profile Mode" resource ID [R.id.flag_enable_lowprof],
     * toggles the `setSystemUiVisibility` flag [View.SYSTEM_UI_FLAG_LOW_PROFILE] which requests
     * that the [View] enter an unobtrusive "low profile" mode. This is for use in games, book
     * readers, video players, or any other "immersive" application where the usual system chrome is
     * deemed too distracting. In low profile mode, the status bar and/or navigation icons may dim.
     */
    private lateinit var mLowProfileCheckBox: CheckBox

    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in the
     * process of being created. As such, you can not rely on things like the activity's content
     * view hierarchy being initialized at this point. If you want to do work once the activity
     * itself is created, see [onActivityCreated]. Any restored child fragments will be created
     * before the base [Fragment.onCreate] method returns. First we call our super's implementation
     * of `onCreate`, then we call the [setHasOptionsMenu] method with `true` to report that this
     * fragment would like to participate in populating the options menu by receiving a call to
     * [onCreateOptionsMenu] and related methods.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    /**
     * Called to have the fragment instantiate its user interface view. This will be called between
     * [onCreate] and {[onActivityCreated]. It is recommended to __only__ inflate the layout in this
     * method and move logic that operates on the returned [View] to [onViewCreated].
     *
     * We use our [LayoutInflater] parameter [inflater] to inflate our layout file [R.layout.fragment_flags],
     * using our [ViewGroup] parameter [container] for its `LayoutParams` without attaching to it and
     * use the [View] that [inflater] returns to initialize our variable `val flagsView`.
     *
     * We find the [CheckBox] in `flagsView` with ID [R.id.flag_enable_lowprof] (labeled "Enable Low
     * Profile Mode") to initialize [mLowProfileCheckBox], find the [CheckBox] in `flagsView` with ID
     * [R.id.flag_hide_navbar] (labeled "Hide Navigation bar") to initialize [mHideNavCheckbox], find
     * the [CheckBox] in `flagsView` with ID [R.id.flag_hide_statbar] (labeled "Hide Status Bar") to
     * initialize [mHideStatusBarCheckBox], find the [CheckBox] in `flagsView` with ID
     * [R.id.flag_enable_immersive] (labeled "Enable Immersive Mode") to initialize [mImmersiveModeCheckBox],
     * and find the [CheckBox] in `flagsView` with ID [R.id.flag_enable_immersive_sticky] (labeled
     * "Enable Immersive Mode (Sticky)") to initialize [mImmersiveModeStickyCheckBox].
     *
     * We initialize our [Button] variable `val toggleFlagsButton` to the [Button] in `flagsView` with
     * ID [R.id.btn_changeFlags] (labeled "Do things!") and set its `OnClickListener` to a lambda
     * which calls our method [toggleUiFlags] to have it apply the current [CheckBox] selections to
     * our UI.
     *
     * We initialize our [Button] variable `val presetsImmersiveModeButton` to the [Button] in `flagsView`
     * with ID [R.id.btn_immersive] (labeled "Immersive Mode") and set its `OnClickListener` to a lambda
     * which sets the flags [View.SYSTEM_UI_FLAG_FULLSCREEN], [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION], and
     * [View.SYSTEM_UI_FLAG_IMMERSIVE] and clears the flags [View.SYSTEM_UI_FLAG_LOW_PROFILE], and
     * [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] and does the same thing to the checked state of the
     * [CheckBox] which toggles each of these flags.
     *
     * We initialize our [Button] variable `val presetsLeanbackModeButton` to the [Button] in `flagsView`
     * with ID [R.id.btn_leanback] (labeled "Leanback Mode") and set its `OnClickListener` to a lambda
     * which sets the flags [View.SYSTEM_UI_FLAG_FULLSCREEN], [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION],
     * and clears the flags [View.SYSTEM_UI_FLAG_LOW_PROFILE], [View.SYSTEM_UI_FLAG_IMMERSIVE] and
     * [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] and does the same thing to the checked state of the
     * [CheckBox] which toggles each of these flags.
     *
     * Next we set some flags which will make the content appear under the navigation bars, so that
     * showing or hiding the nav bars doesn't resize the content window, which can be jarring. To
     * do this we initialize our [Int] variable `var uiOptions` to the current `systemUiVisibility`
     * property of `flagsView`, then we set these flags in `uiOptions`:
     *  - [View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION] which requests that the [View] would like its
     *  window to be laid out as if it has requested [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION], even if
     *  it currently hasn't. This allows it to avoid artifacts when switching in and out of that mode,
     *  at the expense that some of its user interface may be covered by screen decorations when they
     *  are shown.
     *  - [View.SYSTEM_UI_FLAG_LAYOUT_STABLE] which requests that when using other layout flags, we
     *  would like a stable view of the content insets given to `fitSystemWindows`. This means that
     *  the insets seen there will always represent the worst case that the application can expect
     *  as a continuous state. In the stock Android UI this is the space for the system bar, nav bar,
     *  and status bar, but not more transient elements such as an input method.
     *  - [View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN] which requests that the [View] be laid out as if
     *  it has requested [View.SYSTEM_UI_FLAG_FULLSCREEN], even if it currently hasn't. This allows
     *  it to avoid artifacts when switching in and out of that mode, at the expense that some of
     *  its user interface may be covered by screen decorations when they are shown.
     *
     * Having added these flags to `uiOptions` we set the `systemUiVisibility` property of `flagsView`
     * to `uiOptions` and return 'flagsView` to the caller.
     *
     * @param inflater The [LayoutInflater] object that can be used to inflate
     * any views in the fragment.
     * @param container If non-`null`, this is the parent [ViewGroup] that the fragment's UI will be
     * attached to. The fragment should not add the view itself, but this can be used to generate
     * the `LayoutParams` of the view.
     * @param state If non-`null`, this fragment is being re-constructed from a previous saved
     * state as given here.
     * @return the [View] for the fragment's UI.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        state: Bundle?
    ): View? {
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
        var uiOptions: Int = flagsView.systemUiVisibility
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        uiOptions = uiOptions or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        flagsView.systemUiVisibility = uiOptions
        return flagsView
    }

    /**
     * Helper method to dump flag state to the log.
     *
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
     * Applies the system UI visibility flags selected by the checked and unchecked state of the
     * [CheckBox]'s of our UI to the `systemUiVisibility` property of the top-level window decor
     * view (the `Window` which contains the standard window frame/decorations and our content
     * inside of that). First we initialize our [View] variable `val decorView` to the top-level
     * window decor view, initialize our [Int] variable `val uiOptions` to the `systemUiVisibility`
     * property of `decorView` (its current SystemUiVisibility flags), and initialize our [Int]
     * variable `var newUiOptions` to `uiOptions`. Then we proceed to check the checked state of
     * each [CheckBox] in our UI and set or unset the SystemUiVisibility flag that they control:
     *  - [mLowProfileCheckBox] sets the [View.SYSTEM_UI_FLAG_LOW_PROFILE] flag in `newUiOptions` if
     *  checked (requests the system UI to enter an unobtrusive "low profile" mode where the status
     *  bar and/or navigation icons may dim). Clears the flag if unchecked.
     *  - [mHideStatusBarCheckBox] sets the [View.SYSTEM_UI_FLAG_FULLSCREEN] flag in `newUiOptions`
     *  if checked (request non-critical UI be hidden, such as the status bar, the bar reappears
     *  when the user swipes it down). Clears the flag if unchecked.
     *  - [mHideNavCheckbox] sets the [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION] flag in `newUiOptions`
     *  if checked (requests that the system navigation be temporarily hidden, nav bar normally
     *  instantly reappears when the user touches the screen but  when immersive mode is also enabled,
     *  the nav bar stays hidden until the user swipes it back). Clears the flag if unchecked.
     *  - [mImmersiveModeCheckBox] sets the [View.SYSTEM_UI_FLAG_IMMERSIVE] flag in `newUiOptions`
     *  if checked (when enabled, it allows the user to swipe the status and/or nav bars off-screen.
     *  When the user swipes the bars back onto the screen, the flags are cleared and immersive mode
     *  is automatically disabled). Clears the flag if unchecked.
     *  - [mImmersiveModeStickyCheckBox] sets the [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] flag in
     *  `newUiOptions` if checked (similar to [View.SYSTEM_UI_FLAG_IMMERSIVE] but it uses semi
     *  transparent bars for the nav and status bars, and this UI flag will *not* be cleared when
     *  the user interacts with the UI. When the user swipes, the bars will temporarily appear for
     *  a few seconds and then disappear again). Clears the flag if unchecked.
     *
     * After setting or clearing the new UI flags selected by the [CheckBox]'s in `newUiOptions` we
     * set the `systemUiVisibility` to `newUiOptions`, then call our [dumpFlagStateToLog] method with
     * `uiOptions` to have it log the old UI flags.
     */
    private fun toggleUiFlags() {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The "Decor View" is the parent view of the Activity.  It's also conveniently the easiest
        // one to find from within a fragment, since there's a handy helper method to pull it, and
        // we don't have to bother with picking a view somewhere deeper in the hierarchy and calling
        // "findViewById" on it.
        val decorView: View = requireActivity().window.decorView
        val uiOptions: Int = decorView.systemUiVisibility
        var newUiOptions: Int = uiOptions
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
        /**
         * TAG used for logging.
         */
        const val TAG: String = "AdvancedImmersiveModeFragment"
    }
}