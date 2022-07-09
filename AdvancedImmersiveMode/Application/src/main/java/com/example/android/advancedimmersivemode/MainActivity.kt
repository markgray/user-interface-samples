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
package com.example.android.advancedimmersivemode

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ViewAnimator
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.android.common.activities.SampleActivityBase
import com.example.android.common.logger.Log
import com.example.android.common.logger.LogFragment
import com.example.android.common.logger.LogWrapper
import com.example.android.common.logger.MessageOnlyLogFilter

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * [Fragment] which can display a view.
 *
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
class MainActivity : SampleActivityBase() {
    /**
     * Whether the Log [Fragment] is currently shown
     */
    private var mLogShown = false

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * and then we set our content view to our layout file [R.layout.activity_main]. The default
     * layout file layout/activity_main.xml outer most View is a vertical `LinearLayout` which holds
     * a [ViewAnimator] which holds a `ScrollView` and a `fragment` (intended for our [LogFragment]),
     * as well as a `FrameLayout` to hold our [AdvancedImmersiveModeFragment]. The layout file for
     * displays with a width of 720dp or greater is layout-w720dp/activity_main.xml whose outer most
     * View is a horizontal `LinearLayout` which holds a vertical `LinearLayout` on the left which
     * holds a `TextView` displaying our intro message and a `fragment` for holding a [LogFragment],
     * and on the right is a `FrameLayout` to hold our [AdvancedImmersiveModeFragment].
     *
     * If our [Bundle] parameter [savedInstanceState] is `null` we are being started for the first
     * time so we use the [FragmentManager] for interacting with fragments associated with this
     * activity to begin a [FragmentTransaction] which we save in our variable `val transaction`,
     * then we construct a new instance of [AdvancedImmersiveModeFragment] to initialize our variable
     * `val fragment`, use `transaction` to `replace` the contents of the container with ID
     * [R.id.sample_content_fragment] with fragment and commit our [FragmentTransaction] variable
     * `transaction`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this is not `null`, and any [Fragment] we added before being shut down will be
     * restored by the system. If it *is* `null` we are starting for the first time and need to
     * construct and add our [AdvancedImmersiveModeFragment] to our UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = AdvancedImmersiveModeFragment()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in to [Menu] parameter [menu]. This is only called once, the first time the options
     * menu is displayed. To update the menu every time it is displayed, see [onPrepareOptionsMenu].
     * We use a [MenuInflater] for this context to inflate our menu layout file [R.menu.main] into
     * our [Menu] parameter [menu]. This layout file holds a single menu item with resource ID
     * [R.id.menu_toggle_log], whose title toggles between "Show Log" and "Hide Log" depending on
     * the value of our [Boolean] field [mLogShown], and it is set to invisible when we are running
     * on a display with a width of 720dp or greater (determined in our [onPrepareOptionsMenu]
     * override by checking whether the View with ID [R.id.sample_output] is a [ViewAnimator]).
     * Finally we return `true` so that the [Menu] will be displayed.
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
     * Prepare the Screen's standard options menu to be displayed. This is called right before the
     * menu is shown, every time it is shown. You can use this method to efficiently enable/disable
     * items or otherwise dynamically modify the contents. We initialize our [MenuItem] variable
     * `val logToggle` by finding the item with ID [R.id.menu_toggle_log] in our [Menu] parameter
     * [menu]. We set `logToggle` to visible if the View with ID [R.id.sample_output] is an instance
     * of [ViewAnimator] (which it is only when the display is narrower than 720dp). Then we set
     * the title of `logToggle` to "Hide Log" if our [Boolean] field [mLogShown] is `true` or to
     * "Show Log" if it is `false`. Finally we return the value returned by our super's implementation
     * of `onPrepareOptionsMenu` to our caller.
     *
     * @param menu The options [Menu] as last shown or first initialized by [onCreateOptionsMenu].
     * @return You must return `true` for the menu to be displayed, if you return `false` it will
     * not be shown.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val logToggle: MenuItem = menu.findItem(R.id.menu_toggle_log)
        logToggle.isVisible = findViewById<View>(R.id.sample_output) is ViewAnimator
        logToggle.setTitle(if (mLogShown) R.string.sample_hide_log else R.string.sample_show_log)
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected. When the `itemId` of
     * our [MenuItem] parameter [item] is [R.id.menu_toggle_log] we toggle the value of our [Boolean]
     * field [mLogShown], then initialize our [ViewAnimator] variable `val output` by finding the
     * [View] with ID [R.id.sample_output]. If [mLogShown] is now `true` we set the `displayedChild`
     * of `output` to 1 (our [LogFragment]), and if it is now `false` we set the `displayedChild` of
     * `output` to 0 (the `ScrollView` holding a `TextView` that displays our intro message). We then
     * call the [invalidateOptionsMenu] method to declare that the options menu should be recreated,
     * and return `true` to consume the event here. If the `itemId` of [menu] is not one of ours we
     * return the value returned by our super's implementation of `onOptionsItemSelected`.
     *
     * @param item The [MenuItem] that was selected.
     * @return boolean Return `false` to allow normal menu processing to proceed, `true` to consume
     * it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_toggle_log -> {
                mLogShown = !mLogShown
                val output = findViewById<View>(R.id.sample_output) as ViewAnimator
                if (mLogShown) {
                    output.displayedChild = 1
                } else {
                    output.displayedChild = 0
                }
                invalidateOptionsMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Create a chain of targets that will receive log data.
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
        Log.i(TAG, "Ready")
    }

    companion object {
        /**
         * TAG used for logging.
         */
        const val TAG: String = "MainActivity"
    }
}