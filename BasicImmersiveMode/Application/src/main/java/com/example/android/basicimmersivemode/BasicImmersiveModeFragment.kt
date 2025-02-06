/*
* Copyright (C) 2013 The Android Open Source Project
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
package com.example.android.basicimmersivemode

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.Window
import androidx.fragment.app.Fragment
import com.example.android.common.logger.Log

/**
 * The [Fragment] which toggles the visibility of the status bar and system navigation for the app
 * when the "Toggle Immersive Mode!" [MenuItem] in the options menu is clicked. It has no [View]
 * itself, but controls the top-level window decor view (containing the standard window frame
 * decorations and the client's content inside of that) of the current [Window] of the activity.
 */
class BasicImmersiveModeFragment : Fragment() {
    /**
     * Called to do initial creation of a fragment. This is called after [onAttach] and before
     * [onCreateView]. Note that this can be called while the fragment's activity is still in
     * the process of being created. As such, you can not rely on things like the activity's
     * content view hierarchy being initialized at this point. If you want to do work once the
     * activity itself is created, see [onActivityCreated].
     *
     * First we call our super's implementation of `onCreate`, then we call the [setHasOptionsMenu]
     * method with `true` to report that this fragment would like to participate in populating the
     * options menu by receiving a call to [onCreateOptionsMenu] and related methods.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state. We do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION") // TODO: Use MenuProvider
        setHasOptionsMenu(true)
    }

    /**
     * Called when all saved state has been restored into the view hierarchy of the fragment. This
     * can be used to do initialization based on saved state that you are letting the view hierarchy
     * track itself, such as whether check box widgets are currently checked. This is called after
     * [onViewCreated] and before [onStart].
     *
     * First we call our super's implementation of `onViewStateRestored`. Then we initialize our
     * [View] variable `val decorView` to the top-level window decor view (containing the standard
     * window frame decorations and the client's content inside of that) of the current [Window] of
     * the activity. Finally we set the [View.OnSystemUiVisibilityChangeListener] of `decorView` to
     * a lambda which retrieves the height of `decorView` to initialize its variable `val height`
     * and then log that height.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * this is the state. We do not override [onSaveInstanceState] so do not use.
     */
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val decorView: View = requireActivity().window.decorView
        @Suppress("DEPRECATION") // TODO: Use WindowInsets.isVisible(int) to find out about system bar visibilities by setting a View.OnApplyWindowInsetsListener on this view.
        decorView.setOnSystemUiVisibilityChangeListener {
            val height = decorView.height
            Log.i(TAG, "Current height: $height")
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected. If the `itemId` of our
     * [MenuItem] parameter [item] is `R.id.sample_action` ("Toggle Immersive Mode!") we call our
     * method [toggleHideyBar] to have it detect and toggle immersive mode. In any case we return
     * `true` to consume the event here.
     *
     * @param item The menu item that was selected.
     * @return boolean Return `false` to allow normal menu processing to proceed, `true` to consume
     * it here.
     */
    @Deprecated("Deprecated in Java") // TODO: Use MenuProvider
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.sample_action) {
            toggleHideyBar()
        }
        return true
    }

    /**
     * Detects and toggles immersive mode. First we initialize our [Int] variable `val uiOptions` to
     * the currently enabled UI options of the top-level window decor view of the current [Window]
     * of the activity, and initialize our variable `var newUiOptions` to `uiOptions`. We set our
     * [Boolean] variable `val isImmersiveModeEnabled` to `true` if the bit flag
     * [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY]  in `uiOptions` is set, and then if
     * `isImmersiveModeEnabled` is `true` we log "Turning immersive mode mode off.", and if it is
     * `false` we log "Turning immersive mode mode on." Then we proceed to toggle the value of the
     * following UI options in `newUiOptions`:
     *  - [View.SYSTEM_UI_FLAG_HIDE_NAVIGATION] requests that the system navigation be hidden.
     *  - [View.SYSTEM_UI_FLAG_FULLSCREEN] requests that the [View] go into normal fullscreen mode
     *  so that its content can take over the screen while still allowing the user to interact with
     *  the application.
     *  - [View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY] the [View] would like to remain interactive when
     *  hiding the status bar and hiding the navigation bar. The "sticky" form of immersive mode
     *  will let the user swipe the bars back in again, but will automatically make them disappear
     *  a few seconds later.
     */
    private fun toggleHideyBar() {
        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        val uiOptions = requireActivity().window.decorView.systemUiVisibility
        var newUiOptions = uiOptions
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        val isImmersiveModeEnabled = uiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY == uiOptions
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ")
        } else {
            Log.i(TAG, "Turning immersive mode mode on.")
        }

        // Immersive mode: Backward compatible to KitKat (API 19).
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // This sample uses the "sticky" form of immersive mode, which will let the user swipe
        // the bars back in again, but will automatically make them disappear a few seconds later.
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_FULLSCREEN
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        newUiOptions = newUiOptions xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        @Suppress("DEPRECATION") // TODO: SystemUiVisibility flags are deprecated. Use WindowInsetsController instead.
        requireActivity().window.decorView.systemUiVisibility = newUiOptions
        //END_INCLUDE (set_ui_flags)
    }

    companion object {
        /**
         * TAG used for logging.
         */
        const val TAG: String = "BasicImmersiveModeFragment"
    }
}
