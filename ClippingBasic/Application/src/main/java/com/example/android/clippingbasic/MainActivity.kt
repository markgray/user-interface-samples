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
package com.example.android.clippingbasic

import android.content.Context
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
 * [Fragment] which can display a view. For devices with displays with a width of 720dp or greater,
 * the sample log is always visible, on other devices it's visibility is controlled by an item on
 * the Action Bar.
 *
 * This is a basic app showing how to clip on a [View] using the `ViewOutlineProvider` interface,
 * by which a View builds the outline to be used for its shadowing and clipping.
 *
 */
class MainActivity : SampleActivityBase() {
    /**
     * Whether the Log Fragment is currently shown
     */
    private var mLogShown = false

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main]. The layout file used
     * for displays narrower then 720dp - layout/activity_main.xml consists of a root `LinearLayout`
     * holding a `ViewAnimator` with two child views: a `ScrollView` holding a `TextView` displaying
     * the sample description, and a `fragment` for our [LogFragment] (these are toggled between using
     * our options menu). Below the `ViewAnimator` in the `LinearLayout` is a 1dp "darker_gray" spacer
     * `View` and a `FrameLayout` with the ID [R.id.sample_content_fragment] which is used to hold
     * our sample fragment [ClippingBasicFragment]. The layout file used for displays 720dp and wider
     * is the file layout-w720dp/activity_main.xml which consists of a root horizontal `LinearLayout`
     * holding a vertical `LinearLayout` which holds a `FrameLayout` holding a `TextView` displaying
     * the sample description, followed by a 1dp "darker_gray" spacer `View`, followed by a `fragment`
     * for our [LogFragment]. To the right in the root `LinearLayout` is a 1dp "darker_gray" spacer
     * `View` and a `FrameLayout` with the ID [R.id.sample_content_fragment] which is used to hold
     * our sample fragment [ClippingBasicFragment].
     *
     * If our [Bundle] parameter [savedInstanceState] is non-`null` we are being restarted so our
     * [ClippingBasicFragment] will be restored by the system so we just return. If it is `null` we
     * are starting for the first time so we use the [FragmentManager] for interacting with fragments
     * associated with this activity to begin a [FragmentTransaction] with which we initialize our
     * variable `val transaction`, and initialize our [ClippingBasicFragment] variable `val fragment`
     * to a new instance. We then use `transaction` to replace any existing fragment that is in the
     * container with resource ID [R.id.sample_content_fragment] with `fragment`. Finally we "commit"
     * `transaction`.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     * ***Note: Otherwise it is null.*** We just use the fact that it is `null` only when we are being
     * started for the first time to determine whether we need to add our [ClippingBasicFragment] to
     * our UI -- if it is non-`null` the system framework will restore the old fragment.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = ClippingBasicFragment()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in the [Menu] parameter [menu]. This is only called once, the first time the options
     * menu is displayed. To update the menu every time it is displayed, see [onPrepareOptionsMenu].
     * We use a [MenuInflater] for our [Context] to inflate our menu layout file [R.menu.main] into
     * our [Menu] parameter [menu] (it consists of a single [MenuItem] with ID [R.id.menu_toggle_log]
     * whose title toggles between "Show Log" and "Hide Log" depending on whether the [LogFragment]
     * is invisible or visible at the moment). Finally we return `true` so that the menu will be
     * displayed.
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
     * `val logToggle` by finding the item in our [Menu] parameter [menu] with ID [R.id.menu_toggle_log]
     * and set its visibility to visible iff the view with ID [R.id.sample_output] is a [ViewAnimator]
     * (this is true only for displays narrower than 720dp, for wider displays the view is a
     * `LinearLayout` holding both the sample description and our [LogFragment] which is always
     * visible). The we set the title of `logToggle` to "Hide Log" is our [Boolean] field [mLogShown]
     * is `true` or to "Show Log" if it is `false`. Finally we return the value returned by our super's
     * implementation of `onPrepareOptionsMenu` to the caller.
     *
     * @param menu The options menu as last shown or first initialized by [onCreateOptionsMenu].
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
     * This hook is called whenever an item in your options menu is selected. When the item ID of our
     * [MenuItem] parameter [item] is [R.id.menu_toggle_log] we toggle the value of our [Boolean]
     * field [mLogShown] then initialize our [ViewAnimator] variable `val output` to the view with
     * ID [R.id.sample_output]. If [mLogShown] is now `true` we set the displayed child of `output`
     * to 1 (the `fragment` holding our [LogFragment]) and if it is now `false` we set its displayed
     * child to 0 (the `ScrollView` wrapped `TextView` displaying the sample description). We then
     * call the [invalidateOptionsMenu] method to report that the options menu has changed, so should
     * be recreated, and return `true` to consume the event here. If the item ID is not our [MenuItem]
     * we return the value returned by our super's implementation of `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return Return `false` to allow normal menu processing to proceed, `true` to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_toggle_log -> {
                mLogShown = !mLogShown
                val output: ViewAnimator = findViewById<View>(R.id.sample_output) as ViewAnimator
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
     * Create a chain of targets that will receive log data
     */
    override fun initializeLogging() {
        // Wraps Android's native log framework.
        val logWrapper = LogWrapper()
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper)

        // Filter strips out everything except the message text.
        val msgFilter = MessageOnlyLogFilter()
        logWrapper.next = msgFilter

        // On screen logging via a fragment with a TextView.
        val logFragment = supportFragmentManager
            .findFragmentById(R.id.log_fragment) as LogFragment?
        msgFilter.next = logFragment!!.logView
        Log.i(TAG, "Ready")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}