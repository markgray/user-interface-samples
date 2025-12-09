/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.android.basicimmersivemode

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.android.common.activities.SampleActivityBase
import com.example.android.common.logger.Log
import com.example.android.common.logger.LogFragment
import com.example.android.common.logger.LogWrapper
import com.example.android.common.logger.MessageOnlyLogFilter

/**
 * Sample demonstrating the use of immersive mode to hide the system and navigation bars for
 * full screen applications.
 *
 * 'Immersive Mode' is a new UI mode which improves 'hide full screen' and 'hide nav bar'
 * modes, by letting users swipe the bars in and out.
 *
 * This sample demonstrates how to enable and disable immersive mode programmatically.
 *
 * Immersive mode was introduced in Android 4.4 (Api Level 19). It is toggled using the
 * SYSTEM_UI_FLAG_IMMERSIVE system ui flag. When combined with the SYSTEM_UI_FLAG_HIDE_NAVIGATION
 * and SYSTEM_UI_FLAG_FULLSCREEN  flags, hides the navigation and status bars and lets your app
 * capture all touch events on the screen.
 */
class MainActivity : SampleActivityBase() {
    /**
     * Called when the activity is starting. First we call [enableEdgeToEdge] to enable edge to
     * edge display, then we call our super's implementation of `onCreate`, and set our content
     * view to our layout file `R.layout.activity_main`.
     *
     * We initialize our [LinearLayout] variable `rootView` to the view with ID
     * `R.id.sample_main_layout` then call [ViewCompat.setOnApplyWindowInsetsListener]
     * to take over the policy for applying window insets to `rootView`, with the
     * `listener` argument a lambda that accepts the [View] passed the lambda
     * in variable `v` and the [WindowInsetsCompat] passed the lambda
     * in variable `windowInsets`. It initializes its [Insets] variable
     * `systemBars` to the [WindowInsetsCompat.getInsets] of `windowInsets` with
     * [WindowInsetsCompat.Type.systemBars] as the argument. It then gets the insets for the
     * IME (keyboard) using [WindowInsetsCompat.Type.ime]. It then updates
     * the layout parameters of `v` to be a [ViewGroup.MarginLayoutParams]
     * with the left margin set to `systemBars.left`, the right margin set to
     * `systemBars.right`, the top margin set to `systemBars.top`, and the bottom margin
     * set to the maximum of the system bars bottom inset and the IME bottom inset.
     * Finally it returns [WindowInsetsCompat.CONSUMED]
     * to the caller (so that the window insets will not keep passing down to
     * descendant views).
     *
     * If the [FragmentManager] for interacting with fragments associated with this activity is not
     * able to find a [Fragment] whose tag name is [FRAGTAG], we are being started for the first
     * time so we use the [FragmentManager] for interacting with fragments associated with this
     * activity to begin a [FragmentTransaction] which we save in our variable `val transaction`,
     * then we construct a new instance of [BasicImmersiveModeFragment] to initialize our variable
     * `val fragment`, use `transaction` to `add` `fragment` to the activity state using the tag
     * name [FRAGTAG] and then commit `transaction`.
     *
     * @param savedInstanceState we do not override [onSaveInstanceState] so do not use.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val rootView = findViewById<LinearLayout>(R.id.sample_main_layout)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v: View, windowInsets: WindowInsetsCompat ->
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            // Apply the insets as a margin to the view.
            v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = systemBars.left
                rightMargin = systemBars.right
                topMargin = systemBars.top
                bottomMargin = systemBars.bottom.coerceAtLeast(ime.bottom)
            }
            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }
        if (supportFragmentManager.findFragmentByTag(FRAGTAG) == null) {
            val transaction = supportFragmentManager.beginTransaction()
            val fragment = BasicImmersiveModeFragment()
            transaction.add(fragment, FRAGTAG)
            transaction.commit()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in to [Menu] parameter [menu]. This is only called once, the first time the options
     * menu is displayed. To update the menu every time it is displayed, see [onPrepareOptionsMenu].
     * We use a [MenuInflater] for this context to inflate our menu layout file `R.menu.main` into
     * our [Menu] parameter [menu]. This layout file holds a single menu item with resource ID
     * `R.id.sample_action`, whose title is "Toggle Immersive Mode!" Finally we return `true` so
     * that the [Menu] will be displayed.
     *
     * @param menu The options [Menu] in which you place your items.
     * @return You must return `true` for the menu to be displayed, if you return `false` it will
     * not be shown.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    /**
     * Create a chain of targets that will receive log data
     */
    override fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.logNode = logWrapper

        // Filter strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On screen logging via a fragment with a TextView.
        val logFragment = supportFragmentManager
            .findFragmentById(R.id.log_fragment) as LogFragment?
        msgFilter.next = (logFragment ?: return).logView
        (logFragment.logView ?: return).setTextAppearance(R.style.Log)
        (logFragment.logView ?: return).setBackgroundColor(Color.WHITE)
        Log.i(TAG, "Ready")
    }

    companion object {
        /**
         * TAG used for logging.
         */
        const val TAG: String = "MainActivity"

        /**
         * [Fragment] tag name we use when we add our [BasicImmersiveModeFragment] fragment to the
         * activity state.
         */
        const val FRAGTAG: String = "BasicImmersiveModeFragment"
    }
}
