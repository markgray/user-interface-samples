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
package com.example.android.elevationdrag

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
 * This sample demonstrates a drag and drop action on a circle. Elevation and z-translation are used
 * to render the shadows and the view is clipped using an Outline. (The original intention appears
 * to have been to have several shapes, but it actually only has a circle at the moment.)
 */
class MainActivity : SampleActivityBase() {
    /**
     * Whether the Log Fragment is currently shown
     */
    private var mLogShown = false

    /**
     * Called when the activity is starting. First we call our super's implementation of `onCreate`,
     * then we set our content view to our layout file [R.layout.activity_main]. There are two of
     * these: layout/activity_main.xml used by displays narrower than 720dp, and displays 720dp or
     * greater will use layout-w720dp/activity_main.xml with the only difference being that the
     * layout/activity_main.xml file contains a [ViewAnimator] that holds the sample description
     * and a `fragment` used for [LogFragment] with which child is displayed being toggled by our
     * option menu, and layout-w720dp/activity_main.xml displays both the sample description and a
     * `fragment` used for [LogFragment] at the same time.
     *
     * If our [Bundle] parameter [savedInstanceState] is `null` it means we are being started for
     * the first time so we use the [FragmentManager] for interacting with fragments associated with
     * this activity to start a new [FragmentTransaction] which we use to initialize our variable
     * `val transaction`, initialize our [ElevationDragFragment] variable `val fragment` with a new
     * instance, use `transaction` to replace any content in the container in our UI whose ID is
     * [R.id.sample_content_fragment] with `fragment` and then commit `transaction`. If it is
     * non-`null` we are being re-initialized after previously being shut down and the system will
     * take care of restoring our fragments.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut
     * down then this [Bundle] contains the data it most recently supplied in [onSaveInstanceState].
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = ElevationDragFragment()
            transaction.replace(R.id.sample_content_fragment, fragment)
            transaction.commit()
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu. You should place your menu
     * items in [Menu] parameter [menu]. This is only called once, the first time the options menu
     * is displayed. To update the menu every time it is displayed, see [onPrepareOptionsMenu]. When
     * you add items to the menu, you can implement the Activity's [onOptionsItemSelected] method to
     * handle them there.
     *
     * We use a [MenuInflater] for this context to inflate our menu layout file [R.menu.main]
     * into our [Menu] parameter [menu] and return `true` so that the menu will be displayed.
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
     * items or otherwise dynamically modify the contents.
     *
     * We initialize our [MenuItem] variable `val logToggle` by finding the item in our [Menu]
     * parameter [menu] with ID [R.id.menu_toggle_log], and set it to visible if the [View] in our
     * UI with ID [R.id.sample_output] is a [ViewAnimator] (layout file layout/activity_main.xml
     * for displays narrower than 720dp only, it is a `LinearLayout` in the layout file used for
     * 720dp or wider displays layout-w720dp/activity_main.xml). If our [Boolean] field [mLogShown]
     * is `true` we set the title of `logToggle` to "Hide Log" or to "Show Log" if it is `false`.
     * Finally we return the value returned by our super's implementation of `onPrepareOptionsMenu`
     * to the caller.
     *
     * @param menu The options menu as last shown or first initialized by [onCreateOptionsMenu].
     * @return You must return `true` for the menu to be displayed, if you return `false` it will
     * not be shown.
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val logToggle = menu.findItem(R.id.menu_toggle_log)
        logToggle.isVisible = findViewById<View>(R.id.sample_output) is ViewAnimator
        logToggle.setTitle(if (mLogShown) R.string.sample_hide_log else R.string.sample_show_log)
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     *
     * When the `itemId` property of our [MenuItem] parameter [item] is [R.id.menu_toggle_log] we
     * toggle the value of our [Boolean] field [mLogShown], initialize our [ViewAnimator] variable
     * `val output` by finding the [View] in our UI with ID [R.id.sample_output] and if [mLogShown]
     * is now `true` set the child view of `output` that will be displayed to 1 (our [LogFragment])
     * if it is now `false` we set the child view of `output` that will be displayed to 0 (the
     * text describing the sample). We then call [invalidateOptionsMenu] to report that the options
     * menu has changed, and should be recreated by a call to [onCreateOptionsMenu], and return
     * `true` to consume the event here. If the `itemID` is not [R.id.menu_toggle_log] we return the
     * value returned by our super's implementation of `onOptionsItemSelected`.
     *
     * @param item The menu item that was selected.
     * @return boolean Return `false` to allow normal menu processing to proceed, `true` to
     * consume it here.
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

    /** Create a chain of targets that will receive log data  */
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
        /**
         * TAG used for logging.
         */
        const val TAG = "MainActivity"
    }
}